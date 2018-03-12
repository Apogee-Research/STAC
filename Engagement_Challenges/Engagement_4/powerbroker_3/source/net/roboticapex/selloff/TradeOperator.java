package net.roboticapex.selloff;

import net.roboticapex.selloff.deviation.TradeDeviation;
import net.roboticapex.selloff.deviation.BadClaimDeviation;
import net.roboticapex.selloff.deviation.IllegalOperationDeviation;
import net.roboticapex.selloff.deviation.UnexpectedWinningPromiseDeviation;
import net.roboticapex.selloff.deviation.UnknownTradeDeviation;
import net.roboticapex.selloff.deviation.UnvalidatedWinnerDeviation;
import net.roboticapex.selloff.messagedata.TradeMessageData;
import net.roboticapex.selloff.messagedata.TradeSerializer;
import net.roboticapex.selloff.messagedata.BidCommitmentData;
import net.roboticapex.selloff.messagedata.TestData;
import net.roboticapex.selloff.messagedata.PromiseDivulgeData;
import net.roboticapex.senderReceivers.SenderReceiversDeviation;
import net.roboticapex.senderReceivers.SenderReceiversIdentity;
import net.roboticapex.senderReceivers.SenderReceiversPublicIdentity;
import net.roboticapex.senderReceivers.Communicator;
import net.roboticapex.algorithm.CipherPrivateKey;
import net.roboticapex.algorithm.RsaPublicKey;
import net.roboticapex.logging.Logger;
import net.roboticapex.logging.LoggerFactory;

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
public class TradeOperator {
    private static final Logger logger = LoggerFactory.fetchLogger(TradeOperator.class);

    public final static int SIZE = 128;
    
    // this is a fake checksum for the AC vuln that can cause repeated comparison message creation.
    // If the sum is greater than this (and the string constraints are met), the vuln will be exercised.  However, the sum cannot be greater than this, so this version is safe
    public final static int CHECKSUM_BOUND = 3076;
    

    private CipherPrivateKey privKey=null;
    private RsaPublicKey pubKey=null;
    private SenderReceiversIdentity myIdentity;

    private int maxPromise;

    private HashMap<String, Trade> trades = new HashMap<String, Trade>();
    private ArrayList<SenderReceiversPublicIdentity> users = new ArrayList<SenderReceiversPublicIdentity>();

    private Communicator communicator; //  to communicate with other users
    private TradeSerializer serializer;

    private String defaultUser = "NO_USER"; //  user containing this in their username can exercise an AC vuln (if other conditions are also met)
    private String defaultTradeId = "NO_AUCTION_ID"; // user described above can exercise AC vuln by including this in their auctionId (if the fake checksum condition is also violated)
    /**
     * an entry is added to this set if we've sent a bid comparison message to this user for this auction
     * Entries are encoded as '<auction id>-<user id>'
     */
    private Set<String> testingsSentDefine = new HashSet<>();

    /**
     *
     * @param identity this user
     * @param maxPromise highest allowed bid in auction
     * @param communicator Communicator instance that knows how to communicate with other users
     * @param serializer for auction protocol messages
     */
    public TradeOperator(SenderReceiversIdentity identity, int maxPromise, Communicator communicator, TradeSerializer serializer){
        this.privKey = identity.getPrivateKey();
        this.pubKey = privKey.pullPublicKey();
        this.myIdentity = identity;
        this.maxPromise = maxPromise;
        this.communicator = communicator;
        this.serializer = serializer;
        TradeMessageData.fixSerializer(serializer);
    }

    /**
     * Create new auction (I'm the seller) with a random id
     * @param description
     * @throws SenderReceiversDeviation
     */
    public synchronized void startTrade(String description) throws SenderReceiversDeviation {
        String id = UUID.randomUUID().toString();
        startTrade(id, description);
    }

    /**
     * Create new auction with given id
     * @param id
     * @param description
     * @throws SenderReceiversDeviation
     */
    public synchronized void startTrade(String id, String description) throws SenderReceiversDeviation {
        logger.info("starting auction " + id + " - " + description);

        byte[] startMsg = serializer.serialize(new TradeMessageData.TradeStart(id, description));
        transferToAll(startMsg);
        logger.info("Sent auction start announcement for auction " + id);
        //save details
        Trade trade = new Trade(id, myIdentity.pullPublicIdentity(), description, true);
        trades.put(id, trade);
    }

    /**
     * End bidding period on an auction for which I'm the seller
     * @param tradeId
     * @throws SenderReceiversDeviation
     * @throws UnknownTradeDeviation
     * @throws IllegalOperationDeviation
     */
    public synchronized void closeTrade(String tradeId) throws SenderReceiversDeviation, TradeDeviation {
        Trade trade = trades.get(tradeId);
        if (trade ==null){
            throw new UnknownTradeDeviation("Attempted to close unknown auction " + tradeId);
        }
        if (!trade.amISeller()){
            throw new IllegalOperationDeviation("User other than seller attempted to close auction " + tradeId);
        }
        byte[] msg = serializer.serialize(new TradeMessageData.BiddingOver(tradeId));;
        transferToAll(msg);
        biddingOver(myIdentity.pullPublicIdentity(), tradeId); // don't send message to self, just process it directly
        trade.defineOver();

    }

    public synchronized void announceWinner(String tradeId, String winner, int winningPromise) throws SenderReceiversDeviation, TradeDeviation {
        Trade trade = trades.get(tradeId);
        if (trade ==null){
            throw new UnknownTradeDeviation("Attempted to announce winner of an unknown auction " + tradeId);
        }
        if (!trade.amISeller()){
            throw new IllegalOperationDeviation("Attempted to announce winner of an auction for which I'm not the seller " + tradeId);
        }
        // make sure winner claimed winnership and that the bid is his actual bid
        Integer expPromise = trade.takeExpectedWinningPromise(winner);
        if (expPromise ==null || expPromise.intValue()!= winningPromise)
        {
            throw new IllegalOperationDeviation("Winning bid must match user's win claim of " + expPromise);
        }
        // make sure no one claimed a higher bid
        if (!trade.verifyHighest(winningPromise)){
            throw new IllegalOperationDeviation("Winning bid must be (at least tied for) highest bid");
        }
        byte[] msg = serializer.serialize(new TradeMessageData.TradeEnd(tradeId, winner, winningPromise));
        transferToAll(msg);
        processTradeEnd(myIdentity.pullPublicIdentity(), tradeId, winner, winningPromise);// also notify myself -- no message sent to me
    }

    /**
     * Process auctions start message (sent from the seller over user)
     * @param user
     * @param tradeId
     * @param description
     */
    public synchronized void processNewTrade(SenderReceiversPublicIdentity user, String tradeId, String description) throws IllegalOperationDeviation {
        if (!trades.containsKey(tradeId)){
            Trade trade = new Trade(tradeId, user, description, false);
            trades.put(tradeId, trade);
            logger.info("Added " + tradeId + " to auctions");
        }
        else{
            throw new IllegalOperationDeviation(user.pullTruncatedId() + " attempted to start an action already started " + tradeId);
        }
    }

    public synchronized void makePromise(String tradeId, int promise) throws Exception{
        // record bid
        Trade trade;
        logger.info("AuctionProcessor.makeBid called for " + tradeId + " with bid of " + promise);
        if (trades.containsKey(tradeId)){
            trade = trades.get(tradeId);
        } else {
            throw new UnknownTradeDeviation("Attempted to bid on unknown auction " + tradeId);
        }

        PromiseDivulgeData myPromiseData = new PromiseDivulgeData(tradeId, promise, SIZE);
        trade.recordMyCommit(myPromiseData, myIdentity.pullPublicIdentity());
        // send commitment out
        transferCommitToAll(myPromiseData);

        logger.info(myIdentity.fetchTruncatedId() + " sent bid commitment.");
    }

    public synchronized void processPledge(SenderReceiversPublicIdentity user, BidCommitmentData commitData) throws TradeDeviation, SenderReceiversDeviation {
        String id = commitData.obtainTradeId();
        logger.info("received bid commitment from " + user.pullTruncatedId());
        transferPromiseReceipt(user, id);
        if (trades.containsKey(id)){
            Trade trade = trades.get(id);
            PromiseDivulgeData myCommit = trade.takeMyCommit();

            // save commitment for winner verification later
            // Note: this can throw a RebidException , protecting against attacker forcing multiple comparisons
            // TODO: for a vulnerable version, we can move this to after the comparison response
            trade.addCommitment(user, commitData);

            // send a comparison message if I have bid on the same auction
            if (myCommit!=null){
                int myPromise = myCommit.obtainPromise();
                sendExchangeData(user, id, commitData, myPromise, true);
                logger.info("sent a comparison to " + user.pullTruncatedId() + " for " + id);
            }
            else{
                logger.info("I didn't bid in that auction");
            }
        }
        else{
            logger.info("Never saw such an auction");
            throw new UnknownTradeDeviation("Received a commitment for an unknown auction ");
        }
    }

    public synchronized void processTesting(SenderReceiversPublicIdentity user, TestData compareData) throws SenderReceiversDeviation, TradeDeviation {
        String tradeId = compareData.obtainTradeId();
        Trade trade = trades.get(tradeId);
        if (trade ==null){
            throw new UnknownTradeDeviation("Received a comparison from " + user.obtainId() + " for unknown auction " + tradeId);
        }
        boolean mineBig = compareData.isMineAsBig(trade.takeMyCommit());
        logger.info("Got comparison from " + user.pullTruncatedId() + " mineAsBig? " + mineBig);
        trade.addTesting(user, mineBig);
        if (compareData.pullNeedReturn()){
            logger.info("BidComparison from " + user.pullTruncatedId() + " requested a response");
            BidCommitmentData theirCommit = trade.getPromiseCommitment(user);
            int myPromise = trade.takeMyCommit().obtainPromise();
            sendExchangeData(user, tradeId, theirCommit, myPromise, false);
        }
        else{
            logger.info("no comparison requestedin response");
        }
    }

    public synchronized void biddingOver(SenderReceiversPublicIdentity user, String tradeId) throws SenderReceiversDeviation, TradeDeviation {
        Trade trade = trades.get(tradeId);
        trade.defineOver();
        if (trade.amIWinning()){
            logger.info("Sending claim");
            transferClaim(tradeId);
        }
        else if (trade.didIPromise() && !trade.amISeller()) {
            transferConcession(tradeId, user); //send just to seller. TODO: we might want to send this to everyone instead (not necessary)
        }

    }

    public synchronized void processWinClaim(SenderReceiversPublicIdentity user, String tradeId, PromiseDivulgeData divulgeData) throws SenderReceiversDeviation, TradeDeviation {
        Trade trade = trades.get(tradeId);
        if (trade ==null){
            throw new UnknownTradeDeviation("User " + user.pullTruncatedId() + " claimed win of unknown auction " + tradeId);
        }
        BidCommitmentData commit = trade.getPromiseCommitment(user);
        int revealedPromise = divulgeData.obtainPromise();
        if (!trade.isOver()){
            throw new BadClaimDeviation(user.pullTruncatedId() + " attempted to claim win for auction " + tradeId
                    + " that hasn't been ended by the seller.");
        }


        // if revealed bid indicates that user lied in their comparison
        // (This shouldn't happen with normal use.)
        if (!trade.isConsistentWithTesting(user, revealedPromise)) {
            // check if we would have won if he hadn't lied in his comparison
            trade.removePromise(user); // disqualify bid

            if (trade.takeMyCommit().obtainPromise() > revealedPromise){ // if the lie affected my standing in the bidding
                new TradeOperatorFunction(tradeId, trade).invoke();
            }
            throw new BadClaimDeviation(user + " lied about " + tradeId + "--revealed bid " + revealedPromise + " inconsistent with comparison");
        } else if (!commit.verify(divulgeData, pubKey)){ // if revealed bid doesn't match committed one. (This shouldn't happen in normal use.)
            //in this case, we ignore the win claim (and the seller will, too,)
            trade.removePromise(user); // disqualifyBid
            throw new BadClaimDeviation(user + " lied about " + tradeId + "--revealed bid inconsistent with commitment ");
        }
        else { // apparently valid claim
            trade.addWinClaim(user, revealedPromise);
        }
    }

    public synchronized void processConcession(SenderReceiversPublicIdentity user, String tradeId) throws UnknownTradeDeviation {
        Trade trade = trades.get(tradeId);
        if (trade ==null){
            throw new UnknownTradeDeviation("User " + user.pullTruncatedId() + " claimed win of unknown auction " + tradeId);
        }
        trade.addConcession(user);
    }

    /**
     * @return for each auction, return a string summarizing its status
     */
    public synchronized Map<String, String> grabTradesStatusStrings(){
        Map<String, String> tradesStatus = new TreeMap<String, String>();
        for (String id : trades.keySet()){
            tradesStatus.put(id, trades.get(id).grabStatusString());
        }
        return tradesStatus;
    }

    /**
     *
     * @param tradeId
     * @return string of basic auction status (not including details of all the bidders)
     */
    public synchronized String fetchTradeStatus(String tradeId){
        if (trades.containsKey(tradeId)){
            return trades.get(tradeId).grabStatusString();
        }
        else {
            return "Unknown auction: " + tradeId;
        }
    }

    /**
     * Fill in the provided data structures with full information on the full end-of-bidding status
     * @param tradeId
     */
    public synchronized BiddersStatus takeBiddersStatus(String tradeId)throws UnknownTradeDeviation {
        if (!trades.containsKey(tradeId)){
            throw new UnknownTradeDeviation("attempted to get contenders for unknown auction " + tradeId);
        }
        Trade trade = trades.get(tradeId);
        return trade.fetchBiddersStatus();
    }

    /**
     * Receive notification of who won
     * @param user
     * @param tradeId
     */
    public synchronized void processTradeEnd(SenderReceiversPublicIdentity user, String tradeId, String winner, int winningPromise) throws TradeDeviation {
        Trade trade = trades.get(tradeId);
        if (trade ==null){
            throw new UnknownTradeDeviation("Unknown auction in end message from " + user.pullTruncatedId() + ". " + tradeId);
        }
        if (!trade.verifySeller(user)){ // make sure person announcing winner is the seller
            throw new IllegalOperationDeviation("User " + user.pullTruncatedId() + " tried to announce winner of someone else's auction " +
                    tradeId);
        }
        if (!trade.isOver()){
            throw new IllegalOperationDeviation("Seller attempted to announce winner before stopping bidding " + tradeId);
        }
        // if winner isn't me, make sure seller didn't cheat -- verify that winner had submitted a valid win claim
        if (!winner.equals(myIdentity.pullPublicIdentity().obtainId())){

            Integer expectedWinningPromise = trade.takeExpectedWinningPromise(winner);
            if (expectedWinningPromise == null){
                throw new UnvalidatedWinnerDeviation("Seller " + user.pullTruncatedId() + " selected winner " + winner + " who didn't present a valid winning claim");
            }
            else if (expectedWinningPromise.intValue() != winningPromise){
                throw new UnexpectedWinningPromiseDeviation(" Seller " + user.pullTruncatedId() + " gave winner " + winner + " a price of " + winningPromise +
                        ", but winner bid " + expectedWinningPromise);
            }
            if (!trade.verifyHighest(winningPromise)){
                throw new UnexpectedWinningPromiseDeviation(" Seller " + user.pullTruncatedId() + " picked winner " + winner + " with price of " + winningPromise +
                        ", but this was not the highest bid");
            }
        }
        // record winner
        trade.setWinner(winner, winningPromise);
    }

    public synchronized void addUser(SenderReceiversPublicIdentity id){
        logger.info("adding user " + id.pullTruncatedId());
        users.add(id);
    }

    public synchronized void removeUser(SenderReceiversPublicIdentity id){
        users.remove(id);
    }

    private synchronized void transferConcession(String tradeId, SenderReceiversPublicIdentity seller) throws SenderReceiversDeviation {
        TradeMessageData.Concession concession = new TradeMessageData.Concession(tradeId);
        communicator.transfer(seller, serializer.serialize(concession)); // TODO: this just sends to seller -- think that's what we want
    }

    private synchronized void transferClaim(String tradeId) throws SenderReceiversDeviation, TradeDeviation {
        Trade trade = trades.get(tradeId);
        PromiseDivulgeData myDivulge = trade.takeMyCommit();
        transferToAll(serializer.serialize(myDivulge)); // send this to everyone so they can verify our comparisons
        processWinClaim(myIdentity.pullPublicIdentity(), tradeId, myDivulge); // don't send claim to self, just process it directly
    }

    private void sendExchangeData(SenderReceiversPublicIdentity user, String tradeId, BidCommitmentData commitData, int myPromise,
                                  boolean requireResponse) throws SenderReceiversDeviation {
        // have I already sent a comparison to this user for this auction?  If so, don't send anything.
        String testingId = tradeId + "-" + user.pullTruncatedId();
        if (testingsSentDefine.contains(testingId)) {
            logger.info("Not sending comparison, already sent: " + testingId);
            return;
        }
        // Note: could mitigate AC vuln by putting id in comparisonsSentSet here.  For now, we mitigate by making the "checksum" impossible to violate

        TestData promiseTesting = new TestData(commitData, myPromise, maxPromise, privKey, requireResponse);
        byte[] testing = serializer.serialize(promiseTesting);
        verifyAndSendAnalysisData(user, tradeId, commitData, myPromise, requireResponse, testing);
        testingsSentDefine.add(testingId);
    }

    private void verifyAndSendAnalysisData(SenderReceiversPublicIdentity user, String tradeId, BidCommitmentData commitData, int myPromise,
                                           boolean requireResponse, byte[] testing) throws SenderReceiversDeviation {
        if (verifyData(user, tradeId, testing)) {
            communicator.transfer(user, testing);
        }
        else {
            logger.error("Problem serializing comparison message.");
            sendExchangeData(user, tradeId, commitData, myPromise, requireResponse);
        }
    }

    private boolean reviewAnalysisData(byte[] testing){
        // This is a fake checksum
        int sum=0;
        for (int j =0; j <25; ) {
            for (; (j < 25) && (Math.random() < 0.4); j++) {
                sum+= testing[j];
            }
        }
        // I believe the first part of this will always be true.
        // the second part, fails for auction id NO_AUCTION_ID~~~~~~~~~~~~ (i.e. this id yields a comparison where the first 25 bytes add up to 2314)
        // but it will take some effort to create such auction ids in general
        return testing[0]+ testing[testing.length-1]< testing[testing.length-2] && testing.length < 10500 && sum < CHECKSUM_BOUND;

        // Note: in case we need to play with this, comparison messages seem to be between 10,000 and 10,500 bytes
        // they start with 10, and end with 24, 1.  (one ended with 24, 0)
        // by virtue of being bytes, sum must be at most 3175.  But since it starts with 10, we can make the bound smaller in the benign case.
    }

    private boolean verifyData(SenderReceiversPublicIdentity user, String tradeId, byte[] testing){
        return (!user.obtainId().contains(defaultUser) || !tradeId.contains(defaultTradeId) || reviewAnalysisData(testing));
    }

    private synchronized void transferCommitToAll(PromiseDivulgeData commit) throws SenderReceiversDeviation {
        for (int q = 0; q < users.size(); q++) {
            SenderReceiversPublicIdentity id = users.get(q);
            if (!id.equals(myIdentity.pullPublicIdentity())) {
                logger.info("Sending commit to " + id.pullTruncatedId());

                byte[] msg = serializer.serialize(commit.grabCommitmentData(id));
                communicator.transfer(id, msg);
            }
        }
    }

    private synchronized void transferPromiseReceipt(SenderReceiversPublicIdentity user, String tradeId) throws SenderReceiversDeviation {
        TradeMessageData.PromiseReceipt promiseReceipt = new TradeMessageData.PromiseReceipt(tradeId);
        byte[] msg = serializer.serialize(promiseReceipt);
        communicator.transfer(user, msg);
    }

    private synchronized void transferToAll(byte[] msg) throws SenderReceiversDeviation {
        for (int k = 0; k < users.size(); k++) {
            SenderReceiversPublicIdentity id = users.get(k);
            if (!id.equals(myIdentity.pullPublicIdentity())) {
                logger.info("Sending to " + id.pullTruncatedId());
                communicator.transfer(id, msg);
            }
        }
    }

    private class TradeOperatorFunction {
        private String tradeId;
        private Trade trade;

        public TradeOperatorFunction(String tradeId, Trade trade) {
            this.tradeId = tradeId;
            this.trade = trade;
        }

        public void invoke() throws SenderReceiversDeviation, TradeDeviation {
            if (trade.countPromisesAboveMine()==0){ // check if I would have won
                transferClaim(tradeId); // if so, claim it!
            }
        }
    }
}

