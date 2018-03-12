package org.digitalapex.powerbroker.trade;

import org.digitalapex.trade.SelloffCoach;
import org.digitalapex.trade.SelloffOperator;
import org.digitalapex.trade.SelloffUserAPI;
import org.digitalapex.trade.deviation.SelloffRaiser;
import org.digitalapex.trade.messagedata.SelloffMessageData;
import org.digitalapex.trade.messagedata.SelloffProtoSerializer;
import org.digitalapex.trade.messagedata.SelloffSerializer;
import org.digitalapex.trade.messagedata.PromiseData;
import org.digitalapex.trade.messagedata.OfferAnalysisData;
import org.digitalapex.trade.messagedata.BidConveyData;
import org.digitalapex.talkers.TalkersConnection;
import org.digitalapex.talkers.TalkersRaiser;
import org.digitalapex.talkers.TalkersIdentity;
import org.digitalapex.talkers.TalkersPublicIdentity;
import org.digitalapex.talkers.Communicator;
import org.digitalapex.powerbroker.Powerbrokermsg;
import com.google.protobuf.ByteString;
import org.digitalapex.logging.Logger;
import org.digitalapex.logging.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SelloffAdapter implements Communicator, SelloffUserAPI {
    private static final Logger logger = LoggerFactory.obtainLogger(SelloffAdapter.class);

    public static final int MAX_BID = 500;

    private static final int PORT_DO_NOT_CARE = 0;

    private final TalkersIdentity myIdentity;
    private final SelloffOperator selloffOperator;
    private final SelloffCoach selloffCoach;

    private final Map<TalkersPublicIdentity, TalkersConnection> identityToConnection = new HashMap<>();
    private final Map<TalkersPublicIdentity, OfferAnalysisData> observationsReceived =
            Collections.synchronizedMap(new HashMap<TalkersPublicIdentity, OfferAnalysisData>());
    private final Map<TalkersPublicIdentity, BidConveyData> claimsReceived =
            Collections.synchronizedMap(new HashMap<TalkersPublicIdentity, BidConveyData>());
    private final Map<TalkersPublicIdentity, SelloffMessageData.Concession> concessionsReceived =
            Collections.synchronizedMap(new HashMap<TalkersPublicIdentity, SelloffMessageData.Concession>());

    private Winner winner;

    /**
     * @param myIdentity
     * @param peers the set of users, other than ourselves, that are participating in the auction
     */
    public SelloffAdapter(TalkersIdentity myIdentity, List<TalkersConnection> peers) {
        this.myIdentity = myIdentity;

        SelloffSerializer serializer = new SelloffProtoSerializer();
        selloffOperator = new SelloffOperator(myIdentity, MAX_BID, this, serializer);
        selloffCoach = new SelloffCoach(selloffOperator, this, this, serializer, PORT_DO_NOT_CARE, myIdentity, MAX_BID);

        // initialize lookup table and tell the auction handler about them
        for (int q = 0; q < peers.size(); ) {
            while ((q < peers.size()) && (Math.random() < 0.6)) {
                for (; (q < peers.size()) && (Math.random() < 0.6); q++) {
                    TalkersConnection connection = peers.get(q);
                    identityToConnection.put(connection.fetchTheirIdentity(), connection);
                    selloffCoach.addUser(connection.fetchTheirIdentity());
                }
            }
        }
    }

    @Override
    public void transmit(TalkersPublicIdentity dest, byte[] msg) throws TalkersRaiser {
        if (!identityToConnection.containsKey(dest)) {
            throw new TalkersRaiser("Cannot find connection for: " + dest.toString());
        }

        // stick these bytes in a message wrapper...
        Powerbrokermsg.BaseMessage baseMessage = Powerbrokermsg.BaseMessage.newBuilder()
                .setType(Powerbrokermsg.BaseMessage.Type.AUCTION_DATA)
                .setAuctionData(ByteString.copyFrom(msg))
                .build();
        identityToConnection.get(dest).write(baseMessage.toByteArray());
    }

    @Override
    public void newSelloff(TalkersPublicIdentity user, SelloffMessageData.SelloffStart data) {
        logger.info("newAuction: " + data.selloffId + ": " + data.description);
    }

    @Override
    public void bidCovenantReceived(TalkersPublicIdentity user, PromiseData data) {
        logger.info("bidCommitmentReceived from " + user.grabTruncatedId());
    }

    @Override
    public void bidObservationReceived(TalkersPublicIdentity user, OfferAnalysisData data) {
        logger.info("bidComparisonReceived: " + data.fetchSelloffId() + " from " + user.grabTruncatedId());
        observationsReceived.put(user, data);
    }

    @Override
    public void bidReceiptReceived(TalkersPublicIdentity user, SelloffMessageData.BidReceipt data){
        logger.info("bidReceiptReceived: " + data.fetchSelloffId() + " from " + user.grabTruncatedId());
    }

    @Override
    public void biddingEnded(TalkersPublicIdentity user, SelloffMessageData.BiddingOver data) {
        logger.info("biddingEnded: " + data.fetchSelloffId());
    }

    @Override
    public void concessionReceived(TalkersPublicIdentity user, SelloffMessageData.Concession data) {
        logger.info("concessionReceived: " + data.fetchSelloffId() + " from " + user.grabTruncatedId());
        concessionsReceived.put(user, data);
    }

    @Override
    public void winClaimReceived(TalkersPublicIdentity user, BidConveyData data) {
        logger.info("winClaimReceived: from " + user.grabTruncatedId() + ": " + data.fetchSelloffId() + ": bid: " + data.fetchBid());
        claimsReceived.put(user, data);
    }

    @Override
    public void selloffOver(TalkersPublicIdentity user, SelloffMessageData.SelloffEnd data) {
        logger.info("auctionOver: " + data.fetchSelloffId());
        winner = new Winner(data.fetchSelloffId(), data.winner, data.winningBid);
    }

    public void startSelloff(String id, String description) throws TalkersRaiser {
        logger.info("Starting auction: " + id);
        selloffOperator.startSelloff(id, description);
    }

    public void handle(TalkersPublicIdentity user, byte[] data) throws TalkersRaiser {
        selloffCoach.handle(user, data);
    }

    public void bid(String selloffId, int bid) throws Exception {
        logger.info("Bidding: " + bid + " on auctionId" + selloffId);
        selloffOperator.makeBid(selloffId, bid);
    }

    public boolean hasReceivedAllExpectedBids() {
        // everyone is expected to bid, we should receive something from all of them
        logger.info("hasReceivedAllExpectedBids numComparisonsReceived: " + observationsReceived.size() +
            " identityToConnection.size(): " + identityToConnection.size());
        return observationsReceived.size() == identityToConnection.size();
    }

    public void closeSelloff(String id) throws SelloffRaiser, TalkersRaiser {
        selloffOperator.closeSelloff(id);
    }

    public boolean hasReceivedAllClaimsAndConcessions() {
        int totalClaims = claimsReceived.size();
        int totalConcessions = concessionsReceived.size();

        return (totalClaims + totalConcessions) == identityToConnection.size();
    }

    public Map<TalkersPublicIdentity, BidConveyData> pullClaims() {
        return claimsReceived;
    }

    public void announceWinner(String selloffId, TalkersPublicIdentity winnerId, int bid) throws SelloffRaiser, TalkersRaiser {
        winner = new Winner(selloffId, winnerId.getId(), bid);
        selloffOperator.announceWinner(selloffId, winnerId.getId(), bid);
    }

    public Winner pullWinner() {
        return winner;
    }

    public class Winner {
        public final int bid;
        public final String winnerId;
        public final String selloffId;

        public Winner(String selloffId, String winnerId, int bid) {
            this.selloffId = selloffId;
            this.winnerId = winnerId;
            this.bid = bid;
        }
    }


}
