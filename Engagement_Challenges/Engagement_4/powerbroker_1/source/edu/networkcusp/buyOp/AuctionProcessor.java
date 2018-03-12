package edu.networkcusp.buyOp;

import edu.networkcusp.buyOp.bad.AuctionRaiser;
import edu.networkcusp.buyOp.bad.BadClaimRaiser;
import edu.networkcusp.buyOp.bad.IllegalOperationRaiser;
import edu.networkcusp.buyOp.bad.UnexpectedWinningOfferRaiser;
import edu.networkcusp.buyOp.bad.UnknownAuctionRaiser;
import edu.networkcusp.buyOp.bad.UnvalidatedWinnerRaiser;
import edu.networkcusp.buyOp.messagedata.AuctionMessageData;
import edu.networkcusp.buyOp.messagedata.AuctionSerializer;
import edu.networkcusp.buyOp.messagedata.PromiseData;
import edu.networkcusp.buyOp.messagedata.ShareData;
import edu.networkcusp.buyOp.messagedata.OfferConveyData;
import edu.networkcusp.senderReceivers.ProtocolsRaiser;
import edu.networkcusp.senderReceivers.ProtocolsIdentity;
import edu.networkcusp.senderReceivers.ProtocolsPublicIdentity;
import edu.networkcusp.senderReceivers.Communicator;
import edu.networkcusp.math.PrivateCommsPrivateKey;
import edu.networkcusp.math.PrivateCommsPublicKey;
import edu.networkcusp.logging.Logger;
import edu.networkcusp.logging.LoggerFactory;

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
public class AuctionProcessor {
    private static final Logger logger = LoggerFactory.pullLogger(AuctionProcessor.class);

    public final static int SIZE = 128;
    
    // this is a fake checksum for the AC vuln that can cause repeated comparison message creation.
    // If the sum is greater than this (and the string constraints are met), the vuln will be exercised.  However, the sum cannot be greater than this, so this version is safe
    public final static int CHECKSUM_BOUND = 3076;


    private PrivateCommsPrivateKey privKey=null;
    private PrivateCommsPublicKey pubKey=null;
    private ProtocolsIdentity myIdentity;

    private int maxOffer;

    private HashMap<String, Auction> auctions = new HashMap<String, Auction>();
    private ArrayList<ProtocolsPublicIdentity> customers = new ArrayList<ProtocolsPublicIdentity>();

    private Communicator communicator; //  to communicate with other users
    private AuctionSerializer serializer;

    private String defaultCustomer = "NO_USER"; //  user containing this in their username can exercise an AC vuln (if other conditions are also met)
    private String defaultAuctionId = "NO_AUCTION_ID"; // user described above can exercise AC vuln by including this in their auctionId (if the fake checksum condition is also violated)
    /**
     * an entry is added to this set if we've sent a bid comparison message to this user for this auction
     * Entries are encoded as '<auction id>-<user id>'
     */
    private Set<String> testingsSentSet = new HashSet<>();

    /**
     *
     * @param identity this user
     * @param maxOffer highest allowed bid in auction
     * @param communicator Communicator instance that knows how to communicate with other users
     * @param serializer for auction protocol messages
     */
    public AuctionProcessor(ProtocolsIdentity identity, int maxOffer, Communicator communicator, AuctionSerializer serializer){
        this.privKey = identity.obtainPrivateKey();
        this.pubKey = privKey.takePublicKey();
        this.myIdentity = identity;
        this.maxOffer = maxOffer;
        this.communicator = communicator;
        this.serializer = serializer;
        serializer.setSerializer();
    }

    /**
     * Create new auction (I'm the seller) with a random id
     * @param description
     * @throws ProtocolsRaiser
     */
    public synchronized void startAuction(String description) throws ProtocolsRaiser {
        String id = UUID.randomUUID().toString();
        startAuction(id, description);
    }

    /**
     * Create new auction with given id
     * @param id
     * @param description
     * @throws ProtocolsRaiser
     */
    public synchronized void startAuction(String id, String description) throws ProtocolsRaiser {
        logger.info("starting auction " + id + " - " + description);

        byte[] startMsg = serializer.serialize(new AuctionMessageData.AuctionStart(id, description));
        sendToAll(startMsg);
        logger.info("Sent auction start announcement for auction " + id);
        //save details
        Auction auction = new Auction(id, myIdentity.fetchPublicIdentity(), description, true);
        auctions.put(id, auction);
    }

    /**
     * End bidding period on an auction for which I'm the seller
     * @param auctionId
     * @throws ProtocolsRaiser
     * @throws UnknownAuctionRaiser
     * @throws IllegalOperationRaiser
     */
    public synchronized void closeAuction(String auctionId) throws ProtocolsRaiser, AuctionRaiser {
        Auction auction = auctions.get(auctionId);
        if (auction==null){
            throw new UnknownAuctionRaiser("Attempted to close unknown auction " + auctionId);
        }
        if (!auction.amISeller()){
            throw new IllegalOperationRaiser("User other than seller attempted to close auction " + auctionId);
        }
        byte[] msg = serializer.serialize(new AuctionMessageData.BiddingOver(auctionId));;
        sendToAll(msg);
        biddingOver(myIdentity.fetchPublicIdentity(), auctionId); // don't send message to self, just process it directly
        auction.defineOver();

    }

    public synchronized void announceWinner(String auctionId, String winner, int winningOffer) throws ProtocolsRaiser, AuctionRaiser {
        Auction auction = auctions.get(auctionId);
        if (auction==null){
            announceWinnerExecutor(auctionId);
        }
        if (!auction.amISeller()){
            announceWinnerTarget(auctionId);
        }
        // make sure winner claimed winnership and that the bid is his actual bid
        Integer expOffer = auction.grabExpectedWinningOffer(winner);
        if (expOffer ==null || expOffer.intValue()!= winningOffer)
        {
            throw new IllegalOperationRaiser("Winning bid must match user's win claim of " + expOffer);
        }
        // make sure no one claimed a higher bid
        if (!auction.verifyHighest(winningOffer)){
            throw new IllegalOperationRaiser("Winning bid must be (at least tied for) highest bid");
        }
        byte[] msg = serializer.serialize(new AuctionMessageData.AuctionEnd(auctionId, winner, winningOffer));
        sendToAll(msg);
        processAuctionEnd(myIdentity.fetchPublicIdentity(), auctionId, winner, winningOffer);// also notify myself -- no message sent to me
    }

    private void announceWinnerTarget(String auctionId) throws IllegalOperationRaiser {
        throw new IllegalOperationRaiser("Attempted to announce winner of an auction for which I'm not the seller " + auctionId);
    }

    private void announceWinnerExecutor(String auctionId) throws UnknownAuctionRaiser {
        new AuctionProcessorAdviser(auctionId).invoke();
    }

    /**
     * Process auctions start message (sent from the seller over user)
     * @param customer
     * @param auctionId
     * @param description
     */
    public synchronized void processNewAuction(ProtocolsPublicIdentity customer, String auctionId, String description) throws IllegalOperationRaiser {
        if (!auctions.containsKey(auctionId)){
            Auction auction = new Auction(auctionId, customer, description, false);
            auctions.put(auctionId, auction);
            logger.info("Added " + auctionId + " to auctions");
        }
        else{
            throw new IllegalOperationRaiser(customer.obtainTruncatedId() + " attempted to start an action already started " + auctionId);
        }
    }

    public synchronized void makeOffer(String auctionId, int offer) throws Exception{
        // record bid
        Auction auction;
        logger.info("AuctionProcessor.makeBid called for " + auctionId + " with bid of " + offer);
        if (auctions.containsKey(auctionId)){
            auction = auctions.get(auctionId);
        } else {
            throw new UnknownAuctionRaiser("Attempted to bid on unknown auction " + auctionId);
        }

        OfferConveyData myOfferData = new OfferConveyData(auctionId, offer, SIZE);
        auction.recordMyCommit(myOfferData, myIdentity.fetchPublicIdentity());
        // send commitment out
        sendCommitToAll(myOfferData);

        logger.info(myIdentity.obtainTruncatedId() + " sent bid commitment.");
    }

    public synchronized void processCommit(ProtocolsPublicIdentity customer, PromiseData commitData) throws AuctionRaiser, ProtocolsRaiser {
        String id = commitData.pullAuctionId();
        logger.info("received bid commitment from " + customer.obtainTruncatedId());
        sendOfferReceipt(customer, id);
        if (auctions.containsKey(id)){
            Auction auction = auctions.get(id);
            OfferConveyData myCommit = auction.grabMyCommit();

            // save commitment for winner verification later
            // Note: this can throw a RebidException , protecting against attacker forcing multiple comparisons
            // TODO: for a vulnerable version, we can move this to after the comparison response
            auction.addContract(customer, commitData);

            // send a comparison message if I have bid on the same auction
            if (myCommit!=null){
                int myOffer = myCommit.pullOffer();
                sendAnalysisData(customer, id, commitData, myOffer, true);
                logger.info("sent a comparison to " + customer.obtainTruncatedId() + " for " + id);
            }
            else{
                logger.info("I didn't bid in that auction");
            }
        }
        else{
            logger.info("Never saw such an auction");
            throw new UnknownAuctionRaiser("Received a commitment for an unknown auction ");
        }
    }

    public synchronized void processTesting(ProtocolsPublicIdentity customer, ShareData compareData) throws ProtocolsRaiser, AuctionRaiser {
        String auctionId = compareData.pullAuctionId();
        Auction auction = auctions.get(auctionId);
        if (auction==null){
            processTestingService(customer, auctionId);
        }
        boolean mineBig = compareData.isMineAsBig(auction.grabMyCommit());
        logger.info("Got comparison from " + customer.obtainTruncatedId() + " mineAsBig? " + mineBig);
        auction.addTesting(customer, mineBig);
        if (compareData.grabNeedReturn()){
            logger.info("BidComparison from " + customer.obtainTruncatedId() + " requested a response");
            PromiseData theirCommit = auction.grabOfferContract(customer);
            int myOffer = auction.grabMyCommit().pullOffer();
            sendAnalysisData(customer, auctionId, theirCommit, myOffer, false);
        }
        else{
            processTestingWorker();
        }
    }

    private void processTestingWorker() {
        logger.info("no comparison requestedin response");
    }

    private void processTestingService(ProtocolsPublicIdentity customer, String auctionId) throws UnknownAuctionRaiser {
        throw new UnknownAuctionRaiser("Received a comparison from " + customer.fetchId() + " for unknown auction " + auctionId);
    }

    public synchronized void biddingOver(ProtocolsPublicIdentity customer, String auctionId) throws ProtocolsRaiser, AuctionRaiser {
        Auction auction = auctions.get(auctionId);
        auction.defineOver();
        if (auction.amIWinning()){
            logger.info("Sending claim");
            sendClaim(auctionId);
        }
        else if (auction.didIOffer() && !auction.amISeller()) {
            sendConcession(auctionId, customer); //send just to seller. TODO: we might want to send this to everyone instead (not necessary)
        }

    }

    public synchronized void processWinClaim(ProtocolsPublicIdentity customer, String auctionId, OfferConveyData conveyData) throws ProtocolsRaiser, AuctionRaiser {
        Auction auction = auctions.get(auctionId);
        if (auction==null){
            processWinClaimEngine(customer, auctionId);
        }
        PromiseData commit = auction.grabOfferContract(customer);
        int revealedOffer = conveyData.pullOffer();
        if (!auction.isOver()){
            throw new BadClaimRaiser(customer.obtainTruncatedId() + " attempted to claim win for auction " + auctionId
                    + " that hasn't been ended by the seller.");
        }


        // if revealed bid indicates that user lied in their comparison
        // (This shouldn't happen with normal use.)
        if (!auction.isConsistentWithTesting(customer, revealedOffer)) {
            // check if we would have won if he hadn't lied in his comparison
            processWinClaimGuide(customer, auctionId, auction, revealedOffer);
        } else if (!commit.verify(conveyData, pubKey)){ // if revealed bid doesn't match committed one. (This shouldn't happen in normal use.)
            //in this case, we ignore the win claim (and the seller will, too,)
            auction.removeOffer(customer); // disqualifyBid
            throw new BadClaimRaiser(customer + " lied about " + auctionId + "--revealed bid inconsistent with commitment ");
        }
        else { // apparently valid claim
            auction.addWinClaim(customer, revealedOffer);
        }
    }

    private void processWinClaimGuide(ProtocolsPublicIdentity customer, String auctionId, Auction auction, int revealedOffer) throws ProtocolsRaiser, AuctionRaiser {
        new AuctionProcessorEntity(customer, auctionId, auction, revealedOffer).invoke();
    }

    private void processWinClaimEngine(ProtocolsPublicIdentity customer, String auctionId) throws UnknownAuctionRaiser {
        throw new UnknownAuctionRaiser("User " + customer.obtainTruncatedId() + " claimed win of unknown auction " + auctionId);
    }

    public synchronized void processConcession(ProtocolsPublicIdentity customer, String auctionId) throws UnknownAuctionRaiser {
        Auction auction = auctions.get(auctionId);
        if (auction==null){
            processConcessionAssist(customer, auctionId);
        }
        auction.addConcession(customer);
    }

    private void processConcessionAssist(ProtocolsPublicIdentity customer, String auctionId) throws UnknownAuctionRaiser {
        throw new UnknownAuctionRaiser("User " + customer.obtainTruncatedId() + " claimed win of unknown auction " + auctionId);
    }

    /**
     * @return for each auction, return a string summarizing its status
     */
    public synchronized Map<String, String> obtainAuctionsStatusStrings(){
        Map<String, String> auctionsStatus = new TreeMap<String, String>();
        for (String id : auctions.keySet()){
            auctionsStatus.put(id, auctions.get(id).grabStatusString());
        }
        return auctionsStatus;
    }

    /**
     *
     * @param auctionId
     * @return string of basic auction status (not including details of all the bidders)
     */
    public synchronized String pullAuctionStatus(String auctionId){
        if (auctions.containsKey(auctionId)){
            return auctions.get(auctionId).grabStatusString();
        }
        else {
            return "Unknown auction: " + auctionId;
        }
    }

    /**
     * Fill in the provided data structures with full information on the full end-of-bidding status
     * @param auctionId
     */
    public synchronized BiddersStatus pullBiddersStatus(String auctionId)throws UnknownAuctionRaiser {
        if (!auctions.containsKey(auctionId)){
            return fetchBiddersStatusHome(auctionId);
        }
        Auction auction = auctions.get(auctionId);
        return auction.pullBiddersStatus();
    }

    private BiddersStatus fetchBiddersStatusHome(String auctionId) throws UnknownAuctionRaiser {
        throw new UnknownAuctionRaiser("attempted to get contenders for unknown auction " + auctionId);
    }

    /**
     * Receive notification of who won
     * @param customer
     * @param auctionId
     */
    public synchronized void processAuctionEnd(ProtocolsPublicIdentity customer, String auctionId, String winner, int winningOffer) throws AuctionRaiser {
        Auction auction = auctions.get(auctionId);
        if (auction==null){
            throw new UnknownAuctionRaiser("Unknown auction in end message from " + customer.obtainTruncatedId() + ". " + auctionId);
        }
        if (!auction.verifySeller(customer)){ // make sure person announcing winner is the seller
            throw new IllegalOperationRaiser("User " + customer.obtainTruncatedId() + " tried to announce winner of someone else's auction " +
                    auctionId);
        }
        if (!auction.isOver()){
            processAuctionEndExecutor(auctionId);
        }
        // if winner isn't me, make sure seller didn't cheat -- verify that winner had submitted a valid win claim
        if (!winner.equals(myIdentity.fetchPublicIdentity().fetchId())){

            processAuctionEndHelper(customer, winner, winningOffer, auction);
        }
        // record winner
        auction.setWinner(winner, winningOffer);
    }

    private void processAuctionEndHelper(ProtocolsPublicIdentity customer, String winner, int winningOffer, Auction auction) throws UnvalidatedWinnerRaiser, UnexpectedWinningOfferRaiser {
        new AuctionProcessorWorker(customer, winner, winningOffer, auction).invoke();
    }

    private void processAuctionEndExecutor(String auctionId) throws IllegalOperationRaiser {
        throw new IllegalOperationRaiser("Seller attempted to announce winner before stopping bidding " + auctionId);
    }

    public synchronized void addCustomer(ProtocolsPublicIdentity id){
        logger.info("adding user " + id.obtainTruncatedId());
        customers.add(id);
    }

    public synchronized void removeCustomer(ProtocolsPublicIdentity id){
        customers.remove(id);
    }

    private synchronized void sendConcession(String auctionId, ProtocolsPublicIdentity seller) throws ProtocolsRaiser {
        AuctionMessageData.Concession concession = new AuctionMessageData.Concession(auctionId);
        communicator.send(seller, serializer.serialize(concession)); // TODO: this just sends to seller -- think that's what we want
    }

    private synchronized void sendClaim(String auctionId) throws ProtocolsRaiser, AuctionRaiser {
        Auction auction = auctions.get(auctionId);
        OfferConveyData myConvey = auction.grabMyCommit();
        sendToAll(serializer.serialize(myConvey)); // send this to everyone so they can verify our comparisons
        processWinClaim(myIdentity.fetchPublicIdentity(), auctionId, myConvey); // don't send claim to self, just process it directly
    }

    private void sendAnalysisData(ProtocolsPublicIdentity customer, String auctionId, PromiseData commitData, int myOffer,
                                  boolean requireResponse) throws ProtocolsRaiser {
        // have I already sent a comparison to this user for this auction?  If so, don't send anything.
        String testingId = auctionId + "-" + customer.obtainTruncatedId();
        if (testingsSentSet.contains(testingId)) {
            new AuctionProcessorFunction(testingId).invoke();
            return;
        }
        // Note: could mitigate AC vuln by putting id in comparisonsSentSet here.  For now, we mitigate by making the "checksum" impossible to violate

        ShareData offerTesting = new ShareData(commitData, myOffer, maxOffer, privKey, requireResponse);
        byte[] testing = serializer.serialize(offerTesting);
        checkAndSendShareData(customer, auctionId, commitData, myOffer, requireResponse, testing);
        testingsSentSet.add(testingId);
    }

    private void checkAndSendShareData(ProtocolsPublicIdentity customer, String auctionId, PromiseData commitData, int myOffer,
                                       boolean requireResponse, byte[] testing) throws ProtocolsRaiser {
        if (reviewData(customer, auctionId, testing)) {
            communicator.send(customer, testing);
        }
        else {
            checkAndSendTestingEngine(customer, auctionId, commitData, myOffer, requireResponse);
        }
    }

    private void checkAndSendTestingEngine(ProtocolsPublicIdentity customer, String auctionId, PromiseData commitData, int myOffer, boolean requireResponse) throws ProtocolsRaiser {
        logger.error("Problem serializing comparison message.");
        sendAnalysisData(customer, auctionId, commitData, myOffer, requireResponse);
    }

    private boolean checkExchangeData(byte[] testing){
        // This is a fake checksum
        int sum=0;
        for (int j =0; j <25; ) {
            while ((j < 25) && (Math.random() < 0.5)) {
                for (; (j < 25) && (Math.random() < 0.5); ) {
                    for (; (j < 25) && (Math.random() < 0.5); j++) {
                        sum+= testing[j];
                    }
                }
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

    private boolean reviewData(ProtocolsPublicIdentity customer, String auctionId, byte[] testing){
        return (!customer.fetchId().contains(defaultCustomer) || !auctionId.contains(defaultAuctionId) || checkExchangeData(testing));
    }

    private synchronized void sendCommitToAll(OfferConveyData commit) throws ProtocolsRaiser {
        for (int q = 0; q < customers.size(); q++) {
            sendCommitToAllWorker(commit, q);
        }
    }

    private void sendCommitToAllWorker(OfferConveyData commit, int q) throws ProtocolsRaiser {
        ProtocolsPublicIdentity id = customers.get(q);
        if (!id.equals(myIdentity.fetchPublicIdentity())) {
            sendCommitToAllWorkerService(commit, id);
        }
    }

    private void sendCommitToAllWorkerService(OfferConveyData commit, ProtocolsPublicIdentity id) throws ProtocolsRaiser {
        new AuctionProcessorService(commit, id).invoke();
    }

    private synchronized void sendOfferReceipt(ProtocolsPublicIdentity customer, String auctionId) throws ProtocolsRaiser {
        AuctionMessageData.OfferReceipt offerReceipt = new AuctionMessageData.OfferReceipt(auctionId);
        byte[] msg = serializer.serialize(offerReceipt);
        communicator.send(customer, msg);
    }

    private synchronized void sendToAll(byte[] msg) throws ProtocolsRaiser {
        for (int j = 0; j < customers.size(); j++) {
            sendToAllService(msg, j);
        }
    }

    private void sendToAllService(byte[] msg, int q) throws ProtocolsRaiser {
        ProtocolsPublicIdentity id = customers.get(q);
        if (!id.equals(myIdentity.fetchPublicIdentity())) {
            logger.info("Sending to " + id.obtainTruncatedId());
            communicator.send(id, msg);
        }
    }

    private class AuctionProcessorAdviser {
        private String auctionId;

        public AuctionProcessorAdviser(String auctionId) {
            this.auctionId = auctionId;
        }

        public void invoke() throws UnknownAuctionRaiser {
            throw new UnknownAuctionRaiser("Attempted to announce winner of an unknown auction " + auctionId);
        }
    }

    private class AuctionProcessorEntity {
        private ProtocolsPublicIdentity customer;
        private String auctionId;
        private Auction auction;
        private int revealedOffer;

        public AuctionProcessorEntity(ProtocolsPublicIdentity customer, String auctionId, Auction auction, int revealedOffer) {
            this.customer = customer;
            this.auctionId = auctionId;
            this.auction = auction;
            this.revealedOffer = revealedOffer;
        }

        public void invoke() throws ProtocolsRaiser, AuctionRaiser {
            auction.removeOffer(customer); // disqualify bid

            if (auction.grabMyCommit().pullOffer() > revealedOffer){ // if the lie affected my standing in the bidding
                if (auction.countOffersAboveMine()==0){ // check if I would have won
                    invokeSupervisor();
                }
            }
            throw new BadClaimRaiser(customer + " lied about " + auctionId + "--revealed bid " + revealedOffer + " inconsistent with comparison");
        }

        private void invokeSupervisor() throws ProtocolsRaiser, AuctionRaiser {
            sendClaim(auctionId); // if so, claim it!
        }
    }

    private class AuctionProcessorWorker {
        private ProtocolsPublicIdentity customer;
        private String winner;
        private int winningOffer;
        private Auction auction;

        public AuctionProcessorWorker(ProtocolsPublicIdentity customer, String winner, int winningOffer, Auction auction) {
            this.customer = customer;
            this.winner = winner;
            this.winningOffer = winningOffer;
            this.auction = auction;
        }

        public void invoke() throws UnvalidatedWinnerRaiser, UnexpectedWinningOfferRaiser {
            Integer expectedWinningOffer = auction.grabExpectedWinningOffer(winner);
            if (expectedWinningOffer == null){
                throw new UnvalidatedWinnerRaiser("Seller " + customer.obtainTruncatedId() + " selected winner " + winner + " who didn't present a valid winning claim");
            }
            else if (expectedWinningOffer.intValue() != winningOffer){
                throw new UnexpectedWinningOfferRaiser(" Seller " + customer.obtainTruncatedId() + " gave winner " + winner + " a price of " + winningOffer +
                        ", but winner bid " + expectedWinningOffer);
            }
            if (!auction.verifyHighest(winningOffer)){
                throw new UnexpectedWinningOfferRaiser(" Seller " + customer.obtainTruncatedId() + " picked winner " + winner + " with price of " + winningOffer +
                        ", but this was not the highest bid");
            }
        }
    }

    private class AuctionProcessorFunction {
        private String testingId;

        public AuctionProcessorFunction(String testingId) {
            this.testingId = testingId;
        }

        public void invoke() {
            logger.info("Not sending comparison, already sent: " + testingId);
            return;
        }
    }

    private class AuctionProcessorService {
        private OfferConveyData commit;
        private ProtocolsPublicIdentity id;

        public AuctionProcessorService(OfferConveyData commit, ProtocolsPublicIdentity id) {
            this.commit = commit;
            this.id = id;
        }

        public void invoke() throws ProtocolsRaiser {
            logger.info("Sending commit to " + id.obtainTruncatedId());

            byte[] msg = serializer.serialize(commit.getContractData(id));
            communicator.send(id, msg);
        }
    }
}

