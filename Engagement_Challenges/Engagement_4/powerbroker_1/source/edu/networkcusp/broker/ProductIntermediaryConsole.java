package edu.networkcusp.broker;

import edu.networkcusp.senderReceivers.ProtocolsRaiser;
import edu.networkcusp.console.Console;

import java.io.IOException;

public class ProductIntermediaryConsole implements ProductIntermediaryCustomer {
    private static final String PROMPT = "PowerBroker";
    private final ProductIntermediary productIntermediary;
    private final Console console;

    public ProductIntermediaryConsole(ProductIntermediary productIntermediary) throws IOException {
        this.productIntermediary = productIntermediary;

        console = new Console(PROMPT);

        prepConsole(console);
    }

    private void prepConsole(Console console) {
        console.addCommand(new BeginCommand(productIntermediary));
    }

    public void run() throws IOException, ProtocolsRaiser {
        try {
            console.execute();
        } finally {
            productIntermediary.stop();
        }
    }

    @Override
    public void auctionSequenceComplete(PurchasePlan offerPlan) {
        console.stashLine();
        console.fetchOutputStream().println("Done processing power profile\n" + "Ended with budget: " + offerPlan.obtainBudget()
                + " and needed power: " + offerPlan.grabNeededProduct());
        console.unstashLine();
    }

    @Override
    public void auctionStarted(String id, int amount) {
        console.stashLine();
        console.fetchOutputStream().println("Auction started: " + id);
        console.unstashLine();
    }

    @Override
    public void auctionEnded(String id, int productAmount) {
        console.stashLine();
        console.fetchOutputStream().println("Auction ended: " + id);
        console.unstashLine();
    }

    @Override
    public void soldProduct(int productAmount, int offer, String to) {
        console.stashLine();
        if (to.equals(productIntermediary.takeIdentity().pullId())) {
            console.fetchOutputStream().println("Unable to sell " + productAmount + " reserve not met");
        } else {
            new ProductIntermediaryConsoleCoordinator(productAmount, offer, to).invoke();
        }
        console.unstashLine();
    }

    @Override
    public void boughtProduct(int productAmount, int offer, String from) {
        console.stashLine();
        console.fetchOutputStream().println("Bought " + productAmount + " power for " + offer + " from " + from);
        console.unstashLine();
    }

    @Override
    public void bidding(int productAmount, int offer) {
        console.stashLine();
        console.fetchOutputStream().println("Bidding " + offer + " on " + productAmount);
        console.unstashLine();
    }

    @Override
    public void resultsCalculated(PurchasePlan currentOfferPlan) {
        console.stashLine();
        console.fetchOutputStream().println("Power still needed: " + currentOfferPlan.grabNeededProduct() + "\nCurrent budget: "
                + currentOfferPlan.obtainBudget());
        console.unstashLine();
    }

    public void disconnectedFromAllCustomers() {
        console.stashLine();
        console.fetchOutputStream().println("Disconnected from all users");
        console.unstashLine();
    }

    private class ProductIntermediaryConsoleCoordinator {
        private int productAmount;
        private int offer;
        private String to;

        public ProductIntermediaryConsoleCoordinator(int productAmount, int offer, String to) {
            this.productAmount = productAmount;
            this.offer = offer;
            this.to = to;
        }

        public void invoke() {
            console.fetchOutputStream().println("Sold " + productAmount + " power for " + offer + " to " + to);
        }
    }
}
