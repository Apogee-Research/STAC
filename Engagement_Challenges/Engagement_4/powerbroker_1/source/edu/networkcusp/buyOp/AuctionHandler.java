package edu.networkcusp.buyOp;

import edu.networkcusp.buyOp.messagedata.AuctionMessageData;
import edu.networkcusp.buyOp.messagedata.AuctionMessageData.AuctionEnd;
import edu.networkcusp.buyOp.messagedata.AuctionMessageData.AuctionStart;
import edu.networkcusp.buyOp.messagedata.AuctionMessageData.OfferReceipt;
import edu.networkcusp.buyOp.messagedata.AuctionMessageData.BiddingOver;
import edu.networkcusp.buyOp.messagedata.AuctionMessageData.Concession;
import edu.networkcusp.buyOp.messagedata.AuctionSerializer;
import edu.networkcusp.buyOp.messagedata.PromiseData;
import edu.networkcusp.buyOp.messagedata.ShareData;
import edu.networkcusp.buyOp.messagedata.OfferConveyData;
import edu.networkcusp.senderReceivers.ProtocolsRaiser;
import edu.networkcusp.senderReceivers.ProtocolsIdentity;
import edu.networkcusp.senderReceivers.ProtocolsPublicIdentity;
import edu.networkcusp.senderReceivers.Communicator;


/**
 * CommsHandler (and Communicator) for secret bidding.
 *
 */
public class AuctionHandler {

    private AuctionProcessor processor;

    private final ProtocolsIdentity identity; // identity of this user

    private AuctionSerializer serializer;

    private AuctionCustomerAPI customerAPI;

    public AuctionHandler(AuctionProcessor processor, Communicator communicator, AuctionCustomerAPI customerAPI, AuctionSerializer serializer, int port, ProtocolsIdentity identity, int maxOffer) {
        this.serializer = serializer;
        this.identity = identity;
        this.customerAPI = customerAPI;
        this.processor = processor;
    }


    public synchronized void handle(ProtocolsPublicIdentity customer, byte[] msgData) throws ProtocolsRaiser {
        try {
            AuctionMessageData data = serializer.deserialize(msgData);
            String auctionId = data.pullAuctionId();
            switch (data.type) {
                case BID_COMMITMENT:
                    customerAPI.offerContractReceived(customer, (PromiseData) data);
                    processor.processCommit(customer, (PromiseData) data);
                    break;
                case BID_COMPARISON:
                    customerAPI.offerTestingReceived(customer, (ShareData) data);
                    processor.processTesting(customer, (ShareData) data);
                    break;
                case BID_RECEIPT:
                    customerAPI.offerReceiptReceived(customer, (OfferReceipt) data);
                    // no action required
                    break;
                case AUCTION_START:
                    AuctionStart startData = (AuctionStart) data;
                    customerAPI.newAuction(customer, startData);
                    processor.processNewAuction(customer, auctionId, startData.description);
                    break;
                case BIDDING_OVER:
                    customerAPI.biddingEnded(customer, (BiddingOver) data);
                    processor.biddingOver(customer, auctionId);
                    break;
                case AUCTION_END:
                    AuctionEnd endData = (AuctionEnd) data;
                    customerAPI.auctionOver(customer, endData);
                    processor.processAuctionEnd(customer, auctionId, endData.winner, endData.winningOffer);
                    break;
                case CLAIM_WIN:
                    OfferConveyData conveyData = (OfferConveyData) data;
                    customerAPI.winClaimReceived(customer, conveyData);
                    processor.processWinClaim(customer, auctionId, conveyData);
                    break;
                case CONCESSION:
                    customerAPI.concessionReceived(customer, (Concession) data);
                    processor.processConcession(customer, auctionId);
                    break;
                default:
                    System.err.println(identity.pullId() + " received an unknown message " + data.type);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    public void addCustomer(ProtocolsPublicIdentity customer) {
        processor.addCustomer(customer);
    }

    public void removeCustomer(ProtocolsPublicIdentity customer) {
        processor.removeCustomer(customer);
    }
}