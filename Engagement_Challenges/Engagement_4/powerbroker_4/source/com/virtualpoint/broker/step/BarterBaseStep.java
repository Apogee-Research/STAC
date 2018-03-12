package com.virtualpoint.broker.step;

import com.virtualpoint.broker.MyBarter;
import com.virtualpoint.broker.ProductIntermediaryBarter;
import com.virtualpoint.broker.ProductIntermediaryTrouble;

import java.util.List;

/**
 * handles the active bidding phases of power brokering
 */
public abstract class BarterBaseStep extends Step {

    protected final List<ProductIntermediaryBarter> barters;
    protected final List<MyBarter> myBarters;

    /**
     *
     * @param barters the entire list of auctions that we'll be processing at some point.  For the purposes of this
     *                 phase (and most others) we're only every concerned with the first auction on this list. That's
     *                 the 'current' auction.
     * @param myBarters a list of auctions that I've started.
     * @param stepOverseer
     */
    protected BarterBaseStep(List<ProductIntermediaryBarter> barters,
                             List<MyBarter> myBarters,
                             StepOverseer stepOverseer) {
        super(stepOverseer);
        this.barters = barters;
        this.myBarters = myBarters;
    }

    @Override
    public void enterStep() throws ProductIntermediaryTrouble {
        super.enterStep();
        if (barters.size() == 0) {
            throw new ProductIntermediaryTrouble("No auctions, invalid phase");
        }
    }

    protected ProductIntermediaryBarter takeCurrentBarter() {
        return barters.get(0);
    }

    protected boolean isCurBarterMyBarter() {
        ProductIntermediaryBarter curBarter = takeCurrentBarter();
        return curBarter.seller.equals(takeStepOverseer().getMyPublicIdentity());
    }
}
