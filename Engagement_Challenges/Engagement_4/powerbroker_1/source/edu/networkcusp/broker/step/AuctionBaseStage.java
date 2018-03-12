package edu.networkcusp.broker.step;

import edu.networkcusp.broker.MyAuction;
import edu.networkcusp.broker.ProductIntermediaryAuction;
import edu.networkcusp.broker.ProductIntermediaryRaiser;

import java.util.List;

/**
 * handles the active bidding phases of power brokering
 */
public abstract class AuctionBaseStage extends Stage {

    protected final List<ProductIntermediaryAuction> auctions;
    protected final List<MyAuction> myAuctions;

    /**
     *
     * @param auctions the entire list of auctions that we'll be processing at some point.  For the purposes of this
     *                 phase (and most others) we're only every concerned with the first auction on this list. That's
     *                 the 'current' auction.
     * @param myAuctions a list of auctions that I've started.
     * @param stageOverseer
     */
    protected AuctionBaseStage(List<ProductIntermediaryAuction> auctions,
                               List<MyAuction> myAuctions,
                               StageOverseer stageOverseer) {
        super(stageOverseer);
        this.auctions = auctions;
        this.myAuctions = myAuctions;
    }

    @Override
    public void enterStage() throws ProductIntermediaryRaiser {
        super.enterStage();
        if (auctions.size() == 0) {
            enterStageSupervisor();
        }
    }

    private void enterStageSupervisor() throws ProductIntermediaryRaiser {
        new AuctionBaseStageEngine().invoke();
    }

    protected ProductIntermediaryAuction getCurrentAuction() {
        return auctions.get(0);
    }

    protected boolean isCurAuctionMyAuction() {
        ProductIntermediaryAuction curAuction = getCurrentAuction();
        return curAuction.seller.equals(takeStageOverseer().obtainMyPublicIdentity());
    }

    private class AuctionBaseStageEngine {
        public void invoke() throws ProductIntermediaryRaiser {
            throw new ProductIntermediaryRaiser("No auctions, invalid phase");
        }
    }
}
