package net.roboticapex.broker.period;

import net.roboticapex.broker.BidPlan;
import net.roboticapex.broker.MyTrade;
import net.roboticapex.broker.ProductLiaisonTrade;
import net.roboticapex.broker.selloff.TradeAdapter;

import java.util.List;

public class TradeEndStepBuilder {
    private StepOverseer stepOverseer;
    private int myPromise;
    private BidPlan promisePlan;
    private TradeAdapter tradeAdapter;
    private List<ProductLiaisonTrade> trades;
    private List<MyTrade> myTrades;

    public TradeEndStepBuilder setStepOverseer(StepOverseer stepOverseer) {
        this.stepOverseer = stepOverseer;
        return this;
    }

    public TradeEndStepBuilder fixMyPromise(int myPromise) {
        this.myPromise = myPromise;
        return this;
    }

    public TradeEndStepBuilder definePromisePlan(BidPlan promisePlan) {
        this.promisePlan = promisePlan;
        return this;
    }

    public TradeEndStepBuilder defineTradeAdapter(TradeAdapter tradeAdapter) {
        this.tradeAdapter = tradeAdapter;
        return this;
    }

    public TradeEndStepBuilder defineTrades(List<ProductLiaisonTrade> trades) {
        this.trades = trades;
        return this;
    }

    public TradeEndStepBuilder fixMyTrades(List<MyTrade> myTrades) {
        this.myTrades = myTrades;
        return this;
    }

    public TradeEndStep makeTradeEndStep() {
        return new TradeEndStep(trades, myTrades, promisePlan, tradeAdapter, myPromise, stepOverseer);
    }
}