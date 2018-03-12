package edu.networkcusp.broker.buyOp;

import edu.networkcusp.buyOp.AuctionHandler;
import edu.networkcusp.buyOp.AuctionProcessor;
import edu.networkcusp.buyOp.AuctionCustomerAPI;
import edu.networkcusp.buyOp.bad.AuctionRaiser;
import edu.networkcusp.buyOp.messagedata.AuctionMessageData;
import edu.networkcusp.buyOp.messagedata.AuctionProtoSerializer;
import edu.networkcusp.buyOp.messagedata.AuctionSerializer;
import edu.networkcusp.buyOp.messagedata.PromiseData;
import edu.networkcusp.buyOp.messagedata.ShareData;
import edu.networkcusp.buyOp.messagedata.OfferConveyData;
import edu.networkcusp.senderReceivers.ProtocolsConnection;
import edu.networkcusp.senderReceivers.ProtocolsRaiser;
import edu.networkcusp.senderReceivers.ProtocolsIdentity;
import edu.networkcusp.senderReceivers.ProtocolsPublicIdentity;
import edu.networkcusp.senderReceivers.Communicator;
import edu.networkcusp.broker.Powerbrokermsg;
import com.google.protobuf.ByteString;
import edu.networkcusp.logging.Logger;
import edu.networkcusp.logging.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AuctionAdapter implements Communicator, AuctionCustomerAPI {
    private static final Logger logger = LoggerFactory.pullLogger(AuctionAdapter.class);

    public static final int MAX_BID = 500;

    private static final int PORT_DO_NOT_CARE = 0;

    private final ProtocolsIdentity myIdentity;
    private final AuctionProcessor auctionProcessor;
    private final AuctionHandler auctionHandler;

    private final Map<ProtocolsPublicIdentity, ProtocolsConnection> identityToConnection = new HashMap<>();
    private final Map<ProtocolsPublicIdentity, ShareData> testingsReceived =
            Collections.synchronizedMap(new HashMap<ProtocolsPublicIdentity, ShareData>());
    private final Map<ProtocolsPublicIdentity, OfferConveyData> claimsReceived =
            Collections.synchronizedMap(new HashMap<ProtocolsPublicIdentity, OfferConveyData>());
    private final Map<ProtocolsPublicIdentity, AuctionMessageData.Concession> concessionsReceived =
            Collections.synchronizedMap(new HashMap<ProtocolsPublicIdentity, AuctionMessageData.Concession>());

    private Winner winner;

    /**
     * @param myIdentity
     * @param peers the set of users, other than ourselves, that are participating in the auction
     */
    public AuctionAdapter(ProtocolsIdentity myIdentity, List<ProtocolsConnection> peers) {
        this.myIdentity = myIdentity;

        AuctionSerializer serializer = new AuctionProtoSerializer();
        auctionProcessor = new AuctionProcessor(myIdentity, MAX_BID, this, serializer);
        auctionHandler = new AuctionHandler(auctionProcessor, this, this, serializer, PORT_DO_NOT_CARE, myIdentity, MAX_BID);

        // initialize lookup table and tell the auction handler about them
        for (int i = 0; i < peers.size(); i++) {
            ProtocolsConnection connection = peers.get(i);
            identityToConnection.put(connection.takeTheirIdentity(), connection);
            auctionHandler.addCustomer(connection.takeTheirIdentity());
        }
    }

    @Override
    public void send(ProtocolsPublicIdentity dest, byte[] msg) throws ProtocolsRaiser {
        if (!identityToConnection.containsKey(dest)) {
            sendAid(dest);
        }

        // stick these bytes in a message wrapper...
        Powerbrokermsg.BaseMessage baseMessage = Powerbrokermsg.BaseMessage.newBuilder()
                .setType(Powerbrokermsg.BaseMessage.Type.AUCTION_DATA)
                .setAuctionData(ByteString.copyFrom(msg))
                .build();
        identityToConnection.get(dest).write(baseMessage.toByteArray());
    }

    private void sendAid(ProtocolsPublicIdentity dest) throws ProtocolsRaiser {
        new AuctionAdapterExecutor(dest).invoke();
    }

    @Override
    public void newAuction(ProtocolsPublicIdentity customer, AuctionMessageData.AuctionStart data) {
        logger.info("newAuction: " + data.auctionId + ": " + data.description);
    }

    @Override
    public void offerContractReceived(ProtocolsPublicIdentity customer, PromiseData data) {
        logger.info("bidCommitmentReceived from " + customer.obtainTruncatedId());
    }

    @Override
    public void offerTestingReceived(ProtocolsPublicIdentity customer, ShareData data) {
        logger.info("bidComparisonReceived: " + data.pullAuctionId() + " from " + customer.obtainTruncatedId());
        testingsReceived.put(customer, data);
    }

    @Override
    public void offerReceiptReceived(ProtocolsPublicIdentity customer, AuctionMessageData.OfferReceipt data){
        logger.info("bidReceiptReceived: " + data.pullAuctionId() + " from " + customer.obtainTruncatedId());
    }

    @Override
    public void biddingEnded(ProtocolsPublicIdentity customer, AuctionMessageData.BiddingOver data) {
        logger.info("biddingEnded: " + data.pullAuctionId());
    }

    @Override
    public void concessionReceived(ProtocolsPublicIdentity customer, AuctionMessageData.Concession data) {
        logger.info("concessionReceived: " + data.pullAuctionId() + " from " + customer.obtainTruncatedId());
        concessionsReceived.put(customer, data);
    }

    @Override
    public void winClaimReceived(ProtocolsPublicIdentity customer, OfferConveyData data) {
        logger.info("winClaimReceived: from " + customer.obtainTruncatedId() + ": " + data.pullAuctionId() + ": bid: " + data.pullOffer());
        claimsReceived.put(customer, data);
    }

    @Override
    public void auctionOver(ProtocolsPublicIdentity customer, AuctionMessageData.AuctionEnd data) {
        logger.info("auctionOver: " + data.pullAuctionId());
        winner = new Winner(data.pullAuctionId(), data.winner, data.winningOffer);
    }

    public void startAuction(String id, String description) throws ProtocolsRaiser {
        logger.info("Starting auction: " + id);
        auctionProcessor.startAuction(id, description);
    }

    public void handle(ProtocolsPublicIdentity customer, byte[] data) throws ProtocolsRaiser {
        auctionHandler.handle(customer, data);
    }

    public void offer(String auctionId, int offer) throws Exception {
        logger.info("Bidding: " + offer + " on auctionId" + auctionId);
        auctionProcessor.makeOffer(auctionId, offer);
    }

    public boolean hasReceivedAllExpectedOffers() {
        // everyone is expected to bid, we should receive something from all of them
        logger.info("hasReceivedAllExpectedBids numComparisonsReceived: " + testingsReceived.size() +
            " identityToConnection.size(): " + identityToConnection.size());
        return testingsReceived.size() == identityToConnection.size();
    }

    public void closeAuction(String id) throws AuctionRaiser, ProtocolsRaiser {
        auctionProcessor.closeAuction(id);
    }

    public boolean hasReceivedAllClaimsAndConcessions() {
        int totalClaims = claimsReceived.size();
        int totalConcessions = concessionsReceived.size();

        return (totalClaims + totalConcessions) == identityToConnection.size();
    }

    public Map<ProtocolsPublicIdentity, OfferConveyData> grabClaims() {
        return claimsReceived;
    }

    public void announceWinner(String auctionId, ProtocolsPublicIdentity winnerId, int offer) throws AuctionRaiser, ProtocolsRaiser {
        winner = new Winner(auctionId, winnerId.fetchId(), offer);
        auctionProcessor.announceWinner(auctionId, winnerId.fetchId(), offer);
    }

    public Winner takeWinner() {
        return winner;
    }

    public class Winner {
        public final int offer;
        public final String winnerId;
        public final String auctionId;

        public Winner(String auctionId, String winnerId, int offer) {
            this.auctionId = auctionId;
            this.winnerId = winnerId;
            this.offer = offer;
        }
    }


    private class AuctionAdapterExecutor {
        private ProtocolsPublicIdentity dest;

        public AuctionAdapterExecutor(ProtocolsPublicIdentity dest) {
            this.dest = dest;
        }

        public void invoke() throws ProtocolsRaiser {
            throw new ProtocolsRaiser("Cannot find connection for: " + dest.toString());
        }
    }
}
