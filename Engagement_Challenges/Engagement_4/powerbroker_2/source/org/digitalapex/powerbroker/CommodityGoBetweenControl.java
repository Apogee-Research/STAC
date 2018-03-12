package org.digitalapex.powerbroker;

import org.digitalapex.talkers.TalkersRaiser;
import org.digitalapex.head.Control;

import java.io.IOException;

public class CommodityGoBetweenControl implements CommodityGoBetweenUser {
    private static final String PROMPT = "PowerBroker";
    private final CommodityGoBetween commodityGoBetween;
    private final Control control;

    public CommodityGoBetweenControl(CommodityGoBetween commodityGoBetween) throws IOException {
        this.commodityGoBetween = commodityGoBetween;

        control = new Control(PROMPT);

        prepControl(control);
    }

    private void prepControl(Control control) {
        control.addCommand(new BeginCommandBuilder().assignCommodityGoBetween(commodityGoBetween).generateBeginCommand());
    }

    public void run() throws IOException, TalkersRaiser {
        try {
            control.execute();
        } finally {
            commodityGoBetween.stop();
        }
    }

    @Override
    public void selloffSequenceComplete(BidPlan bidPlan) {
        control.stashLine();
        control.pullOutputStream().println("Done processing power profile\n" + "Ended with budget: " + bidPlan.takeBudget()
                + " and needed power: " + bidPlan.obtainNeededCommodity());
        control.unstashLine();
    }

    @Override
    public void selloffStarted(String id, int amount) {
        control.stashLine();
        control.pullOutputStream().println("Auction started: " + id);
        control.unstashLine();
    }

    @Override
    public void selloffEnded(String id, int commodityAmount) {
        control.stashLine();
        control.pullOutputStream().println("Auction ended: " + id);
        control.unstashLine();
    }

    @Override
    public void soldCommodity(int commodityAmount, int bid, String to) {
        control.stashLine();
        if (to.equals(commodityGoBetween.getIdentity().pullId())) {
            soldCommodityFunction(commodityAmount);
        } else {
            control.pullOutputStream().println("Sold " + commodityAmount + " power for " + bid + " to " + to);
        }
        control.unstashLine();
    }

    private void soldCommodityFunction(int commodityAmount) {
        control.pullOutputStream().println("Unable to sell " + commodityAmount + " reserve not met");
    }

    @Override
    public void boughtCommodity(int commodityAmount, int bid, String from) {
        control.stashLine();
        control.pullOutputStream().println("Bought " + commodityAmount + " power for " + bid + " from " + from);
        control.unstashLine();
    }

    @Override
    public void bidding(int commodityAmount, int bid) {
        control.stashLine();
        control.pullOutputStream().println("Bidding " + bid + " on " + commodityAmount);
        control.unstashLine();
    }

    @Override
    public void resultsCalculated(BidPlan currentBidPlan) {
        control.stashLine();
        control.pullOutputStream().println("Power still needed: " + currentBidPlan.obtainNeededCommodity() + "\nCurrent budget: "
                + currentBidPlan.takeBudget());
        control.unstashLine();
    }

    public void disconnectedFromAllUsers() {
        control.stashLine();
        control.pullOutputStream().println("Disconnected from all users");
        control.unstashLine();
    }
}
