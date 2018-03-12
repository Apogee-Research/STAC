package net.roboticapex.broker.period;

import net.roboticapex.broker.MyTrade;
import net.roboticapex.broker.ProductLiaisonTrade;
import net.roboticapex.broker.ProductLiaisonDeviation;

import java.util.List;

/**
 * handles the active bidding phases of power brokering
 */
public abstract class TradeBaseStep extends Step {

    protected final List<ProductLiaisonTrade> trades;
    protected final List<MyTrade> myTrades;

    /**
     *
     * @param trades the entire list of auctions that we'll be processing at some point.  For the purposes of this
     *                 phase (and most others) we're only every concerned with the first auction on this list. That's
     *                 the 'current' auction.
     * @param myTrades a list of auctions that I've started.
     * @param stepOverseer
     */
    protected TradeBaseStep(List<ProductLiaisonTrade> trades,
                            List<MyTrade> myTrades,
                            StepOverseer stepOverseer) {
        super(stepOverseer);
        this.trades = trades;
        this.myTrades = myTrades;
    }

    @Override
    public void enterStep() throws ProductLiaisonDeviation {
        super.enterStep();
        if (trades.size() == 0) {
            enterStepHandler();
        }
    }

    private void enterStepHandler() throws ProductLiaisonDeviation {
        throw new ProductLiaisonDeviation("No auctions, invalid phase");
    }

    protected ProductLiaisonTrade pullCurrentTrade() {
        return trades.get(0);
    }

    protected boolean isCurTradeMyTrade() {
        ProductLiaisonTrade curTrade = pullCurrentTrade();
        return curTrade.seller.equals(grabStepOverseer().takeMyPublicIdentity());
    }
}
