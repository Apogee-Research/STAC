package net.roboticapex.broker;

import net.roboticapex.senderReceivers.SenderReceiversDeviation;
import net.roboticapex.control.Display;

import java.io.IOException;

public class ProductLiaisonDisplay implements ProductLiaisonUser {
    private static final String PROMPT = "PowerBroker";
    private final ProductLiaison productLiaison;
    private final Display display;

    public ProductLiaisonDisplay(ProductLiaison productLiaison) throws IOException {
        this.productLiaison = productLiaison;

        display = new Display(PROMPT);

        prepDisplay(display);
    }

    private void prepDisplay(Display display) {
        display.addCommand(new BeginCommand(productLiaison));
    }

    public void run() throws IOException, SenderReceiversDeviation {
        try {
            display.execute();
        } finally {
            productLiaison.stop();
        }
    }

    @Override
    public void tradeSequenceComplete(BidPlan promisePlan) {
        display.stashLine();
        display.takeOutputStream().println("Done processing power profile\n" + "Ended with budget: " + promisePlan.obtainBudget()
                + " and needed power: " + promisePlan.obtainNeededProduct());
        display.unstashLine();
    }

    @Override
    public void tradeStarted(String id, int amount) {
        display.stashLine();
        display.takeOutputStream().println("Auction started: " + id);
        display.unstashLine();
    }

    @Override
    public void tradeEnded(String id, int productAmount) {
        display.stashLine();
        display.takeOutputStream().println("Auction ended: " + id);
        display.unstashLine();
    }

    @Override
    public void soldProduct(int productAmount, int promise, String to) {
        display.stashLine();
        if (to.equals(productLiaison.fetchIdentity().obtainId())) {
            soldProductHelper(productAmount);
        } else {
            display.takeOutputStream().println("Sold " + productAmount + " power for " + promise + " to " + to);
        }
        display.unstashLine();
    }

    private void soldProductHelper(int productAmount) {
        display.takeOutputStream().println("Unable to sell " + productAmount + " reserve not met");
    }

    @Override
    public void boughtProduct(int productAmount, int promise, String from) {
        display.stashLine();
        display.takeOutputStream().println("Bought " + productAmount + " power for " + promise + " from " + from);
        display.unstashLine();
    }

    @Override
    public void bidding(int productAmount, int promise) {
        display.stashLine();
        display.takeOutputStream().println("Bidding " + promise + " on " + productAmount);
        display.unstashLine();
    }

    @Override
    public void resultsCalculated(BidPlan currentPromisePlan) {
        display.stashLine();
        display.takeOutputStream().println("Power still needed: " + currentPromisePlan.obtainNeededProduct() + "\nCurrent budget: "
                + currentPromisePlan.obtainBudget());
        display.unstashLine();
    }

    public void disconnectedFromAllUsers() {
        display.stashLine();
        display.takeOutputStream().println("Disconnected from all users");
        display.unstashLine();
    }
}
