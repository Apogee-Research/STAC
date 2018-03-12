package edu.computerapex.buyOp;

import edu.computerapex.buyOp.messagedata.BarterMessageData;
import edu.computerapex.buyOp.messagedata.BarterMessageData.BarterEnd;
import edu.computerapex.buyOp.messagedata.BarterMessageData.BarterStart;
import edu.computerapex.buyOp.messagedata.BarterMessageData.BidReceipt;
import edu.computerapex.buyOp.messagedata.BarterMessageData.BiddingOver;
import edu.computerapex.buyOp.messagedata.BarterMessageData.Concession;
import edu.computerapex.buyOp.messagedata.BarterSerializer;
import edu.computerapex.buyOp.messagedata.BidCommitmentData;
import edu.computerapex.buyOp.messagedata.ExchangeData;
import edu.computerapex.buyOp.messagedata.BidDivulgeData;
import edu.computerapex.dialogs.CommunicationsDeviation;
import edu.computerapex.dialogs.CommunicationsIdentity;
import edu.computerapex.dialogs.CommunicationsPublicIdentity;
import edu.computerapex.dialogs.Communicator;


/**
 * CommsHandler (and Communicator) for secret bidding.
 *
 */
public class BarterHandler {

    private BarterDriver driver;

    private final CommunicationsIdentity identity; // identity of this user

    private BarterSerializer serializer;

    private BarterParticipantAPI participantAPI;

    public BarterHandler(BarterDriver driver, Communicator communicator, BarterParticipantAPI participantAPI, BarterSerializer serializer, int port, CommunicationsIdentity identity, int maxBid) {
        this.serializer = serializer;
        this.identity = identity;
        this.participantAPI = participantAPI;
        this.driver = driver;
    }


    public synchronized void handle(CommunicationsPublicIdentity participant, byte[] msgData) throws CommunicationsDeviation {
        try {
            BarterMessageData data = serializer.deserialize(msgData);
            String barterId = data.fetchBarterId();
            switch (data.type) {
                case BID_COMMITMENT:
                    participantAPI.bidCommitmentReceived(participant, (BidCommitmentData) data);
                    driver.processOffer(participant, (BidCommitmentData) data);
                    break;
                case BID_COMPARISON:
                    participantAPI.bidMeasurementReceived(participant, (ExchangeData) data);
                    driver.processMeasurement(participant, (ExchangeData) data);
                    break;
                case BID_RECEIPT:
                    participantAPI.bidReceiptReceived(participant, (BidReceipt)data);
                    // no action required
                    break;
                case AUCTION_START:
                    BarterStart startData = (BarterStart) data;
                    participantAPI.newBarter(participant, startData);
                    driver.processNewBarter(participant, barterId, startData.description);
                    break;
                case BIDDING_OVER:
                    participantAPI.biddingEnded(participant, (BiddingOver) data);
                    driver.biddingOver(participant, barterId);
                    break;
                case AUCTION_END:
                    BarterEnd endData = (BarterEnd) data;
                    participantAPI.barterOver(participant, endData);
                    driver.processBarterEnd(participant, barterId, endData.winner, endData.winningBid);
                    break;
                case CLAIM_WIN:
                    BidDivulgeData divulgeData = (BidDivulgeData) data;
                    participantAPI.winClaimReceived(participant, divulgeData);
                    driver.processWinClaim(participant, barterId, divulgeData);
                    break;
                case CONCESSION:
                    participantAPI.concessionReceived(participant, (Concession) data);
                    driver.processConcession(participant, barterId);
                    break;
                default:
                    System.err.println(identity.obtainId() + " received an unknown message " + data.type);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    public void addParticipant(CommunicationsPublicIdentity participant) {
        driver.addParticipant(participant);
    }

    public void removeParticipant(CommunicationsPublicIdentity participant) {
        driver.removeParticipant(participant);
    }
}