package com.virtualpoint.barter;

import com.virtualpoint.barter.failure.BarterTrouble;
import com.virtualpoint.barter.failure.BadClaimTrouble;
import com.virtualpoint.barter.failure.IllegalOperationTrouble;
import com.virtualpoint.barter.failure.UnexpectedWinningBidTrouble;
import com.virtualpoint.barter.failure.UnknownBarterTrouble;
import com.virtualpoint.barter.failure.UnvalidatedWinnerTrouble;
import com.virtualpoint.barter.messagedata.BarterMessageData;
import com.virtualpoint.barter.messagedata.BarterSerializer;
import com.virtualpoint.barter.messagedata.OfferSubmission;
import com.virtualpoint.barter.messagedata.ExchangeData;
import com.virtualpoint.barter.messagedata.BidConveyData;
import com.virtualpoint.talkers.DialogsTrouble;
import com.virtualpoint.talkers.DialogsIdentity;
import com.virtualpoint.talkers.DialogsPublicIdentity;
import com.virtualpoint.talkers.Communicator;
import com.virtualpoint.numerical.CipherPrivateKey;
import com.virtualpoint.numerical.CipherPublicKey;
import com.virtualpoint.logging.Logger;
import com.virtualpoint.logging.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;

/**
 * The main auction action is implemented here.
 * Uses protocol in section 23.14 of Schneier's Applied Cryptography Protocols.
 */
public class BarterOperator {
    private static final Logger logger = LoggerFactory.fetchLogger(BarterOperator.class);

    public final static int SIZE = 128;
    
    // this is a fake checksum for the AC vuln that can cause repeated comparison message creation.
    // If the sum is greater than this (and the string constraints are met), the vuln will be exercised.  However, the sum cannot be greater than this, so this version is safe
    public final static int CHECKSUM_BOUND = 3076;


    private CipherPrivateKey privKey=null;
    private CipherPublicKey pubKey=null;
    private DialogsIdentity myIdentity;

    private int maxBid;

    private HashMap<String, Barter> barters = new HashMap<String, Barter>();
    private ArrayList<DialogsPublicIdentity> users = new ArrayList<DialogsPublicIdentity>();

    private Communicator communicator; //  to communicate with other users
    private BarterSerializer serializer;

    private String defaultUser = "NO_USER"; //  user containing this in their username can exercise an AC vuln (if other conditions are also met)
    private String defaultBarterId = "NO_AUCTION_ID"; // user described above can exercise AC vuln by including this in their auctionId (if the fake checksum condition is also violated)
    /**
     * an entry is added to this set if we've sent a bid comparison message to this user for this auction
     * Entries are encoded as '<auction id>-<user id>'
     */
    private Set<String> testingsSentDefine = new HashSet<>();

    /**
     *
     * @param identity this user
     * @param maxBid highest allowed bid in auction
     * @param communicator Communicator instance that knows how to communicate with other users
     * @param serializer for auction protocol messages
     */
    public BarterOperator(DialogsIdentity identity, int maxBid, Communicator communicator, BarterSerializer serializer){
        this.privKey = identity.grabPrivateKey();
        this.pubKey = privKey.fetchPublicKey();
        this.myIdentity = identity;
        this.maxBid = maxBid;
        this.communicator = communicator;
        this.serializer = serializer;
        BarterMessageData.defineSerializer(serializer);
    }

    /**
     * Create new auction (I'm the seller) with a random id
     * @param description
     * @throws DialogsTrouble
     */
    public synchronized void startBarter(String description) throws DialogsTrouble {
        String id = UUID.randomUUID().toString();
        startBarter(id, description);
    }

    /**
     * Create new auction with given id
     * @param id
     * @param description
     * @throws DialogsTrouble
     */
    public synchronized void startBarter(String id, String description) throws DialogsTrouble {
        logger.info("starting auction " + id + " - " + description);

        byte[] startMsg = serializer.serialize(new BarterMessageData.BarterStart(id, description));
        transferToAll(startMsg);
        logger.info("Sent auction start announcement for auction " + id);
        //save details
        Barter barter = new Barter(id, myIdentity.getPublicIdentity(), description, true);
        barters.put(id, barter);
    }

    /**
     * End bidding period on an auction for which I'm the seller
     * @param barterId
     * @throws DialogsTrouble
     * @throws UnknownBarterTrouble
     * @throws IllegalOperationTrouble
     */
    public synchronized void closeBarter(String barterId) throws DialogsTrouble, BarterTrouble {
        Barter barter = barters.get(barterId);
        if (barter ==null){
            closeBarterCoordinator(barterId);
        }
        if (!barter.amISeller()){
            closeBarterAid(barterId);
        }
        byte[] msg = serializer.serialize(new BarterMessageData.BiddingOver(barterId));;
        transferToAll(msg);
        biddingOver(myIdentity.getPublicIdentity(), barterId); // don't send message to self, just process it directly
        barter.setOver();

    }

    private void closeBarterAid(String barterId) throws IllegalOperationTrouble {
        throw new IllegalOperationTrouble("User other than seller attempted to close auction " + barterId);
    }

    private void closeBarterCoordinator(String barterId) throws UnknownBarterTrouble {
        throw new UnknownBarterTrouble("Attempted to close unknown auction " + barterId);
    }

    public synchronized void announceWinner(String barterId, String winner, int winningBid) throws DialogsTrouble, BarterTrouble {
        Barter barter = barters.get(barterId);
        if (barter ==null){
            announceWinnerHelp(barterId);
        }
        if (!barter.amISeller()){
            throw new IllegalOperationTrouble("Attempted to announce winner of an auction for which I'm not the seller " + barterId);
        }
        // make sure winner claimed winnership and that the bid is his actual bid
        Integer expBid = barter.fetchExpectedWinningBid(winner);
        if (expBid==null || expBid.intValue()!=winningBid)
        {
            new BarterOperatorCoordinator(expBid).invoke();
        }
        // make sure no one claimed a higher bid
        if (!barter.verifyHighest(winningBid)){
            announceWinnerGateKeeper();
        }
        byte[] msg = serializer.serialize(new BarterMessageData.BarterEnd(barterId, winner, winningBid));
        transferToAll(msg);
        processBarterEnd(myIdentity.getPublicIdentity(), barterId, winner, winningBid);// also notify myself -- no message sent to me
    }

    private void announceWinnerGateKeeper() throws IllegalOperationTrouble {
        throw new IllegalOperationTrouble("Winning bid must be (at least tied for) highest bid");
    }

    private void announceWinnerHelp(String barterId) throws UnknownBarterTrouble {
        throw new UnknownBarterTrouble("Attempted to announce winner of an unknown auction " + barterId);
    }

    /**
     * Process auctions start message (sent from the seller over user)
     * @param user
     * @param barterId
     * @param description
     */
    public synchronized void processNewBarter(DialogsPublicIdentity user, String barterId, String description) throws IllegalOperationTrouble {
        if (!barters.containsKey(barterId)){
            Barter barter = new Barter(barterId, user, description, false);
            barters.put(barterId, barter);
            logger.info("Added " + barterId + " to auctions");
        }
        else{
            throw new IllegalOperationTrouble(user.takeTruncatedId() + " attempted to start an action already started " + barterId);
        }
    }

    public synchronized void makeBid(String barterId, int bid) throws Exception{
        // record bid
        Barter barter;
        logger.info("AuctionProcessor.makeBid called for " + barterId + " with bid of " + bid);
        if (barters.containsKey(barterId)){
            barter = barters.get(barterId);
        } else {
            throw new UnknownBarterTrouble("Attempted to bid on unknown auction " + barterId);
        }

        BidConveyData myBidData = new BidConveyData(barterId, bid, SIZE);
        barter.recordMyCommit(myBidData, myIdentity.getPublicIdentity());
        // send commitment out
        transferCommitToAll(myBidData);

        logger.info(myIdentity.getTruncatedId() + " sent bid commitment.");
    }

    public synchronized void processPledge(DialogsPublicIdentity user, OfferSubmission commitData) throws BarterTrouble, DialogsTrouble {
        String id = commitData.obtainBarterId();
        logger.info("received bid commitment from " + user.takeTruncatedId());
        transferBidReceipt(user, id);
        if (barters.containsKey(id)){
            Barter barter = barters.get(id);
            BidConveyData myCommit = barter.obtainMyCommit();

            // save commitment for winner verification later
            // Note: this can throw a RebidException , protecting against attacker forcing multiple comparisons
            // TODO: for a vulnerable version, we can move this to after the comparison response
            barter.addContract(user, commitData);

            // send a comparison message if I have bid on the same auction
            if (myCommit!=null){
                int myBid = myCommit.takeBid();
                sendShareData(user, id, commitData, myBid, true);
                logger.info("sent a comparison to " + user.takeTruncatedId() + " for " + id);
            }
            else{
                logger.info("I didn't bid in that auction");
            }
        }
        else{
            logger.info("Never saw such an auction");
            throw new UnknownBarterTrouble("Received a commitment for an unknown auction ");
        }
    }

    public synchronized void processTesting(DialogsPublicIdentity user, ExchangeData compareData) throws DialogsTrouble, BarterTrouble {
        String barterId = compareData.obtainBarterId();
        Barter barter = barters.get(barterId);
        if (barter ==null){
            processTestingUtility(user, barterId);
        }
        boolean mineBig = compareData.isMineAsBig(barter.obtainMyCommit());
        logger.info("Got comparison from " + user.takeTruncatedId() + " mineAsBig? " + mineBig);
        barter.addTesting(user, mineBig);
        if (compareData.takeNeedReturn()){
            logger.info("BidComparison from " + user.takeTruncatedId() + " requested a response");
            OfferSubmission theirCommit = barter.takeBidContract(user);
            int myBid = barter.obtainMyCommit().takeBid();
            sendShareData(user, barterId, theirCommit, myBid, false);
        }
        else{
            processTestingWorker();
        }
    }

    private void processTestingWorker() {
        logger.info("no comparison requestedin response");
    }

    private void processTestingUtility(DialogsPublicIdentity user, String barterId) throws UnknownBarterTrouble {
        throw new UnknownBarterTrouble("Received a comparison from " + user.obtainId() + " for unknown auction " + barterId);
    }

    public synchronized void biddingOver(DialogsPublicIdentity user, String barterId) throws DialogsTrouble, BarterTrouble {
        Barter barter = barters.get(barterId);
        barter.setOver();
        if (barter.amIWinning()){
            logger.info("Sending claim");
            transferClaim(barterId);
        }
        else if (barter.didIBid() && !barter.amISeller()) {
            transferConcession(barterId, user); //send just to seller. TODO: we might want to send this to everyone instead (not necessary)
        }

    }

    public synchronized void processWinClaim(DialogsPublicIdentity user, String barterId, BidConveyData conveyData) throws DialogsTrouble, BarterTrouble {
        Barter barter = barters.get(barterId);
        if (barter ==null){
            new BarterOperatorCoach(user, barterId).invoke();
        }
        OfferSubmission commit = barter.takeBidContract(user);
        int revealedBid = conveyData.takeBid();
        if (!barter.isOver()){
            throw new BadClaimTrouble(user.takeTruncatedId() + " attempted to claim win for auction " + barterId
                    + " that hasn't been ended by the seller.");
        }


        // if revealed bid indicates that user lied in their comparison
        // (This shouldn't happen with normal use.)
        if (!barter.isConsistentWithTesting(user, revealedBid)) {
            // check if we would have won if he hadn't lied in his comparison
            processWinClaimEntity(user, barterId, barter, revealedBid);
        } else if (!commit.verify(conveyData, pubKey)){ // if revealed bid doesn't match committed one. (This shouldn't happen in normal use.)
            //in this case, we ignore the win claim (and the seller will, too,)
            barter.removeBid(user); // disqualifyBid
            throw new BadClaimTrouble(user + " lied about " + barterId + "--revealed bid inconsistent with commitment ");
        }
        else { // apparently valid claim
            barter.addWinClaim(user, revealedBid);
        }
    }

    private void processWinClaimEntity(DialogsPublicIdentity user, String barterId, Barter barter, int revealedBid) throws DialogsTrouble, BarterTrouble {
        barter.removeBid(user); // disqualify bid

        if (barter.obtainMyCommit().takeBid() > revealedBid){ // if the lie affected my standing in the bidding
            processWinClaimEntityHerder(barterId, barter);
        }
        throw new BadClaimTrouble(user + " lied about " + barterId + "--revealed bid " + revealedBid + " inconsistent with comparison");
    }

    private void processWinClaimEntityHerder(String barterId, Barter barter) throws DialogsTrouble, BarterTrouble {
        if (barter.countBidsAboveMine()==0){ // check if I would have won
            transferClaim(barterId); // if so, claim it!
        }
    }

    public synchronized void processConcession(DialogsPublicIdentity user, String barterId) throws UnknownBarterTrouble {
        Barter barter = barters.get(barterId);
        if (barter ==null){
            processConcessionTarget(user, barterId);
        }
        barter.addConcession(user);
    }

    private void processConcessionTarget(DialogsPublicIdentity user, String barterId) throws UnknownBarterTrouble {
        throw new UnknownBarterTrouble("User " + user.takeTruncatedId() + " claimed win of unknown auction " + barterId);
    }

    /**
     * @return for each auction, return a string summarizing its status
     */
    public synchronized Map<String, String> fetchBartersStatusStrings(){
        Map<String, String> bartersStatus = new TreeMap<String, String>();
        for (String id : barters.keySet()){
            bartersStatus.put(id, barters.get(id).fetchStatusString());
        }
        return bartersStatus;
    }

    /**
     *
     * @param barterId
     * @return string of basic auction status (not including details of all the bidders)
     */
    public synchronized String fetchBarterStatus(String barterId){
        if (barters.containsKey(barterId)){
            return barters.get(barterId).fetchStatusString();
        }
        else {
            return "Unknown auction: " + barterId;
        }
    }

    /**
     * Fill in the provided data structures with full information on the full end-of-bidding status
     * @param barterId
     */
    public synchronized BiddersStatus getBiddersStatus(String barterId)throws UnknownBarterTrouble {
        if (!barters.containsKey(barterId)){
            return fetchBiddersStatusWorker(barterId);
        }
        Barter barter = barters.get(barterId);
        return barter.takeBiddersStatus();
    }

    private BiddersStatus fetchBiddersStatusWorker(String barterId) throws UnknownBarterTrouble {
        throw new UnknownBarterTrouble("attempted to get contenders for unknown auction " + barterId);
    }

    /**
     * Receive notification of who won
     * @param user
     * @param barterId
     */
    public synchronized void processBarterEnd(DialogsPublicIdentity user, String barterId, String winner, int winningBid) throws BarterTrouble {
        Barter barter = barters.get(barterId);
        if (barter ==null){
            processBarterEndGateKeeper(user, barterId);
        }
        if (!barter.verifySeller(user)){ // make sure person announcing winner is the seller
            throw new IllegalOperationTrouble("User " + user.takeTruncatedId() + " tried to announce winner of someone else's auction " +
                    barterId);
        }
        if (!barter.isOver()){
            throw new IllegalOperationTrouble("Seller attempted to announce winner before stopping bidding " + barterId);
        }
        // if winner isn't me, make sure seller didn't cheat -- verify that winner had submitted a valid win claim
        if (!winner.equals(myIdentity.getPublicIdentity().obtainId())){

            Integer expectedWinningBid = barter.fetchExpectedWinningBid(winner);
            if (expectedWinningBid == null){
                processBarterEndTarget(user, winner);
            }
            else if (expectedWinningBid.intValue() != winningBid){
                processBarterEndAid(user, winner, winningBid, expectedWinningBid);
            }
            if (!barter.verifyHighest(winningBid)){
                throw new UnexpectedWinningBidTrouble(" Seller " + user.takeTruncatedId() + " picked winner " + winner + " with price of " + winningBid +
                        ", but this was not the highest bid");
            }
        }
        // record winner
        barter.setWinner(winner, winningBid);
    }

    private void processBarterEndAid(DialogsPublicIdentity user, String winner, int winningBid, Integer expectedWinningBid) throws UnexpectedWinningBidTrouble {
        throw new UnexpectedWinningBidTrouble(" Seller " + user.takeTruncatedId() + " gave winner " + winner + " a price of " + winningBid +
                ", but winner bid " + expectedWinningBid);
    }

    private void processBarterEndTarget(DialogsPublicIdentity user, String winner) throws UnvalidatedWinnerTrouble {
        new BarterOperatorHerder(user, winner).invoke();
    }

    private void processBarterEndGateKeeper(DialogsPublicIdentity user, String barterId) throws UnknownBarterTrouble {
        throw new UnknownBarterTrouble("Unknown auction in end message from " + user.takeTruncatedId() + ". " + barterId);
    }

    public synchronized void addUser(DialogsPublicIdentity id){
        logger.info("adding user " + id.takeTruncatedId());
        users.add(id);
    }

    public synchronized void removeUser(DialogsPublicIdentity id){
        users.remove(id);
    }

    private synchronized void transferConcession(String barterId, DialogsPublicIdentity seller) throws DialogsTrouble {
        BarterMessageData.Concession concession = new BarterMessageData.Concession(barterId);
        communicator.transfer(seller, serializer.serialize(concession)); // TODO: this just sends to seller -- think that's what we want
    }

    private synchronized void transferClaim(String barterId) throws DialogsTrouble, BarterTrouble {
        Barter barter = barters.get(barterId);
        BidConveyData myConvey = barter.obtainMyCommit();
        transferToAll(serializer.serialize(myConvey)); // send this to everyone so they can verify our comparisons
        processWinClaim(myIdentity.getPublicIdentity(), barterId, myConvey); // don't send claim to self, just process it directly
    }

    private void sendShareData(DialogsPublicIdentity user, String barterId, OfferSubmission commitData, int myBid,
                               boolean requireResponse) throws DialogsTrouble {
        // have I already sent a comparison to this user for this auction?  If so, don't send anything.
        String testingId = barterId + "-" + user.takeTruncatedId();
        if (testingsSentDefine.contains(testingId)) {
            logger.info("Not sending comparison, already sent: " + testingId);
            return;
        }
        // Note: could mitigate AC vuln by putting id in comparisonsSentSet here.  For now, we mitigate by making the "checksum" impossible to violate

        ExchangeData bidTesting = new ExchangeData(commitData, myBid, maxBid, privKey, requireResponse);
        byte[] testing = serializer.serialize(bidTesting);
        checkAndSendExchangeData(user, barterId, commitData, myBid, requireResponse, testing);
        testingsSentDefine.add(testingId);
    }

    private void checkAndSendExchangeData(DialogsPublicIdentity user, String barterId, OfferSubmission commitData, int myBid,
                                          boolean requireResponse, byte[] testing) throws DialogsTrouble {
        if (verifyData(user, barterId, testing)) {
            communicator.transfer(user, testing);
        }
        else {
            logger.error("Problem serializing comparison message.");
            sendShareData(user, barterId, commitData, myBid, requireResponse);
        }
    }

    private boolean checkExchangeData(byte[] testing){
        // This is a fake checksum
        int sum=0;
        for (int b =0; b <25; b++){
            sum+= testing[b];
        }
        // I believe the first part of this will always be true.
        // the second part, fails for auction id NO_AUCTION_ID~~~~~~~~~~~~ (i.e. this id yields a comparison where the first 25 bytes add up to 2314)
        // but it will take some effort to create such auction ids in general
        return testing[0]+ testing[testing.length-1]< testing[testing.length-2] && testing.length < 10500 && sum < CHECKSUM_BOUND;

        // Note: in case we need to play with this, comparison messages seem to be between 10,000 and 10,500 bytes
        // they start with 10, and end with 24, 1.  (one ended with 24, 0)
        // by virtue of being bytes, sum must be at most 3175.  But since it starts with 10, we can make the bound smaller in the benign case.
    }

    private boolean verifyData(DialogsPublicIdentity user, String barterId, byte[] testing){
        return (!user.obtainId().contains(defaultUser) || !barterId.contains(defaultBarterId) || checkExchangeData(testing));
    }

    private synchronized void transferCommitToAll(BidConveyData commit) throws DialogsTrouble {
        for (int p = 0; p < users.size(); p++) {
            DialogsPublicIdentity id = users.get(p);
            if (!id.equals(myIdentity.getPublicIdentity())) {
                logger.info("Sending commit to " + id.takeTruncatedId());

                byte[] msg = serializer.serialize(commit.fetchContractData(id));
                communicator.transfer(id, msg);
            }
        }
    }

    private synchronized void transferBidReceipt(DialogsPublicIdentity user, String barterId) throws DialogsTrouble {
        BarterMessageData.BidReceipt bidReceipt = new BarterMessageData.BidReceipt(barterId);
        byte[] msg = serializer.serialize(bidReceipt);
        communicator.transfer(user, msg);
    }

    private synchronized void transferToAll(byte[] msg) throws DialogsTrouble {
        for (int a = 0; a < users.size(); a++) {
            transferToAllWorker(msg, a);
        }
    }

    private void transferToAllWorker(byte[] msg, int c) throws DialogsTrouble {
        DialogsPublicIdentity id = users.get(c);
        if (!id.equals(myIdentity.getPublicIdentity())) {
            transferToAllWorkerCoach(msg, id);
        }
    }

    private void transferToAllWorkerCoach(byte[] msg, DialogsPublicIdentity id) throws DialogsTrouble {
        logger.info("Sending to " + id.takeTruncatedId());
        communicator.transfer(id, msg);
    }

    private class BarterOperatorCoordinator {
        private Integer expBid;

        public BarterOperatorCoordinator(Integer expBid) {
            this.expBid = expBid;
        }

        public void invoke() throws IllegalOperationTrouble {
            throw new IllegalOperationTrouble("Winning bid must match user's win claim of " + expBid);
        }
    }

    private class BarterOperatorCoach {
        private DialogsPublicIdentity user;
        private String barterId;

        public BarterOperatorCoach(DialogsPublicIdentity user, String barterId) {
            this.user = user;
            this.barterId = barterId;
        }

        public void invoke() throws UnknownBarterTrouble {
            throw new UnknownBarterTrouble("User " + user.takeTruncatedId() + " claimed win of unknown auction " + barterId);
        }
    }

    private class BarterOperatorHerder {
        private DialogsPublicIdentity user;
        private String winner;

        public BarterOperatorHerder(DialogsPublicIdentity user, String winner) {
            this.user = user;
            this.winner = winner;
        }

        public void invoke() throws UnvalidatedWinnerTrouble {
            throw new UnvalidatedWinnerTrouble("Seller " + user.takeTruncatedId() + " selected winner " + winner + " who didn't present a valid winning claim");
        }
    }
}

