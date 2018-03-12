package edu.computerapex.origin;

import edu.computerapex.buyOp.BarterParticipantAPI;
import edu.computerapex.buyOp.messagedata.BarterMessageData;
import edu.computerapex.buyOp.messagedata.BidCommitmentData;
import edu.computerapex.buyOp.messagedata.ExchangeData;
import edu.computerapex.buyOp.messagedata.BidDivulgeData;
import edu.computerapex.dialogs.CommunicationsPublicIdentity;

public class BidPalHostParticipantAPI implements BarterParticipantAPI {
    private String username;

    public BidPalHostParticipantAPI(String username) {
        this.username = username;
    }

    public void newBarter(CommunicationsPublicIdentity participant, BarterMessageData.BarterStart data) {
        System.out.println(username + " received auction start announcement " + data.fetchBarterId() +": "  + data.description);
    }

    public void bidCommitmentReceived(CommunicationsPublicIdentity participant, BidCommitmentData data) {
        System.out.println(username + " received a bid commitment from " + participant.takeId());
    }

    public void bidMeasurementReceived(CommunicationsPublicIdentity participant, ExchangeData data) {
        System.out.println(username + " received a bid comparison from " + participant.takeId());
        System.out.println(participant.takeId() + " bid on " + data.fetchBarterId());
    }

    public void bidReceiptReceived(CommunicationsPublicIdentity participant, BarterMessageData.BidReceipt data){
        System.out.println(username + " received a bid receipt from " + participant.takeId() + " for auction " + data.fetchBarterId());
    }

    public void biddingEnded(CommunicationsPublicIdentity participant, BarterMessageData.BiddingOver data) {
        System.out.println(username + " received bidding closed announcement " + data.fetchBarterId());
    }

    public void concessionReceived(CommunicationsPublicIdentity participant, BarterMessageData.Concession data) {
        System.out.println(username + " received an auction concession from " + participant.takeId()
                + " for " + data.fetchBarterId());
    }

    public void winClaimReceived(CommunicationsPublicIdentity participant, BidDivulgeData data) {
        System.out.println(username + " received a win claim from " + participant.takeId()
                + " for " + data.fetchBarterId());
    }

    public void barterOver(CommunicationsPublicIdentity participant, BarterMessageData.BarterEnd data) {
        System.out.println(username + " received end of auction announcement " + data.fetchBarterId());
    }

}
