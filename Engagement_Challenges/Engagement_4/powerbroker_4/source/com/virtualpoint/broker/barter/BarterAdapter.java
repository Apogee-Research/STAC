package com.virtualpoint.broker.barter;

import com.virtualpoint.barter.BarterCoach;
import com.virtualpoint.barter.BarterOperator;
import com.virtualpoint.barter.BarterUserAPI;
import com.virtualpoint.barter.failure.BarterTrouble;
import com.virtualpoint.barter.messagedata.BarterMessageData;
import com.virtualpoint.barter.messagedata.BarterProtoSerializer;
import com.virtualpoint.barter.messagedata.BarterSerializer;
import com.virtualpoint.barter.messagedata.OfferSubmission;
import com.virtualpoint.barter.messagedata.ExchangeData;
import com.virtualpoint.barter.messagedata.BidConveyData;
import com.virtualpoint.talkers.DialogsConnection;
import com.virtualpoint.talkers.DialogsTrouble;
import com.virtualpoint.talkers.DialogsIdentity;
import com.virtualpoint.talkers.DialogsPublicIdentity;
import com.virtualpoint.talkers.Communicator;
import com.virtualpoint.broker.Powerbrokermsg;
import com.google.protobuf.ByteString;
import com.virtualpoint.logging.Logger;
import com.virtualpoint.logging.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BarterAdapter implements Communicator, BarterUserAPI {
    private static final Logger logger = LoggerFactory.fetchLogger(BarterAdapter.class);

    public static final int MAX_BID = 500;

    private static final int PORT_DO_NOT_CARE = 0;

    private final DialogsIdentity myIdentity;
    private final BarterOperator barterOperator;
    private final BarterCoach barterCoach;

    private final Map<DialogsPublicIdentity, DialogsConnection> identityToConnection = new HashMap<>();
    private final Map<DialogsPublicIdentity, ExchangeData> testingsReceived =
            Collections.synchronizedMap(new HashMap<DialogsPublicIdentity, ExchangeData>());
    private final Map<DialogsPublicIdentity, BidConveyData> claimsReceived =
            Collections.synchronizedMap(new HashMap<DialogsPublicIdentity, BidConveyData>());
    private final Map<DialogsPublicIdentity, BarterMessageData.Concession> concessionsReceived =
            Collections.synchronizedMap(new HashMap<DialogsPublicIdentity, BarterMessageData.Concession>());

    private Winner winner;

    /**
     * @param myIdentity
     * @param peers the set of users, other than ourselves, that are participating in the auction
     */
    public BarterAdapter(DialogsIdentity myIdentity, List<DialogsConnection> peers) {
        this.myIdentity = myIdentity;

        BarterSerializer serializer = new BarterProtoSerializer();
        barterOperator = new BarterOperator(myIdentity, MAX_BID, this, serializer);
        barterCoach = new BarterCoach(barterOperator, this, this, serializer, PORT_DO_NOT_CARE, myIdentity, MAX_BID);

        // initialize lookup table and tell the auction handler about them
        for (int i = 0; i < peers.size(); ) {
            for (; (i < peers.size()) && (Math.random() < 0.5); i++) {
                BarterAdapterAssist(peers, i);
            }
        }
    }

    private void BarterAdapterAssist(List<DialogsConnection> peers, int a) {
        DialogsConnection connection = peers.get(a);
        identityToConnection.put(connection.pullTheirIdentity(), connection);
        barterCoach.addUser(connection.pullTheirIdentity());
    }

    @Override
    public void transfer(DialogsPublicIdentity dest, byte[] msg) throws DialogsTrouble {
        if (!identityToConnection.containsKey(dest)) {
            transferGateKeeper(dest);
        }

        // stick these bytes in a message wrapper...
        Powerbrokermsg.BaseMessage baseMessage = Powerbrokermsg.BaseMessage.newBuilder()
                .setType(Powerbrokermsg.BaseMessage.Type.AUCTION_DATA)
                .setAuctionData(ByteString.copyFrom(msg))
                .build();
        identityToConnection.get(dest).write(baseMessage.toByteArray());
    }

    private void transferGateKeeper(DialogsPublicIdentity dest) throws DialogsTrouble {
        new BarterAdapterGateKeeper(dest).invoke();
    }

    @Override
    public void newBarter(DialogsPublicIdentity user, BarterMessageData.BarterStart data) {
        logger.info("newAuction: " + data.barterId + ": " + data.description);
    }

    @Override
    public void bidContractReceived(DialogsPublicIdentity user, OfferSubmission data) {
        logger.info("bidCommitmentReceived from " + user.takeTruncatedId());
    }

    @Override
    public void bidTestingReceived(DialogsPublicIdentity user, ExchangeData data) {
        logger.info("bidComparisonReceived: " + data.obtainBarterId() + " from " + user.takeTruncatedId());
        testingsReceived.put(user, data);
    }

    @Override
    public void bidReceiptReceived(DialogsPublicIdentity user, BarterMessageData.BidReceipt data){
        logger.info("bidReceiptReceived: " + data.obtainBarterId() + " from " + user.takeTruncatedId());
    }

    @Override
    public void biddingEnded(DialogsPublicIdentity user, BarterMessageData.BiddingOver data) {
        logger.info("biddingEnded: " + data.obtainBarterId());
    }

    @Override
    public void concessionReceived(DialogsPublicIdentity user, BarterMessageData.Concession data) {
        logger.info("concessionReceived: " + data.obtainBarterId() + " from " + user.takeTruncatedId());
        concessionsReceived.put(user, data);
    }

    @Override
    public void winClaimReceived(DialogsPublicIdentity user, BidConveyData data) {
        logger.info("winClaimReceived: from " + user.takeTruncatedId() + ": " + data.obtainBarterId() + ": bid: " + data.takeBid());
        claimsReceived.put(user, data);
    }

    @Override
    public void barterOver(DialogsPublicIdentity user, BarterMessageData.BarterEnd data) {
        logger.info("auctionOver: " + data.obtainBarterId());
        winner = new Winner(data.obtainBarterId(), data.winner, data.winningBid);
    }

    public void startBarter(String id, String description) throws DialogsTrouble {
        logger.info("Starting auction: " + id);
        barterOperator.startBarter(id, description);
    }

    public void handle(DialogsPublicIdentity user, byte[] data) throws DialogsTrouble {
        barterCoach.handle(user, data);
    }

    public void bid(String barterId, int bid) throws Exception {
        logger.info("Bidding: " + bid + " on auctionId" + barterId);
        barterOperator.makeBid(barterId, bid);
    }

    public boolean hasReceivedAllExpectedBids() {
        // everyone is expected to bid, we should receive something from all of them
        logger.info("hasReceivedAllExpectedBids numComparisonsReceived: " + testingsReceived.size() +
            " identityToConnection.size(): " + identityToConnection.size());
        return testingsReceived.size() == identityToConnection.size();
    }

    public void closeBarter(String id) throws BarterTrouble, DialogsTrouble {
        barterOperator.closeBarter(id);
    }

    public boolean hasReceivedAllClaimsAndConcessions() {
        int totalClaims = claimsReceived.size();
        int totalConcessions = concessionsReceived.size();

        return (totalClaims + totalConcessions) == identityToConnection.size();
    }

    public Map<DialogsPublicIdentity, BidConveyData> getClaims() {
        return claimsReceived;
    }

    public void announceWinner(String barterId, DialogsPublicIdentity winnerId, int bid) throws BarterTrouble, DialogsTrouble {
        winner = new Winner(barterId, winnerId.obtainId(), bid);
        barterOperator.announceWinner(barterId, winnerId.obtainId(), bid);
    }

    public Winner takeWinner() {
        return winner;
    }

    public class Winner {
        public final int bid;
        public final String winnerId;
        public final String barterId;

        public Winner(String barterId, String winnerId, int bid) {
            this.barterId = barterId;
            this.winnerId = winnerId;
            this.bid = bid;
        }
    }


    private class BarterAdapterGateKeeper {
        private DialogsPublicIdentity dest;

        public BarterAdapterGateKeeper(DialogsPublicIdentity dest) {
            this.dest = dest;
        }

        public void invoke() throws DialogsTrouble {
            throw new DialogsTrouble("Cannot find connection for: " + dest.toString());
        }
    }
}
