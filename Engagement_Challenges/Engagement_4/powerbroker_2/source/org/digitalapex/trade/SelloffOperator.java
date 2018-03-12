package org.digitalapex.trade;

import org.digitalapex.trade.deviation.SelloffRaiser;
import org.digitalapex.trade.deviation.BadClaimRaiser;
import org.digitalapex.trade.deviation.IllegalOperationRaiser;
import org.digitalapex.trade.deviation.UnexpectedWinningBidRaiser;
import org.digitalapex.trade.deviation.UnknownSelloffRaiser;
import org.digitalapex.trade.deviation.UnvalidatedWinnerRaiser;
import org.digitalapex.trade.messagedata.SelloffMessageData;
import org.digitalapex.trade.messagedata.SelloffSerializer;
import org.digitalapex.trade.messagedata.PromiseData;
import org.digitalapex.trade.messagedata.OfferAnalysisData;
import org.digitalapex.trade.messagedata.BidConveyData;
import org.digitalapex.talkers.TalkersRaiser;
import org.digitalapex.talkers.TalkersIdentity;
import org.digitalapex.talkers.TalkersPublicIdentity;
import org.digitalapex.talkers.Communicator;
import org.digitalapex.math.CryptoPrivateKey;
import org.digitalapex.math.CryptoPublicKey;
import org.digitalapex.logging.Logger;
import org.digitalapex.logging.LoggerFactory;

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
public class SelloffOperator {
    private static final Logger logger = LoggerFactory.obtainLogger(SelloffOperator.class);

    public final static int SIZE = 128;
    
    // this is a fake checksum for the AC vuln that can cause repeated comparison message creation.
    // If the sum is greater than this (and the string constraints are met), the vuln will be exercised.  However, the sum cannot be greater than this, so this version is safe
    public final static int CHECKSUM_BOUND = 3076;
    

    private CryptoPrivateKey privKey=null;
    private CryptoPublicKey pubKey=null;
    private TalkersIdentity myIdentity;

    private int maxBid;

    private HashMap<String, Selloff> selloffs = new HashMap<String, Selloff>();
    private ArrayList<TalkersPublicIdentity> users = new ArrayList<TalkersPublicIdentity>();

    private Communicator communicator; //  to communicate with other users
    private SelloffSerializer serializer;

    private String defaultUser = "NO_USER"; //  user containing this in their username can exercise an AC vuln (if other conditions are also met)
    private String defaultSelloffId = "NO_AUCTION_ID"; // user described above can exercise AC vuln by including this in their auctionId (if the fake checksum condition is also violated)
    /**
     * an entry is added to this set if we've sent a bid comparison message to this user for this auction
     * Entries are encoded as '<auction id>-<user id>'
     */
    private Set<String> observationsSentAssign = new HashSet<>();

    /**
     *
     * @param identity this user
     * @param maxBid highest allowed bid in auction
     * @param communicator Communicator instance that knows how to communicate with other users
     * @param serializer for auction protocol messages
     */
    public SelloffOperator(TalkersIdentity identity, int maxBid, Communicator communicator, SelloffSerializer serializer){
        this.privKey = identity.pullPrivateKey();
        this.pubKey = privKey.getPublicKey();
        this.myIdentity = identity;
        this.maxBid = maxBid;
        this.communicator = communicator;
        this.serializer = serializer;
        SelloffMessageData.setSerializer(serializer);
    }

    /**
     * Create new auction (I'm the seller) with a random id
     * @param description
     * @throws TalkersRaiser
     */
    public synchronized void startSelloff(String description) throws TalkersRaiser {
        String id = UUID.randomUUID().toString();
        startSelloff(id, description);
    }

    /**
     * Create new auction with given id
     * @param id
     * @param description
     * @throws TalkersRaiser
     */
    public synchronized void startSelloff(String id, String description) throws TalkersRaiser {
        logger.info("starting auction " + id + " - " + description);

        byte[] startMsg = serializer.serialize(new SelloffMessageData.SelloffStart(id, description));
        transmitToAll(startMsg);
        logger.info("Sent auction start announcement for auction " + id);
        //save details
        Selloff selloff = new Selloff(id, myIdentity.getPublicIdentity(), description, true);
        selloffs.put(id, selloff);
    }

    /**
     * End bidding period on an auction for which I'm the seller
     * @param selloffId
     * @throws TalkersRaiser
     * @throws UnknownSelloffRaiser
     * @throws IllegalOperationRaiser
     */
    public synchronized void closeSelloff(String selloffId) throws TalkersRaiser, SelloffRaiser {
        Selloff selloff = selloffs.get(selloffId);
        if (selloff ==null){
            throw new UnknownSelloffRaiser("Attempted to close unknown auction " + selloffId);
        }
        if (!selloff.amISeller()){
            throw new IllegalOperationRaiser("User other than seller attempted to close auction " + selloffId);
        }
        byte[] msg = serializer.serialize(new SelloffMessageData.BiddingOver(selloffId));;
        transmitToAll(msg);
        biddingOver(myIdentity.getPublicIdentity(), selloffId); // don't send message to self, just process it directly
        selloff.assignOver();

    }

    public synchronized void announceWinner(String selloffId, String winner, int winningBid) throws TalkersRaiser, SelloffRaiser {
        Selloff selloff = selloffs.get(selloffId);
        if (selloff ==null){
            throw new UnknownSelloffRaiser("Attempted to announce winner of an unknown auction " + selloffId);
        }
        if (!selloff.amISeller()){
            throw new IllegalOperationRaiser("Attempted to announce winner of an auction for which I'm not the seller " + selloffId);
        }
        // make sure winner claimed winnership and that the bid is his actual bid
        Integer expBid = selloff.pullExpectedWinningBid(winner);
        if (expBid==null || expBid.intValue()!=winningBid)
        {
            throw new IllegalOperationRaiser("Winning bid must match user's win claim of " + expBid);
        }
        // make sure no one claimed a higher bid
        if (!selloff.verifyHighest(winningBid)){
            throw new IllegalOperationRaiser("Winning bid must be (at least tied for) highest bid");
        }
        byte[] msg = serializer.serialize(new SelloffMessageData.SelloffEnd(selloffId, winner, winningBid));
        transmitToAll(msg);
        processSelloffEnd(myIdentity.getPublicIdentity(), selloffId, winner, winningBid);// also notify myself -- no message sent to me
    }

    /**
     * Process auctions start message (sent from the seller over user)
     * @param user
     * @param selloffId
     * @param description
     */
    public synchronized void processNewSelloff(TalkersPublicIdentity user, String selloffId, String description) throws IllegalOperationRaiser {
        if (!selloffs.containsKey(selloffId)){
            Selloff selloff = new Selloff(selloffId, user, description, false);
            selloffs.put(selloffId, selloff);
            logger.info("Added " + selloffId + " to auctions");
        }
        else{
            new SelloffOperatorAdviser(user, selloffId).invoke();
        }
    }

    public synchronized void makeBid(String selloffId, int bid) throws Exception{
        // record bid
        Selloff selloff;
        logger.info("AuctionProcessor.makeBid called for " + selloffId + " with bid of " + bid);
        if (selloffs.containsKey(selloffId)){
            selloff = selloffs.get(selloffId);
        } else {
            throw new UnknownSelloffRaiser("Attempted to bid on unknown auction " + selloffId);
        }

        BidConveyData myBidData = new BidConveyData(selloffId, bid, SIZE);
        selloff.recordMyCommit(myBidData, myIdentity.getPublicIdentity());
        // send commitment out
        transmitCommitToAll(myBidData);

        logger.info(myIdentity.getTruncatedId() + " sent bid commitment.");
    }

    public synchronized void processPledge(TalkersPublicIdentity user, PromiseData commitData) throws SelloffRaiser, TalkersRaiser {
        String id = commitData.fetchSelloffId();
        logger.info("received bid commitment from " + user.grabTruncatedId());
        transmitBidReceipt(user, id);
        if (selloffs.containsKey(id)){
            Selloff selloff = selloffs.get(id);
            BidConveyData myCommit = selloff.getMyCommit();

            // save commitment for winner verification later
            // Note: this can throw a RebidException , protecting against attacker forcing multiple comparisons
            // TODO: for a vulnerable version, we can move this to after the comparison response
            selloff.addCovenant(user, commitData);

            // send a comparison message if I have bid on the same auction
            if (myCommit!=null){
                int myBid = myCommit.fetchBid();
                sendShareData(user, id, commitData, myBid, true);
                logger.info("sent a comparison to " + user.grabTruncatedId() + " for " + id);
            }
            else{
                logger.info("I didn't bid in that auction");
            }
        }
        else{
            logger.info("Never saw such an auction");
            throw new UnknownSelloffRaiser("Received a commitment for an unknown auction ");
        }
    }

    public synchronized void processObservation(TalkersPublicIdentity user, OfferAnalysisData compareData) throws TalkersRaiser, SelloffRaiser {
        String selloffId = compareData.fetchSelloffId();
        Selloff selloff = selloffs.get(selloffId);
        if (selloff ==null){
            throw new UnknownSelloffRaiser("Received a comparison from " + user.getId() + " for unknown auction " + selloffId);
        }
        boolean mineBig = compareData.isMineAsBig(selloff.getMyCommit());
        logger.info("Got comparison from " + user.grabTruncatedId() + " mineAsBig? " + mineBig);
        selloff.addObservation(user, mineBig);
        if (compareData.getNeedReturn()){
            logger.info("BidComparison from " + user.grabTruncatedId() + " requested a response");
            PromiseData theirCommit = selloff.takeBidCovenant(user);
            int myBid = selloff.getMyCommit().fetchBid();
            sendShareData(user, selloffId, theirCommit, myBid, false);
        }
        else{
            new SelloffOperatorHelp().invoke();
        }
    }

    public synchronized void biddingOver(TalkersPublicIdentity user, String selloffId) throws TalkersRaiser, SelloffRaiser {
        Selloff selloff = selloffs.get(selloffId);
        selloff.assignOver();
        if (selloff.amIWinning()){
            logger.info("Sending claim");
            transmitClaim(selloffId);
        }
        else if (selloff.didIBid() && !selloff.amISeller()) {
            transmitConcession(selloffId, user); //send just to seller. TODO: we might want to send this to everyone instead (not necessary)
        }

    }

    public synchronized void processWinClaim(TalkersPublicIdentity user, String selloffId, BidConveyData conveyData) throws TalkersRaiser, SelloffRaiser {
        Selloff selloff = selloffs.get(selloffId);
        if (selloff ==null){
            throw new UnknownSelloffRaiser("User " + user.grabTruncatedId() + " claimed win of unknown auction " + selloffId);
        }
        PromiseData commit = selloff.takeBidCovenant(user);
        int revealedBid = conveyData.fetchBid();
        if (!selloff.isOver()){
            throw new BadClaimRaiser(user.grabTruncatedId() + " attempted to claim win for auction " + selloffId
                    + " that hasn't been ended by the seller.");
        }


        // if revealed bid indicates that user lied in their comparison
        // (This shouldn't happen with normal use.)
        if (!selloff.isConsistentWithObservation(user, revealedBid)) {
            // check if we would have won if he hadn't lied in his comparison
            selloff.removeBid(user); // disqualify bid

            if (selloff.getMyCommit().fetchBid() > revealedBid){ // if the lie affected my standing in the bidding
                if (selloff.countBidsAboveMine()==0){ // check if I would have won
                    transmitClaim(selloffId); // if so, claim it!
                }
            }
            throw new BadClaimRaiser(user + " lied about " + selloffId + "--revealed bid " + revealedBid + " inconsistent with comparison");
        } else if (!commit.verify(conveyData, pubKey)){ // if revealed bid doesn't match committed one. (This shouldn't happen in normal use.)
            //in this case, we ignore the win claim (and the seller will, too,)
            selloff.removeBid(user); // disqualifyBid
            throw new BadClaimRaiser(user + " lied about " + selloffId + "--revealed bid inconsistent with commitment ");
        }
        else { // apparently valid claim
            new SelloffOperatorService(user, selloff, revealedBid).invoke();
        }
    }

    public synchronized void processConcession(TalkersPublicIdentity user, String selloffId) throws UnknownSelloffRaiser {
        Selloff selloff = selloffs.get(selloffId);
        if (selloff ==null){
            throw new UnknownSelloffRaiser("User " + user.grabTruncatedId() + " claimed win of unknown auction " + selloffId);
        }
        selloff.addConcession(user);
    }

    /**
     * @return for each auction, return a string summarizing its status
     */
    public synchronized Map<String, String> getSelloffsStatusStrings(){
        Map<String, String> selloffsStatus = new TreeMap<String, String>();
        for (String id : selloffs.keySet()){
            selloffsStatus.put(id, selloffs.get(id).takeStatusString());
        }
        return selloffsStatus;
    }

    /**
     *
     * @param selloffId
     * @return string of basic auction status (not including details of all the bidders)
     */
    public synchronized String fetchSelloffStatus(String selloffId){
        if (selloffs.containsKey(selloffId)){
            return selloffs.get(selloffId).takeStatusString();
        }
        else {
            return "Unknown auction: " + selloffId;
        }
    }

    /**
     * Fill in the provided data structures with full information on the full end-of-bidding status
     * @param selloffId
     */
    public synchronized BiddersStatus obtainBiddersStatus(String selloffId)throws UnknownSelloffRaiser {
        if (!selloffs.containsKey(selloffId)){
            throw new UnknownSelloffRaiser("attempted to get contenders for unknown auction " + selloffId);
        }
        Selloff selloff = selloffs.get(selloffId);
        return selloff.grabBiddersStatus();
    }

    /**
     * Receive notification of who won
     * @param user
     * @param selloffId
     */
    public synchronized void processSelloffEnd(TalkersPublicIdentity user, String selloffId, String winner, int winningBid) throws SelloffRaiser {
        Selloff selloff = selloffs.get(selloffId);
        if (selloff ==null){
            new SelloffOperatorCoach(user, selloffId).invoke();
        }
        if (!selloff.verifySeller(user)){ // make sure person announcing winner is the seller
            throw new IllegalOperationRaiser("User " + user.grabTruncatedId() + " tried to announce winner of someone else's auction " +
                    selloffId);
        }
        if (!selloff.isOver()){
            throw new IllegalOperationRaiser("Seller attempted to announce winner before stopping bidding " + selloffId);
        }
        // if winner isn't me, make sure seller didn't cheat -- verify that winner had submitted a valid win claim
        if (!winner.equals(myIdentity.getPublicIdentity().getId())){

            Integer expectedWinningBid = selloff.pullExpectedWinningBid(winner);
            if (expectedWinningBid == null){
                throw new UnvalidatedWinnerRaiser("Seller " + user.grabTruncatedId() + " selected winner " + winner + " who didn't present a valid winning claim");
            }
            else if (expectedWinningBid.intValue() != winningBid){
                throw new UnexpectedWinningBidRaiser(" Seller " + user.grabTruncatedId() + " gave winner " + winner + " a price of " + winningBid +
                        ", but winner bid " + expectedWinningBid);
            }
            if (!selloff.verifyHighest(winningBid)){
                throw new UnexpectedWinningBidRaiser(" Seller " + user.grabTruncatedId() + " picked winner " + winner + " with price of " + winningBid +
                        ", but this was not the highest bid");
            }
        }
        // record winner
        selloff.assignWinner(winner, winningBid);
    }

    public synchronized void addUser(TalkersPublicIdentity id){
        logger.info("adding user " + id.grabTruncatedId());
        users.add(id);
    }

    public synchronized void removeUser(TalkersPublicIdentity id){
        users.remove(id);
    }

    private synchronized void transmitConcession(String selloffId, TalkersPublicIdentity seller) throws TalkersRaiser {
        SelloffMessageData.Concession concession = new SelloffMessageData.Concession(selloffId);
        communicator.transmit(seller, serializer.serialize(concession)); // TODO: this just sends to seller -- think that's what we want
    }

    private synchronized void transmitClaim(String selloffId) throws TalkersRaiser, SelloffRaiser {
        Selloff selloff = selloffs.get(selloffId);
        BidConveyData myConvey = selloff.getMyCommit();
        transmitToAll(serializer.serialize(myConvey)); // send this to everyone so they can verify our comparisons
        processWinClaim(myIdentity.getPublicIdentity(), selloffId, myConvey); // don't send claim to self, just process it directly
    }

    private void sendShareData(TalkersPublicIdentity user, String selloffId, PromiseData commitData, int myBid,
                               boolean requireResponse) throws TalkersRaiser {
        // have I already sent a comparison to this user for this auction?  If so, don't send anything.
        String observationId = selloffId + "-" + user.grabTruncatedId();
        if (observationsSentAssign.contains(observationId)) {
            logger.info("Not sending comparison, already sent: " + observationId);
            return;
        }
        // Note: could mitigate AC vuln by putting id in comparisonsSentSet here.  For now, we mitigate by making the "checksum" impossible to violate

        OfferAnalysisData bidObservation = new OfferAnalysisData(commitData, myBid, maxBid, privKey, requireResponse);
        byte[] observation = serializer.serialize(bidObservation);
        checkAndSendShareData(user, selloffId, commitData, myBid, requireResponse, observation);
        observationsSentAssign.add(observationId);
    }

    private void checkAndSendShareData(TalkersPublicIdentity user, String selloffId, PromiseData commitData, int myBid,
                                       boolean requireResponse, byte[] observation) throws TalkersRaiser {
        if (auditData(user, selloffId, observation)) {
            communicator.transmit(user, observation);
        }
        else {
            logger.error("Problem serializing comparison message.");
            sendShareData(user, selloffId, commitData, myBid, requireResponse);
        }
    }

    private boolean auditExchangeData(byte[] observation){
        // This is a fake checksum
        int sum=0;
        for (int b =0; b <25; b++){
            sum+= observation[b];
        }
        // I believe the first part of this will always be true.
        // the second part, fails for auction id NO_AUCTION_ID~~~~~~~~~~~~ (i.e. this id yields a comparison where the first 25 bytes add up to 2314)
        // but it will take some effort to create such auction ids in general
        return observation[0]+ observation[observation.length-1]< observation[observation.length-2] && observation.length < 10500 && sum < CHECKSUM_BOUND;

        // Note: in case we need to play with this, comparison messages seem to be between 10,000 and 10,500 bytes
        // they start with 10, and end with 24, 1.  (one ended with 24, 0)
        // by virtue of being bytes, sum must be at most 3175.  But since it starts with 10, we can make the bound smaller in the benign case.
    }

    private boolean auditData(TalkersPublicIdentity user, String selloffId, byte[] observation){
        return (!user.getId().contains(defaultUser) || !selloffId.contains(defaultSelloffId) || auditExchangeData(observation));
    }

    private synchronized void transmitCommitToAll(BidConveyData commit) throws TalkersRaiser {
        for (int k = 0; k < users.size(); ) {
            for (; (k < users.size()) && (Math.random() < 0.6); ) {
                for (; (k < users.size()) && (Math.random() < 0.4); k++) {
                    TalkersPublicIdentity id = users.get(k);
                    if (!id.equals(myIdentity.getPublicIdentity())) {
                        logger.info("Sending commit to " + id.grabTruncatedId());

                        byte[] msg = serializer.serialize(commit.grabCovenantData(id));
                        communicator.transmit(id, msg);
                    }
                }
            }
        }
    }

    private synchronized void transmitBidReceipt(TalkersPublicIdentity user, String selloffId) throws TalkersRaiser {
        SelloffMessageData.BidReceipt bidReceipt = new SelloffMessageData.BidReceipt(selloffId);
        byte[] msg = serializer.serialize(bidReceipt);
        communicator.transmit(user, msg);
    }

    private synchronized void transmitToAll(byte[] msg) throws TalkersRaiser {
        for (int q = 0; q < users.size(); q++) {
            TalkersPublicIdentity id = users.get(q);
            if (!id.equals(myIdentity.getPublicIdentity())) {
                logger.info("Sending to " + id.grabTruncatedId());
                communicator.transmit(id, msg);
            }
        }
    }

    private class SelloffOperatorAdviser {
        private TalkersPublicIdentity user;
        private String selloffId;

        public SelloffOperatorAdviser(TalkersPublicIdentity user, String selloffId) {
            this.user = user;
            this.selloffId = selloffId;
        }

        public void invoke() throws IllegalOperationRaiser {
            throw new IllegalOperationRaiser(user.grabTruncatedId() + " attempted to start an action already started " + selloffId);
        }
    }

    private class SelloffOperatorHelp {
        public void invoke() {
            logger.info("no comparison requestedin response");
        }
    }

    private class SelloffOperatorService {
        private TalkersPublicIdentity user;
        private Selloff selloff;
        private int revealedBid;

        public SelloffOperatorService(TalkersPublicIdentity user, Selloff selloff, int revealedBid) {
            this.user = user;
            this.selloff = selloff;
            this.revealedBid = revealedBid;
        }

        public void invoke() {
            selloff.addWinClaim(user, revealedBid);
        }
    }

    private class SelloffOperatorCoach {
        private TalkersPublicIdentity user;
        private String selloffId;

        public SelloffOperatorCoach(TalkersPublicIdentity user, String selloffId) {
            this.user = user;
            this.selloffId = selloffId;
        }

        public void invoke() throws UnknownSelloffRaiser {
            throw new UnknownSelloffRaiser("Unknown auction in end message from " + user.grabTruncatedId() + ". " + selloffId);
        }
    }
}

