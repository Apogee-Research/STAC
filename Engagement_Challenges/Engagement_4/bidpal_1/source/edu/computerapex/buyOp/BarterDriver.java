package edu.computerapex.buyOp;

import edu.computerapex.buyOp.bad.BarterDeviation;
import edu.computerapex.buyOp.bad.BadClaimDeviation;
import edu.computerapex.buyOp.bad.IllegalOperationDeviation;
import edu.computerapex.buyOp.bad.UnexpectedWinningBidDeviation;
import edu.computerapex.buyOp.bad.UnknownBarterDeviation;
import edu.computerapex.buyOp.bad.UnvalidatedWinnerDeviation;
import edu.computerapex.buyOp.messagedata.BarterMessageData;
import edu.computerapex.buyOp.messagedata.BarterSerializer;
import edu.computerapex.buyOp.messagedata.BidCommitmentData;
import edu.computerapex.buyOp.messagedata.ExchangeData;
import edu.computerapex.buyOp.messagedata.BidDivulgeData;
import edu.computerapex.dialogs.CommunicationsDeviation;
import edu.computerapex.dialogs.CommunicationsIdentity;
import edu.computerapex.dialogs.CommunicationsPublicIdentity;
import edu.computerapex.dialogs.Communicator;
import edu.computerapex.math.EncryptionPrivateKey;
import edu.computerapex.math.EncryptionPublicKey;
import edu.computerapex.logging.Logger;
import edu.computerapex.logging.LoggerFactory;

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
public class BarterDriver {
    private static final Logger logger = LoggerFactory.takeLogger(BarterDriver.class);

    public final static int SIZE = 128;
    
    // this is a fake checksum for the AC vuln that can cause repeated comparison message creation.
    // If the sum is greater than this (and the string constraints are met), the vuln will be exercised.  However, the sum cannot be greater than this, so this version is safe
    public final static int CHECKSUM_BOUND = 3076;
    

    private EncryptionPrivateKey privKey=null;
    private EncryptionPublicKey pubKey=null;
    private CommunicationsIdentity myIdentity;

    private int maxBid;

    private HashMap<String, Barter> barters = new HashMap<String, Barter>();
    private ArrayList<CommunicationsPublicIdentity> participants = new ArrayList<CommunicationsPublicIdentity>();

    private Communicator communicator; //  to communicate with other users
    private BarterSerializer serializer;

    private String defaultParticipant = "NO_USER"; //  user containing this in their username can exercise an AC vuln (if other conditions are also met)
    private String defaultBarterId = "NO_AUCTION_ID"; // user described above can exercise AC vuln by including this in their auctionId (if the fake checksum condition is also violated)
    /**
     * an entry is added to this set if we've sent a bid comparison message to this user for this auction
     * Entries are encoded as '<auction id>-<user id>'
     */
    private Set<String> measurementsSentSet = new HashSet<>();

    /**
     *
     * @param identity this user
     * @param maxBid highest allowed bid in auction
     * @param communicator Communicator instance that knows how to communicate with other users
     * @param serializer for auction protocol messages
     */
    public BarterDriver(CommunicationsIdentity identity, int maxBid, Communicator communicator, BarterSerializer serializer){
        this.privKey = identity.fetchPrivateKey();
        this.pubKey = privKey.getPublicKey();
        this.myIdentity = identity;
        this.maxBid = maxBid;
        this.communicator = communicator;
        this.serializer = serializer;
        BarterMessageData.setSerializer(serializer);
    }

    /**
     * Create new auction (I'm the seller) with a random id
     * @param description
     * @throws CommunicationsDeviation
     */
    public synchronized void startBarter(String description) throws CommunicationsDeviation {
        String id = UUID.randomUUID().toString();
        startBarter(id, description);
    }

    /**
     * Create new auction with given id
     * @param id
     * @param description
     * @throws CommunicationsDeviation
     */
    public synchronized void startBarter(String id, String description) throws CommunicationsDeviation {
        logger.info("starting auction " + id + " - " + description);

        byte[] startMsg = serializer.serialize(new BarterMessageData.BarterStart(id, description));
        deliverToAll(startMsg);
        logger.info("Sent auction start announcement for auction " + id);
        //save details
        Barter barter = new Barter(id, myIdentity.takePublicIdentity(), description, true);
        barters.put(id, barter);
    }

    /**
     * End bidding period on an auction for which I'm the seller
     * @param barterId
     * @throws CommunicationsDeviation
     * @throws UnknownBarterDeviation
     * @throws IllegalOperationDeviation
     */
    public synchronized void closeBarter(String barterId) throws CommunicationsDeviation, BarterDeviation {
        Barter barter = barters.get(barterId);
        if (barter ==null){
            throw new UnknownBarterDeviation("Attempted to close unknown auction " + barterId);
        }
        if (!barter.amISeller()){
            throw new IllegalOperationDeviation("User other than seller attempted to close auction " + barterId);
        }
        byte[] msg = serializer.serialize(new BarterMessageData.BiddingOver(barterId));;
        deliverToAll(msg);
        biddingOver(myIdentity.takePublicIdentity(), barterId); // don't send message to self, just process it directly
        barter.fixOver();

    }

    public synchronized void announceWinner(String barterId, String winner, int winningBid) throws CommunicationsDeviation, BarterDeviation {
        Barter barter = barters.get(barterId);
        if (barter ==null){
            throw new UnknownBarterDeviation("Attempted to announce winner of an unknown auction " + barterId);
        }
        if (!barter.amISeller()){
            throw new IllegalOperationDeviation("Attempted to announce winner of an auction for which I'm not the seller " + barterId);
        }
        // make sure winner claimed winnership and that the bid is his actual bid
        Integer expBid = barter.fetchExpectedWinningBid(winner);
        if (expBid==null || expBid.intValue()!=winningBid)
        {
            throw new IllegalOperationDeviation("Winning bid must match user's win claim of " + expBid);
        }
        // make sure no one claimed a higher bid
        if (!barter.verifyHighest(winningBid)){
            throw new IllegalOperationDeviation("Winning bid must be (at least tied for) highest bid");
        }
        byte[] msg = serializer.serialize(new BarterMessageData.BarterEnd(barterId, winner, winningBid));
        deliverToAll(msg);
        processBarterEnd(myIdentity.takePublicIdentity(), barterId, winner, winningBid);// also notify myself -- no message sent to me
    }

    /**
     * Process auctions start message (sent from the seller over user)
     * @param participant
     * @param barterId
     * @param description
     */
    public synchronized void processNewBarter(CommunicationsPublicIdentity participant, String barterId, String description) throws IllegalOperationDeviation {
        if (!barters.containsKey(barterId)){
            Barter barter = new Barter(barterId, participant, description, false);
            barters.put(barterId, barter);
            logger.info("Added " + barterId + " to auctions");
        }
        else{
            throw new IllegalOperationDeviation(participant.obtainTruncatedId() + " attempted to start an action already started " + barterId);
        }
    }

    public synchronized void makeBid(String barterId, int bid) throws Exception{
        // record bid
        Barter barter;
        logger.info("AuctionProcessor.makeBid called for " + barterId + " with bid of " + bid);
        if (barters.containsKey(barterId)){
            barter = barters.get(barterId);
        } else {
            throw new UnknownBarterDeviation("Attempted to bid on unknown auction " + barterId);
        }

        BidDivulgeData myBidData = new BidDivulgeData(barterId, bid, SIZE);
        barter.recordMyCommit(myBidData, myIdentity.takePublicIdentity());
        // send commitment out
        deliverCommitToAll(myBidData);

        logger.info(myIdentity.pullTruncatedId() + " sent bid commitment.");
    }

    public synchronized void processOffer(CommunicationsPublicIdentity participant, BidCommitmentData commitData) throws BarterDeviation, CommunicationsDeviation {
        String id = commitData.fetchBarterId();
        logger.info("received bid commitment from " + participant.obtainTruncatedId());
        deliverBidReceipt(participant, id);
        if (barters.containsKey(id)){
            Barter barter = barters.get(id);
            BidDivulgeData myCommit = barter.takeMyCommit();

            // save commitment for winner verification later
            // Note: this can throw a RebidException , protecting against attacker forcing multiple comparisons
            // TODO: for a vulnerable version, we can move this to after the comparison response
            barter.addCommitment(participant, commitData);

            // send a comparison message if I have bid on the same auction
            if (myCommit!=null){
                int myBid = myCommit.grabBid();
                sendShareData(participant, id, commitData, myBid, true);
                logger.info("sent a comparison to " + participant.obtainTruncatedId() + " for " + id);
            }
            else{
                new BarterDriverCoordinator().invoke();
            }
        }
        else{
            logger.info("Never saw such an auction");
            throw new UnknownBarterDeviation("Received a commitment for an unknown auction ");
        }
    }

    public synchronized void processMeasurement(CommunicationsPublicIdentity participant, ExchangeData compareData) throws CommunicationsDeviation, BarterDeviation {
        String barterId = compareData.fetchBarterId();
        Barter barter = barters.get(barterId);
        if (barter ==null){
            throw new UnknownBarterDeviation("Received a comparison from " + participant.takeId() + " for unknown auction " + barterId);
        }
        boolean mineBig = compareData.isMineAsBig(barter.takeMyCommit());
        logger.info("Got comparison from " + participant.obtainTruncatedId() + " mineAsBig? " + mineBig);
        barter.addMeasurement(participant, mineBig);
        if (compareData.takeNeedReturn()){
            logger.info("BidComparison from " + participant.obtainTruncatedId() + " requested a response");
            BidCommitmentData theirCommit = barter.fetchBidCommitment(participant);
            int myBid = barter.takeMyCommit().grabBid();
            sendShareData(participant, barterId, theirCommit, myBid, false);
        }
        else{
            new BarterDriverHandler().invoke();
        }
    }

    public synchronized void biddingOver(CommunicationsPublicIdentity participant, String barterId) throws CommunicationsDeviation, BarterDeviation {
        Barter barter = barters.get(barterId);
        barter.fixOver();
        if (barter.amIWinning()){
            logger.info("Sending claim");
            deliverClaim(barterId);
        }
        else if (barter.didIBid() && !barter.amISeller()) {
            deliverConcession(barterId, participant); //send just to seller. TODO: we might want to send this to everyone instead (not necessary)
        }

    }

    public synchronized void processWinClaim(CommunicationsPublicIdentity participant, String barterId, BidDivulgeData divulgeData) throws CommunicationsDeviation, BarterDeviation {
        Barter barter = barters.get(barterId);
        if (barter ==null){
            throw new UnknownBarterDeviation("User " + participant.obtainTruncatedId() + " claimed win of unknown auction " + barterId);
        }
        BidCommitmentData commit = barter.fetchBidCommitment(participant);
        int revealedBid = divulgeData.grabBid();
        if (!barter.isOver()){
            throw new BadClaimDeviation(participant.obtainTruncatedId() + " attempted to claim win for auction " + barterId
                    + " that hasn't been ended by the seller.");
        }


        // if revealed bid indicates that user lied in their comparison
        // (This shouldn't happen with normal use.)
        if (!barter.isConsistentWithMeasurement(participant, revealedBid)) {
            // check if we would have won if he hadn't lied in his comparison
            barter.removeBid(participant); // disqualify bid

            if (barter.takeMyCommit().grabBid() > revealedBid){ // if the lie affected my standing in the bidding
                if (barter.countBidsAboveMine()==0){ // check if I would have won
                    new BarterDriverExecutor(barterId).invoke();
                }
            }
            throw new BadClaimDeviation(participant + " lied about " + barterId + "--revealed bid " + revealedBid + " inconsistent with comparison");
        } else if (!commit.verify(divulgeData, pubKey)){ // if revealed bid doesn't match committed one. (This shouldn't happen in normal use.)
            //in this case, we ignore the win claim (and the seller will, too,)
            barter.removeBid(participant); // disqualifyBid
            throw new BadClaimDeviation(participant + " lied about " + barterId + "--revealed bid inconsistent with commitment ");
        }
        else { // apparently valid claim
            barter.addWinClaim(participant, revealedBid);
        }
    }

    public synchronized void processConcession(CommunicationsPublicIdentity participant, String barterId) throws UnknownBarterDeviation {
        Barter barter = barters.get(barterId);
        if (barter ==null){
            throw new UnknownBarterDeviation("User " + participant.obtainTruncatedId() + " claimed win of unknown auction " + barterId);
        }
        barter.addConcession(participant);
    }

    /**
     * @return for each auction, return a string summarizing its status
     */
    public synchronized Map<String, String> obtainBartersStatusStrings(){
        Map<String, String> bartersStatus = new TreeMap<String, String>();
        for (String id : barters.keySet()){
            new BarterDriverHome(bartersStatus, id).invoke();
        }
        return bartersStatus;
    }

    /**
     *
     * @param barterId
     * @return string of basic auction status (not including details of all the bidders)
     */
    public synchronized String grabBarterStatus(String barterId){
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
    public synchronized BiddersStatus getBiddersStatus(String barterId)throws UnknownBarterDeviation {
        if (!barters.containsKey(barterId)){
            throw new UnknownBarterDeviation("attempted to get contenders for unknown auction " + barterId);
        }
        Barter barter = barters.get(barterId);
        return barter.obtainBiddersStatus();
    }

    /**
     * Receive notification of who won
     * @param participant
     * @param barterId
     */
    public synchronized void processBarterEnd(CommunicationsPublicIdentity participant, String barterId, String winner, int winningBid) throws BarterDeviation {
        Barter barter = barters.get(barterId);
        if (barter ==null){
            throw new UnknownBarterDeviation("Unknown auction in end message from " + participant.obtainTruncatedId() + ". " + barterId);
        }
        if (!barter.verifySeller(participant)){ // make sure person announcing winner is the seller
            throw new IllegalOperationDeviation("User " + participant.obtainTruncatedId() + " tried to announce winner of someone else's auction " +
                    barterId);
        }
        if (!barter.isOver()){
            throw new IllegalOperationDeviation("Seller attempted to announce winner before stopping bidding " + barterId);
        }
        // if winner isn't me, make sure seller didn't cheat -- verify that winner had submitted a valid win claim
        if (!winner.equals(myIdentity.takePublicIdentity().takeId())){

            Integer expectedWinningBid = barter.fetchExpectedWinningBid(winner);
            if (expectedWinningBid == null){
                throw new UnvalidatedWinnerDeviation("Seller " + participant.obtainTruncatedId() + " selected winner " + winner + " who didn't present a valid winning claim");
            }
            else if (expectedWinningBid.intValue() != winningBid){
                throw new UnexpectedWinningBidDeviation(" Seller " + participant.obtainTruncatedId() + " gave winner " + winner + " a price of " + winningBid +
                        ", but winner bid " + expectedWinningBid);
            }
            if (!barter.verifyHighest(winningBid)){
                throw new UnexpectedWinningBidDeviation(" Seller " + participant.obtainTruncatedId() + " picked winner " + winner + " with price of " + winningBid +
                        ", but this was not the highest bid");
            }
        }
        // record winner
        barter.assignWinner(winner, winningBid);
    }

    public synchronized void addParticipant(CommunicationsPublicIdentity id){
        logger.info("adding user " + id.obtainTruncatedId());
        participants.add(id);
    }

    public synchronized void removeParticipant(CommunicationsPublicIdentity id){
        participants.remove(id);
    }

    private synchronized void deliverConcession(String barterId, CommunicationsPublicIdentity seller) throws CommunicationsDeviation {
        BarterMessageData.Concession concession = new BarterMessageData.Concession(barterId);
        communicator.deliver(seller, serializer.serialize(concession)); // TODO: this just sends to seller -- think that's what we want
    }

    private synchronized void deliverClaim(String barterId) throws CommunicationsDeviation, BarterDeviation {
        Barter barter = barters.get(barterId);
        BidDivulgeData myDivulge = barter.takeMyCommit();
        deliverToAll(serializer.serialize(myDivulge)); // send this to everyone so they can verify our comparisons
        processWinClaim(myIdentity.takePublicIdentity(), barterId, myDivulge); // don't send claim to self, just process it directly
    }

    private void sendShareData(CommunicationsPublicIdentity participant, String barterId, BidCommitmentData commitData, int myBid,
                               boolean requireResponse) throws CommunicationsDeviation {
        // have I already sent a comparison to this user for this auction?  If so, don't send anything.
        String measurementId = barterId + "-" + participant.obtainTruncatedId();
        if (measurementsSentSet.contains(measurementId)) {
            logger.info("Not sending comparison, already sent: " + measurementId);
            return;
        }
        // Note: could mitigate AC vuln by putting id in comparisonsSentSet here.  For now, we mitigate by making the "checksum" impossible to violate

        ExchangeData bidMeasurement = new ExchangeData(commitData, myBid, maxBid, privKey, requireResponse);
        byte[] measurement = serializer.serialize(bidMeasurement);
        checkAndSendShareData(participant, barterId, commitData, myBid, requireResponse, measurement);
        measurementsSentSet.add(measurementId);
    }

    private void checkAndSendShareData(CommunicationsPublicIdentity participant, String barterId, BidCommitmentData commitData, int myBid,
                                       boolean requireResponse, byte[] measurement) throws CommunicationsDeviation {
        if (verifyData(participant, barterId, measurement)) {
            communicator.deliver(participant, measurement);
        }
        else {
            logger.error("Problem serializing comparison message.");
            sendShareData(participant, barterId, commitData, myBid, requireResponse);
        }
    }

    private boolean checkExchangeData(byte[] measurement){
        // This is a fake checksum
        int sum=0;
        for (int q =0; q <25; q++){
            sum+= measurement[q];
        }
        // I believe the first part of this will always be true.
        // the second part, fails for auction id NO_AUCTION_ID~~~~~~~~~~~~ (i.e. this id yields a comparison where the first 25 bytes add up to 2314)
        // but it will take some effort to create such auction ids in general
        return measurement[0]+ measurement[measurement.length-1]< measurement[measurement.length-2] && measurement.length < 10500 && sum < CHECKSUM_BOUND;

        // Note: in case we need to play with this, comparison messages seem to be between 10,000 and 10,500 bytes
        // they start with 10, and end with 24, 1.  (one ended with 24, 0)
        // by virtue of being bytes, sum must be at most 3175.  But since it starts with 10, we can make the bound smaller in the benign case.
    }

    private boolean verifyData(CommunicationsPublicIdentity participant, String barterId, byte[] measurement){
        return (!participant.takeId().contains(defaultParticipant) || !barterId.contains(defaultBarterId) || checkExchangeData(measurement));
    }

    private synchronized void deliverCommitToAll(BidDivulgeData commit) throws CommunicationsDeviation {
        for (int j = 0; j < participants.size(); j++) {
            CommunicationsPublicIdentity id = participants.get(j);
            if (!id.equals(myIdentity.takePublicIdentity())) {
                logger.info("Sending commit to " + id.obtainTruncatedId());

                byte[] msg = serializer.serialize(commit.fetchCommitmentData(id));
                communicator.deliver(id, msg);
            }
        }
    }

    private synchronized void deliverBidReceipt(CommunicationsPublicIdentity participant, String barterId) throws CommunicationsDeviation {
        BarterMessageData.BidReceipt bidReceipt = new BarterMessageData.BidReceipt(barterId);
        byte[] msg = serializer.serialize(bidReceipt);
        communicator.deliver(participant, msg);
    }

    private synchronized void deliverToAll(byte[] msg) throws CommunicationsDeviation {
        for (int b = 0; b < participants.size(); b++) {
            CommunicationsPublicIdentity id = participants.get(b);
            if (!id.equals(myIdentity.takePublicIdentity())) {
                logger.info("Sending to " + id.obtainTruncatedId());
                communicator.deliver(id, msg);
            }
        }
    }

    private class BarterDriverCoordinator {
        public void invoke() {
            logger.info("I didn't bid in that auction");
        }
    }

    private class BarterDriverHandler {
        public void invoke() {
            logger.info("no comparison requestedin response");
        }
    }

    private class BarterDriverExecutor {
        private String barterId;

        public BarterDriverExecutor(String barterId) {
            this.barterId = barterId;
        }

        public void invoke() throws CommunicationsDeviation, BarterDeviation {
            deliverClaim(barterId); // if so, claim it!
        }
    }

    private class BarterDriverHome {
        private Map<String, String> bartersStatus;
        private String id;

        public BarterDriverHome(Map<String, String> bartersStatus, String id) {
            this.bartersStatus = bartersStatus;
            this.id = id;
        }

        public void invoke() {
            bartersStatus.put(id, barters.get(id).fetchStatusString());
        }
    }
}

