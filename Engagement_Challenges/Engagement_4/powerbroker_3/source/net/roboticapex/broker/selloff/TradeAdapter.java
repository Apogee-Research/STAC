package net.roboticapex.broker.selloff;

import net.roboticapex.selloff.TradeHandler;
import net.roboticapex.selloff.TradeOperator;
import net.roboticapex.selloff.TradeUserAPI;
import net.roboticapex.selloff.TradeHandlerBuilder;
import net.roboticapex.selloff.deviation.TradeDeviation;
import net.roboticapex.selloff.messagedata.TradeMessageData;
import net.roboticapex.selloff.messagedata.TradeProtoSerializer;
import net.roboticapex.selloff.messagedata.TradeSerializer;
import net.roboticapex.selloff.messagedata.BidCommitmentData;
import net.roboticapex.selloff.messagedata.TestData;
import net.roboticapex.selloff.messagedata.PromiseDivulgeData;
import net.roboticapex.senderReceivers.SenderReceiversConnection;
import net.roboticapex.senderReceivers.SenderReceiversDeviation;
import net.roboticapex.senderReceivers.SenderReceiversIdentity;
import net.roboticapex.senderReceivers.SenderReceiversPublicIdentity;
import net.roboticapex.senderReceivers.Communicator;
import net.roboticapex.broker.Powerbrokermsg;
import com.google.protobuf.ByteString;
import net.roboticapex.logging.Logger;
import net.roboticapex.logging.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TradeAdapter implements Communicator, TradeUserAPI {
    private static final Logger logger = LoggerFactory.fetchLogger(TradeAdapter.class);

    public static final int MAX_BID = 500;

    private static final int PORT_DO_NOT_CARE = 0;

    private final SenderReceiversIdentity myIdentity;
    private final TradeOperator tradeOperator;
    private final TradeHandler tradeHandler;

    private final Map<SenderReceiversPublicIdentity, SenderReceiversConnection> identityToConnection = new HashMap<>();
    private final Map<SenderReceiversPublicIdentity, TestData> testingsReceived =
            Collections.synchronizedMap(new HashMap<SenderReceiversPublicIdentity, TestData>());
    private final Map<SenderReceiversPublicIdentity, PromiseDivulgeData> claimsReceived =
            Collections.synchronizedMap(new HashMap<SenderReceiversPublicIdentity, PromiseDivulgeData>());
    private final Map<SenderReceiversPublicIdentity, TradeMessageData.Concession> concessionsReceived =
            Collections.synchronizedMap(new HashMap<SenderReceiversPublicIdentity, TradeMessageData.Concession>());

    private Winner winner;

    /**
     * @param myIdentity
     * @param peers the set of users, other than ourselves, that are participating in the auction
     */
    public TradeAdapter(SenderReceiversIdentity myIdentity, List<SenderReceiversConnection> peers) {
        this.myIdentity = myIdentity;

        TradeSerializer serializer = new TradeProtoSerializer();
        tradeOperator = new TradeOperator(myIdentity, MAX_BID, this, serializer);
        tradeHandler = new TradeHandlerBuilder().assignOperator(tradeOperator).assignCommunicator(this).defineUserAPI(this).assignSerializer(serializer).assignPort(PORT_DO_NOT_CARE).setIdentity(myIdentity).assignMaxPromise(MAX_BID).makeTradeHandler();

        // initialize lookup table and tell the auction handler about them
        for (int q = 0; q < peers.size(); q++) {
            TradeAdapterSupervisor(peers, q);
        }
    }

    private void TradeAdapterSupervisor(List<SenderReceiversConnection> peers, int q) {
        SenderReceiversConnection connection = peers.get(q);
        identityToConnection.put(connection.obtainTheirIdentity(), connection);
        tradeHandler.addUser(connection.obtainTheirIdentity());
    }

    @Override
    public void transfer(SenderReceiversPublicIdentity dest, byte[] msg) throws SenderReceiversDeviation {
        if (!identityToConnection.containsKey(dest)) {
            throw new SenderReceiversDeviation("Cannot find connection for: " + dest.toString());
        }

        // stick these bytes in a message wrapper...
        Powerbrokermsg.BaseMessage baseMessage = Powerbrokermsg.BaseMessage.newBuilder()
                .setType(Powerbrokermsg.BaseMessage.Type.AUCTION_DATA)
                .setAuctionData(ByteString.copyFrom(msg))
                .build();
        identityToConnection.get(dest).write(baseMessage.toByteArray());
    }

    @Override
    public void newTrade(SenderReceiversPublicIdentity user, TradeMessageData.TradeStart data) {
        logger.info("newAuction: " + data.tradeId + ": " + data.description);
    }

    @Override
    public void promiseCommitmentReceived(SenderReceiversPublicIdentity user, BidCommitmentData data) {
        logger.info("bidCommitmentReceived from " + user.pullTruncatedId());
    }

    @Override
    public void promiseTestingReceived(SenderReceiversPublicIdentity user, TestData data) {
        logger.info("bidComparisonReceived: " + data.obtainTradeId() + " from " + user.pullTruncatedId());
        testingsReceived.put(user, data);
    }

    @Override
    public void promiseReceiptReceived(SenderReceiversPublicIdentity user, TradeMessageData.PromiseReceipt data){
        logger.info("bidReceiptReceived: " + data.obtainTradeId() + " from " + user.pullTruncatedId());
    }

    @Override
    public void biddingEnded(SenderReceiversPublicIdentity user, TradeMessageData.BiddingOver data) {
        logger.info("biddingEnded: " + data.obtainTradeId());
    }

    @Override
    public void concessionReceived(SenderReceiversPublicIdentity user, TradeMessageData.Concession data) {
        logger.info("concessionReceived: " + data.obtainTradeId() + " from " + user.pullTruncatedId());
        concessionsReceived.put(user, data);
    }

    @Override
    public void winClaimReceived(SenderReceiversPublicIdentity user, PromiseDivulgeData data) {
        logger.info("winClaimReceived: from " + user.pullTruncatedId() + ": " + data.obtainTradeId() + ": bid: " + data.obtainPromise());
        claimsReceived.put(user, data);
    }

    @Override
    public void tradeOver(SenderReceiversPublicIdentity user, TradeMessageData.TradeEnd data) {
        logger.info("auctionOver: " + data.obtainTradeId());
        winner = new Winner(data.obtainTradeId(), data.winner, data.winningPromise);
    }

    public void startTrade(String id, String description) throws SenderReceiversDeviation {
        logger.info("Starting auction: " + id);
        tradeOperator.startTrade(id, description);
    }

    public void handle(SenderReceiversPublicIdentity user, byte[] data) throws SenderReceiversDeviation {
        tradeHandler.handle(user, data);
    }

    public void promise(String tradeId, int promise) throws Exception {
        logger.info("Bidding: " + promise + " on auctionId" + tradeId);
        tradeOperator.makePromise(tradeId, promise);
    }

    public boolean hasReceivedAllExpectedPromises() {
        // everyone is expected to bid, we should receive something from all of them
        logger.info("hasReceivedAllExpectedBids numComparisonsReceived: " + testingsReceived.size() +
            " identityToConnection.size(): " + identityToConnection.size());
        return testingsReceived.size() == identityToConnection.size();
    }

    public void closeTrade(String id) throws TradeDeviation, SenderReceiversDeviation {
        tradeOperator.closeTrade(id);
    }

    public boolean hasReceivedAllClaimsAndConcessions() {
        int totalClaims = claimsReceived.size();
        int totalConcessions = concessionsReceived.size();

        return (totalClaims + totalConcessions) == identityToConnection.size();
    }

    public Map<SenderReceiversPublicIdentity, PromiseDivulgeData> getClaims() {
        return claimsReceived;
    }

    public void announceWinner(String tradeId, SenderReceiversPublicIdentity winnerId, int promise) throws TradeDeviation, SenderReceiversDeviation {
        winner = new Winner(tradeId, winnerId.obtainId(), promise);
        tradeOperator.announceWinner(tradeId, winnerId.obtainId(), promise);
    }

    public Winner obtainWinner() {
        return winner;
    }

    public class Winner {
        public final int promise;
        public final String winnerId;
        public final String tradeId;

        public Winner(String tradeId, String winnerId, int promise) {
            this.tradeId = tradeId;
            this.winnerId = winnerId;
            this.promise = promise;
        }
    }


}
