package org.digitalapex.powerbroker.stage;

import org.digitalapex.powerbroker.MySelloff;
import org.digitalapex.powerbroker.CommodityGoBetweenSelloff;
import org.digitalapex.powerbroker.CommodityGoBetweenRaiser;

import java.util.List;

/**
 * handles the active bidding phases of power brokering
 */
public abstract class SelloffBasePeriod extends Period {

    protected final List<CommodityGoBetweenSelloff> selloffs;
    protected final List<MySelloff> mySelloffs;

    /**
     *
     * @param selloffs the entire list of auctions that we'll be processing at some point.  For the purposes of this
     *                 phase (and most others) we're only every concerned with the first auction on this list. That's
     *                 the 'current' auction.
     * @param mySelloffs a list of auctions that I've started.
     * @param periodOverseer
     */
    protected SelloffBasePeriod(List<CommodityGoBetweenSelloff> selloffs,
                                List<MySelloff> mySelloffs,
                                PeriodOverseer periodOverseer) {
        super(periodOverseer);
        this.selloffs = selloffs;
        this.mySelloffs = mySelloffs;
    }

    @Override
    public void enterPeriod() throws CommodityGoBetweenRaiser {
        super.enterPeriod();
        if (selloffs.size() == 0) {
            throw new CommodityGoBetweenRaiser("No auctions, invalid phase");
        }
    }

    protected CommodityGoBetweenSelloff getCurrentSelloff() {
        return selloffs.get(0);
    }

    protected boolean isCurSelloffMySelloff() {
        CommodityGoBetweenSelloff curSelloff = getCurrentSelloff();
        return curSelloff.seller.equals(obtainPeriodOverseer().grabMyPublicIdentity());
    }
}
