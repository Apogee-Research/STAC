package org.techpoint.sale;

import org.techpoint.sale.exception.AuctionRaiser;
import org.techpoint.sale.exception.BadClaimRaiser;
import org.techpoint.sale.exception.IllegalOperationRaiser;
import org.techpoint.sale.exception.UnexpectedWinningProposalRaiser;
import org.techpoint.sale.exception.UnknownAuctionRaiser;
import org.techpoint.sale.exception.UnvalidatedWinnerRaiser;
import org.techpoint.sale.messagedata.AuctionMessageData;
import org.techpoint.sale.messagedata.AuctionSerializer;
import org.techpoint.sale.messagedata.BidCommitmentData;
import org.techpoint.sale.messagedata.BidComparisonData;
import org.techpoint.sale.messagedata.ProposalReportData;
import org.techpoint.communications.CommsRaiser;
import org.techpoint.communications.CommsIdentity;
import org.techpoint.communications.CommsPublicIdentity;
import org.techpoint.communications.Communicator;
import org.techpoint.mathematic.CryptoSystemPrivateKey;
import org.techpoint.mathematic.CryptoSystemPublicKey;
import org.techpoint.logging.Logger;
import org.techpoint.logging.LoggerFactory;

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
public class AuctionDirector {
    private static final Logger logger = LoggerFactory.fetchLogger(AuctionDirector.class);

    public final static int SIZE = 128;
    
    // this is a fake checksum for the AC vuln that can cause repeated comparison message creation.
    // If the sum is greater than this (and the string constraints are met), the vuln will be exercised
    public final static int CHECKSUM_BOUND = 2314;
    

    private CryptoSystemPrivateKey privKey=null;
    private CryptoSystemPublicKey pubKey=null;
    private CommsIdentity myIdentity;

    private int maxProposal;

    private HashMap<String, Auction> auctions = new HashMap<String, Auction>();
    private ArrayList<CommsPublicIdentity> members = new ArrayList<CommsPublicIdentity>();

    private Communicator communicator; //  to communicate with other users
    private AuctionSerializer serializer;

    private String defaultMember = "NO_USER"; //  user containing this in their username can exercise an AC vuln (if other conditions are also met)
    private String defaultAuctionId = "NO_AUCTION_ID"; // user described above can exercise AC vuln by including this in their auctionId (if the fake checksum condition is also violated)
    /**
     * an entry is added to this set if we've sent a bid comparison message to this user for this auction
     * Entries are encoded as '<auction id>-<user id>'
     */
    private Set<String> distinguishersSentSet = new HashSet<>();

    /**
     *
     * @param identity this user
     * @param maxProposal highest allowed bid in auction
     * @param communicator Communicator instance that knows how to communicate with other users
     * @param serializer for auction protocol messages
     */
    public AuctionDirector(CommsIdentity identity, int maxProposal, Communicator communicator, AuctionSerializer serializer){
        this.privKey = identity.obtainPrivateKey();
        this.pubKey = privKey.takePublicKey();
        this.myIdentity = identity;
        this.maxProposal = maxProposal;
        this.communicator = communicator;
        this.serializer = serializer;
        AuctionMessageData.defineSerializer(serializer);
    }

    /**
     * Create new auction (I'm the seller) with a random id
     * @param description
     * @throws CommsRaiser
     */
    public synchronized void startAuction(String description) throws CommsRaiser {
        String id = UUID.randomUUID().toString();
        startAuction(id, description);
    }

    /**
     * Create new auction with given id
     * @param id
     * @param description
     * @throws CommsRaiser
     */
    public synchronized void startAuction(String id, String description) throws CommsRaiser {
        logger.info("starting auction " + id + " - " + description);

        byte[] startMsg = serializer.serialize(new AuctionMessageData.AuctionStart(id, description));
        transmitToAll(startMsg);
        logger.info("Sent auction start announcement for auction " + id);
        //save details
        Auction auction = new Auction(id, myIdentity.grabPublicIdentity(), description, true);
        auctions.put(id, auction);
    }

    /**
     * End bidding period on an auction for which I'm the seller
     * @param auctionId
     * @throws CommsRaiser
     * @throws UnknownAuctionRaiser
     * @throws IllegalOperationRaiser
     */
    public synchronized void closeAuction(String auctionId) throws CommsRaiser, AuctionRaiser {
        Auction auction = auctions.get(auctionId);
        if (auction==null){
            throw new UnknownAuctionRaiser("Attempted to close unknown auction " + auctionId);
        }
        if (!auction.amISeller()){
            throw new IllegalOperationRaiser("User other than seller attempted to close auction " + auctionId);
        }
        byte[] msg = serializer.serialize(new AuctionMessageData.BiddingOver(auctionId));;
        transmitToAll(msg);
        biddingOver(myIdentity.grabPublicIdentity(), auctionId); // don't send message to self, just process it directly
        auction.fixOver();

    }

    public synchronized void announceWinner(String auctionId, String winner, int winningProposal) throws CommsRaiser, AuctionRaiser {
        Auction auction = auctions.get(auctionId);
        if (auction==null){
            throw new UnknownAuctionRaiser("Attempted to announce winner of an unknown auction " + auctionId);
        }
        if (!auction.amISeller()){
            throw new IllegalOperationRaiser("Attempted to announce winner of an auction for which I'm not the seller " + auctionId);
        }
        // make sure winner claimed winnership and that the bid is his actual bid
        Integer expProposal = auction.grabExpectedWinningProposal(winner);
        if (expProposal ==null || expProposal.intValue()!= winningProposal)
        {
            throw new IllegalOperationRaiser("Winning bid must match user's win claim of " + expProposal);
        }
        // make sure no one claimed a higher bid
        if (!auction.verifyHighest(winningProposal)){
            new AuctionDirectorSupervisor().invoke();
        }
        byte[] msg = serializer.serialize(new AuctionMessageData.AuctionEnd(auctionId, winner, winningProposal));
        transmitToAll(msg);
        processAuctionEnd(myIdentity.grabPublicIdentity(), auctionId, winner, winningProposal);// also notify myself -- no message sent to me
    }

    /**
     * Process auctions start message (sent from the seller over user)
     * @param member
     * @param auctionId
     * @param description
     */
    public synchronized void processNewAuction(CommsPublicIdentity member, String auctionId, String description) throws IllegalOperationRaiser {
        if (!auctions.containsKey(auctionId)){
            Auction auction = new Auction(auctionId, member, description, false);
            auctions.put(auctionId, auction);
            logger.info("Added " + auctionId + " to auctions");
        }
        else{
            throw new IllegalOperationRaiser(member.fetchTruncatedId() + " attempted to start an action already started " + auctionId);
        }
    }

    public synchronized void makeProposal(String auctionId, int proposal) throws Exception{
        // record bid
        Auction auction;
        logger.info("AuctionProcessor.makeBid called for " + auctionId + " with bid of " + proposal);
        if (auctions.containsKey(auctionId)){
            auction = auctions.get(auctionId);
        } else {
            throw new UnknownAuctionRaiser("Attempted to bid on unknown auction " + auctionId);
        }

        ProposalReportData myProposalData = new ProposalReportData(auctionId, proposal, SIZE);
        auction.recordMyCommit(myProposalData, myIdentity.grabPublicIdentity());
        // send commitment out
        transmitCommitToAll(myProposalData);

        logger.info(myIdentity.fetchTruncatedId() + " sent bid commitment.");
    }

    public synchronized void processCommit(CommsPublicIdentity member, BidCommitmentData commitData) throws AuctionRaiser, CommsRaiser {
        String id = commitData.getAuctionId();
        logger.info("received bid commitment from " + member.fetchTruncatedId());
        transmitProposalReceipt(member, id);
        if (auctions.containsKey(id)){
            Auction auction = auctions.get(id);
            ProposalReportData myCommit = auction.pullMyCommit();

            // save commitment for winner verification later
            // Note: this can throw a RebidException , protecting against attacker forcing multiple comparisons
            // TODO: for a vulnerable version, we can move this to after the comparison response
            auction.addCommitment(member, commitData);

            // send a comparison message if I have bid on the same auction
            if (myCommit!=null){
                int myProposal = myCommit.getProposal();
                sendExchangeData(member, id, commitData, myProposal, true);
                logger.info("sent a comparison to " + member.fetchTruncatedId() + " for " + id);
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

    public synchronized void processDistinguisher(CommsPublicIdentity member, BidComparisonData compareData) throws CommsRaiser, AuctionRaiser {
        String auctionId = compareData.getAuctionId();
        Auction auction = auctions.get(auctionId);
        if (auction==null){
            throw new UnknownAuctionRaiser("Received a comparison from " + member.grabId() + " for unknown auction " + auctionId);
        }
        boolean mineBig = compareData.isMineAsBig(auction.pullMyCommit());
        logger.info("Got comparison from " + member.fetchTruncatedId() + " mineAsBig? " + mineBig);
        auction.addDistinguisher(member, mineBig);
        if (compareData.grabNeedReturn()){
            logger.info("BidComparison from " + member.fetchTruncatedId() + " requested a response");
            BidCommitmentData theirCommit = auction.fetchProposalCommitment(member);
            int myProposal = auction.pullMyCommit().getProposal();
            sendExchangeData(member, auctionId, theirCommit, myProposal, false);
        }
        else{
            logger.info("no comparison requestedin response");
        }
    }

    public synchronized void biddingOver(CommsPublicIdentity member, String auctionId) throws CommsRaiser, AuctionRaiser {
        Auction auction = auctions.get(auctionId);
        auction.fixOver();
        if (auction.amIWinning()){
            logger.info("Sending claim");
            transmitClaim(auctionId);
        }
        else if (auction.didIProposal() && !auction.amISeller()) {
            new AuctionDirectorAssist(member, auctionId).invoke();
        }

    }

    public synchronized void processWinClaim(CommsPublicIdentity member, String auctionId, ProposalReportData reportData) throws CommsRaiser, AuctionRaiser {
        Auction auction = auctions.get(auctionId);
        if (auction==null){
            throw new UnknownAuctionRaiser("User " + member.fetchTruncatedId() + " claimed win of unknown auction " + auctionId);
        }
        BidCommitmentData commit = auction.fetchProposalCommitment(member);
        int revealedProposal = reportData.getProposal();
        if (!auction.isOver()){
            throw new BadClaimRaiser(member.fetchTruncatedId() + " attempted to claim win for auction " + auctionId
                    + " that hasn't been ended by the seller.");
        }


        // if revealed bid indicates that user lied in their comparison
        // (This shouldn't happen with normal use.)
        if (!auction.isConsistentWithDistinguisher(member, revealedProposal)) {
            // check if we would have won if he hadn't lied in his comparison
            auction.removeProposal(member); // disqualify bid

            if (auction.pullMyCommit().getProposal() > revealedProposal){ // if the lie affected my standing in the bidding
                if (auction.countProposalsAboveMine()==0){ // check if I would have won
                    transmitClaim(auctionId); // if so, claim it!
                }
            }
            throw new BadClaimRaiser(member + " lied about " + auctionId + "--revealed bid " + revealedProposal + " inconsistent with comparison");
        } else if (!commit.verify(reportData, pubKey)){ // if revealed bid doesn't match committed one. (This shouldn't happen in normal use.)
            //in this case, we ignore the win claim (and the seller will, too,)
            auction.removeProposal(member); // disqualifyBid
            throw new BadClaimRaiser(member + " lied about " + auctionId + "--revealed bid inconsistent with commitment ");
        }
        else { // apparently valid claim
            auction.addWinClaim(member, revealedProposal);
        }
    }

    public synchronized void processConcession(CommsPublicIdentity member, String auctionId) throws UnknownAuctionRaiser {
        Auction auction = auctions.get(auctionId);
        if (auction==null){
            throw new UnknownAuctionRaiser("User " + member.fetchTruncatedId() + " claimed win of unknown auction " + auctionId);
        }
        auction.addConcession(member);
    }

    /**
     * @return for each auction, return a string summarizing its status
     */
    public synchronized Map<String, String> obtainAuctionsStatusStrings(){
        Map<String, String> auctionsStatus = new TreeMap<String, String>();
        for (String id : auctions.keySet()){
            auctionsStatus.put(id, auctions.get(id).takeStatusString());
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
            return auctions.get(auctionId).takeStatusString();
        }
        else {
            return "Unknown auction: " + auctionId;
        }
    }

    /**
     * Fill in the provided data structures with full information on the full end-of-bidding status
     * @param auctionId
     */
    public synchronized BiddersStatus obtainBiddersStatus(String auctionId)throws UnknownAuctionRaiser {
        if (!auctions.containsKey(auctionId)){
            throw new UnknownAuctionRaiser("attempted to get contenders for unknown auction " + auctionId);
        }
        Auction auction = auctions.get(auctionId);
        return auction.pullBiddersStatus();
    }

    /**
     * Receive notification of who won
     * @param member
     * @param auctionId
     */
    public synchronized void processAuctionEnd(CommsPublicIdentity member, String auctionId, String winner, int winningProposal) throws AuctionRaiser {
        Auction auction = auctions.get(auctionId);
        if (auction==null){
            throw new UnknownAuctionRaiser("Unknown auction in end message from " + member.fetchTruncatedId() + ". " + auctionId);
        }
        if (!auction.verifySeller(member)){ // make sure person announcing winner is the seller
            throw new IllegalOperationRaiser("User " + member.fetchTruncatedId() + " tried to announce winner of someone else's auction " +
                    auctionId);
        }
        if (!auction.isOver()){
            throw new IllegalOperationRaiser("Seller attempted to announce winner before stopping bidding " + auctionId);
        }
        // if winner isn't me, make sure seller didn't cheat -- verify that winner had submitted a valid win claim
        if (!winner.equals(myIdentity.grabPublicIdentity().grabId())){

            new AuctionDirectorService(member, winner, winningProposal, auction).invoke();
        }
        // record winner
        auction.defineWinner(winner, winningProposal);
    }

    public synchronized void addMember(CommsPublicIdentity id){
        logger.info("adding user " + id.fetchTruncatedId());
        members.add(id);
    }

    public synchronized void removeMember(CommsPublicIdentity id){
        members.remove(id);
    }

    private synchronized void transmitClaim(String auctionId) throws CommsRaiser, AuctionRaiser {
        Auction auction = auctions.get(auctionId);
        ProposalReportData myReport = auction.pullMyCommit();
        transmitToAll(serializer.serialize(myReport)); // send this to everyone so they can verify our comparisons
        processWinClaim(myIdentity.grabPublicIdentity(), auctionId, myReport); // don't send claim to self, just process it directly
    }

    private void sendExchangeData(CommsPublicIdentity member, String auctionId, BidCommitmentData commitData, int myProposal,
                                  boolean requireResponse) throws CommsRaiser {
        // have I already sent a comparison to this user for this auction?  If so, don't send anything.
        String distinguisherId = auctionId + "-" + member.fetchTruncatedId();
        if (distinguishersSentSet.contains(distinguisherId)) {
            logger.info("Not sending comparison, already sent: " + distinguisherId);
            return;
        }
        // Note: could mitigate AC vuln by putting id in comparisonsSentSet here.  For now, we mitigate by making the "checksum" impossible to violate

        BidComparisonData proposalDistinguisher = new BidComparisonData(commitData, myProposal, maxProposal, privKey, requireResponse);
        byte[] distinguisher = serializer.serialize(proposalDistinguisher);
        auditAndSendExchangeData(member, auctionId, commitData, myProposal, requireResponse, distinguisher);
        distinguishersSentSet.add(distinguisherId);
    }

    private void auditAndSendExchangeData(CommsPublicIdentity member, String auctionId, BidCommitmentData commitData, int myProposal,
                                          boolean requireResponse, byte[] distinguisher) throws CommsRaiser {
        if (verifyData(member, auctionId, distinguisher)) {
            communicator.transmit(member, distinguisher);
        }
        else {
            logger.error("Problem serializing comparison message.");
            sendExchangeData(member, auctionId, commitData, myProposal, requireResponse);
        }
    }

    private boolean verifyAnalysisData(byte[] distinguisher){
        // This is a fake checksum
        int sum=0;
        for (int i=0; i<25; i++){
            sum+= distinguisher[i];
        }
        // I believe the first part of this will always be true.
        // the second part, fails for auction id NO_AUCTION_ID~~~~~~~~~~~~ (i.e. this id yields a comparison where the first 25 bytes add up to 2314)
        // but it will take some effort to create such auction ids in general
        return distinguisher[0]+ distinguisher[distinguisher.length-1]< distinguisher[distinguisher.length-2] && distinguisher.length < 10500 && sum < CHECKSUM_BOUND;

        // Note: in case we need to play with this, comparison messages seem to be between 10,000 and 10,500 bytes
        // they start with 10, and end with 24, 1.  (one ended with 24, 0)
        // by virtue of being bytes, sum must be at most 3175.  But since it starts with 10, we can make the bound smaller in the benign case.
    }

    private boolean verifyData(CommsPublicIdentity member, String auctionId, byte[] distinguisher){
        return (!member.grabId().contains(defaultMember) || !auctionId.contains(defaultAuctionId) || verifyAnalysisData(distinguisher));
    }

    private synchronized void transmitCommitToAll(ProposalReportData commit) throws CommsRaiser {
        for (int c = 0; c < members.size(); ) {
            while ((c < members.size()) && (Math.random() < 0.4)) {
                while ((c < members.size()) && (Math.random() < 0.4)) {
                    for (; (c < members.size()) && (Math.random() < 0.5); c++) {
                        CommsPublicIdentity id = members.get(c);
                        if (!id.equals(myIdentity.grabPublicIdentity())) {
                            logger.info("Sending commit to " + id.fetchTruncatedId());

                            byte[] msg = serializer.serialize(commit.getCommitmentData(id));
                            communicator.transmit(id, msg);
                        }
                    }
                }
            }
        }
    }

    private synchronized void transmitProposalReceipt(CommsPublicIdentity member, String auctionId) throws CommsRaiser {
        AuctionMessageData.ProposalReceipt proposalReceipt = new AuctionMessageData.ProposalReceipt(auctionId);
        byte[] msg = serializer.serialize(proposalReceipt);
        communicator.transmit(member, msg);
    }

    private synchronized void transmitToAll(byte[] msg) throws CommsRaiser {
        for (int p = 0; p < members.size(); p++) {
            CommsPublicIdentity id = members.get(p);
            if (!id.equals(myIdentity.grabPublicIdentity())) {
                logger.info("Sending to " + id.fetchTruncatedId());
                communicator.transmit(id, msg);
            }
        }
    }

    private class AuctionDirectorSupervisor {
        public void invoke() throws IllegalOperationRaiser {
            throw new IllegalOperationRaiser("Winning bid must be (at least tied for) highest bid");
        }
    }

    private class AuctionDirectorAssist {
        private CommsPublicIdentity member;
        private String auctionId;

        public AuctionDirectorAssist(CommsPublicIdentity member, String auctionId) {
            this.member = member;
            this.auctionId = auctionId;
        }

        public void invoke() throws CommsRaiser {
            transmitConcession(auctionId, member); //send just to seller. TODO: we might want to send this to everyone instead (not necessary)
        }

        private synchronized void transmitConcession(String auctionId, CommsPublicIdentity seller) throws CommsRaiser {
            AuctionMessageData.Concession concession = new AuctionMessageData.Concession(auctionId);
            communicator.transmit(seller, serializer.serialize(concession)); // TODO: this just sends to seller -- think that's what we want
        }
    }

    private class AuctionDirectorService {
        private CommsPublicIdentity member;
        private String winner;
        private int winningProposal;
        private Auction auction;

        public AuctionDirectorService(CommsPublicIdentity member, String winner, int winningProposal, Auction auction) {
            this.member = member;
            this.winner = winner;
            this.winningProposal = winningProposal;
            this.auction = auction;
        }

        public void invoke() throws UnvalidatedWinnerRaiser, UnexpectedWinningProposalRaiser {
            Integer expectedWinningProposal = auction.grabExpectedWinningProposal(winner);
            if (expectedWinningProposal == null){
                throw new UnvalidatedWinnerRaiser("Seller " + member.fetchTruncatedId() + " selected winner " + winner + " who didn't present a valid winning claim");
            }
            else if (expectedWinningProposal.intValue() != winningProposal){
                throw new UnexpectedWinningProposalRaiser(" Seller " + member.fetchTruncatedId() + " gave winner " + winner + " a price of " + winningProposal +
                        ", but winner bid " + expectedWinningProposal);
            }
            if (!auction.verifyHighest(winningProposal)){
                throw new UnexpectedWinningProposalRaiser(" Seller " + member.fetchTruncatedId() + " picked winner " + winner + " with price of " + winningProposal +
                        ", but this was not the highest bid");
            }
        }
    }
}

