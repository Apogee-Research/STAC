package com.virtualpoint.broker;

import com.virtualpoint.talkers.DialogsTrouble;
import com.virtualpoint.console.Display;

import java.io.IOException;

public class ProductIntermediaryDisplay implements ProductIntermediaryUser {
    private static final String PROMPT = "PowerBroker";
    private final ProductIntermediary productIntermediary;
    private final Display display;

    public ProductIntermediaryDisplay(ProductIntermediary productIntermediary) throws IOException {
        this.productIntermediary = productIntermediary;

        display = new Display(PROMPT);

        prepDisplay(display);
    }

    private void prepDisplay(Display display) {
        display.addCommand(new BeginCommand(productIntermediary));
    }

    public void run() throws IOException, DialogsTrouble {
        try {
            display.execute();
        } finally {
            productIntermediary.stop();
        }
    }

    @Override
    public void barterSequenceComplete(PurchasePlan bidPlan) {
        display.stashLine();
        display.getOutputStream().println("Done processing power profile\n" + "Ended with budget: " + bidPlan.getBudget()
                + " and needed power: " + bidPlan.takeNeededProduct());
        display.unstashLine();
    }

    @Override
    public void barterStarted(String id, int amount) {
        display.stashLine();
        display.getOutputStream().println("Auction started: " + id);
        display.unstashLine();
    }

    @Override
    public void barterEnded(String id, int productAmount) {
        display.stashLine();
        display.getOutputStream().println("Auction ended: " + id);
        display.unstashLine();
    }

    @Override
    public void soldProduct(int productAmount, int bid, String to) {
        display.stashLine();
        if (to.equals(productIntermediary.getIdentity().grabId())) {
            display.getOutputStream().println("Unable to sell " + productAmount + " reserve not met");
        } else {
            soldProductEngine(productAmount, bid, to);
        }
        display.unstashLine();
    }

    private void soldProductEngine(int productAmount, int bid, String to) {
        display.getOutputStream().println("Sold " + productAmount + " power for " + bid + " to " + to);
    }

    @Override
    public void boughtProduct(int productAmount, int bid, String from) {
        display.stashLine();
        display.getOutputStream().println("Bought " + productAmount + " power for " + bid + " from " + from);
        display.unstashLine();
    }

    @Override
    public void bidding(int productAmount, int bid) {
        display.stashLine();
        display.getOutputStream().println("Bidding " + bid + " on " + productAmount);
        display.unstashLine();
    }

    @Override
    public void resultsCalculated(PurchasePlan currentBidPlan) {
        display.stashLine();
        display.getOutputStream().println("Power still needed: " + currentBidPlan.takeNeededProduct() + "\nCurrent budget: "
                + currentBidPlan.getBudget());
        display.unstashLine();
    }

    public void disconnectedFromAllUsers() {
        display.stashLine();
        display.getOutputStream().println("Disconnected from all users");
        display.unstashLine();
    }
}
