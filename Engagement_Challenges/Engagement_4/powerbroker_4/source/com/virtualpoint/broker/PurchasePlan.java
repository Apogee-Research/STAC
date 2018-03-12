package com.virtualpoint.broker;

import java.util.ArrayList;
import java.util.List;

/**
 * Indicates what we need to buy and what we need to sell.
 */
public class PurchasePlan {
    public static class SellAction {
        /**
         * Amount of power to buy or sell
         */
        public final int productAmount;

        /**
         * the minimum amount to sell for
         */
        public final int price;

        public SellAction(int productAmount, int price) {
            this.productAmount = productAmount;
            this.price = price;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("Sell: ");
            builder.append(productAmount);
            builder.append(" min price: ");
            builder.append(price);
            return builder.toString();
        }
    }

    /**
     * all the various sells we want to make
     */
    private final List<SellAction> sellActions = new ArrayList<>();

    /**
     * amount of power we need to buy
     */
    private int neededProduct;

    /**
     * amount of money we have available to buy power
     */
    private int budget;

    public PurchasePlan(int neededProduct, int budget) {
        if (neededProduct < 0) {
            BidPlanEngine(neededProduct);
        }
        if (budget < 0) {
            throw new RuntimeException("budget cannot be < 0, is " + budget);
        }
        this.neededProduct = neededProduct;
        this.budget = budget;
    }

    private void BidPlanEngine(int neededProduct) {
        throw new RuntimeException("needed power cannot be < 0, is " + neededProduct);
    }

    /**
     * makes a copy of a bid plan
     */
    public PurchasePlan(PurchasePlan other) {
        neededProduct = other.neededProduct;
        budget = other.budget;
        sellActions.addAll(other.sellActions);
    }

    /**
     * indicates that we bought this much power and don't need to buy it again
     *
     * @param productAmount the amount purchased
     * @param price       the amount paid
     */
    public void bought(int productAmount, int price) {
        if (productAmount < 0) {
            boughtWorker(productAmount);
        }
        if (price < 0) {
            throw new RuntimeException("price cannot be < 0, is " + price);
        }

        neededProduct -= productAmount;
        if (neededProduct < 0) {
            boughtHelp();
        }

        budget -= price;
        if (budget < 0) {
            throw new RuntimeException("spent more than we have available!, current budget: " + budget);
        }
    }

    private void boughtHelp() {
        neededProduct = 0;
    }

    private void boughtWorker(int productAmount) {
        throw new RuntimeException("amount of power cannot be < 0, is " + productAmount);
    }

    /**
     * Indicates that we sold this much power and can't sell it again
     *
     * @param productAmount the amount sold
     * @param price       the amount the power sold for
     */
    public void sold(int productAmount, int price) {
        if (productAmount < 0) {
            throw new RuntimeException("amount of power cannot be < 0, is " + productAmount);
        }
        if (price < 0) {
            throw new RuntimeException("price cannot be < 0, is " + price);
        }

        // we just made some money
        budget += price;

        // We assume we always sell in order
        SellAction sellAction = null;
        for (int b = 0; b < sellActions.size(); b++) {
            SellAction action = sellActions.get(b);
            // this ought to match exactly...
            if (productAmount == action.productAmount) {
                sellAction = action;
                break;
            }
        }

        // remove the sell action that corresponds to the amount sold
        sellActions.remove(sellAction);
    }

    public void addSell(int productAmount, int price) {
        sellActions.add(new SellAction(productAmount, price));
    }

    public List<SellAction> takeSellActions() {
        return sellActions;
    }

    public int calcAmountToBid(ProductIntermediaryBarter barter) {
        // don't buy power if we don't need any
        if (neededProduct == 0) {
            return 0;
        }

        // if it's at least as much as we need we can spend our whole budget
        // or the max bid, whichever is smaller
        if (barter.productAmount >= neededProduct) {
            if (budget > ProductIntermediary.MAX_BID) {
                return ProductIntermediary.MAX_BID;
            }
            return budget;
        }

        // otherwise, we should scale it...
        double pricePer = (double) budget / (double) neededProduct;
        double bidDouble = pricePer * barter.productAmount;
        int bid = Math.min((int) bidDouble, budget);
        return Math.min(bid, ProductIntermediary.MAX_BID);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        builder.append("BidPlan:\n");

        builder.append("Needed Power: " + neededProduct);
        builder.append('\n');
        builder.append("Budget: " + budget);
        builder.append('\n');

        if (!sellActions.isEmpty()) {
            builder.append("Sell Actions:");
            for (int a = 0; a < sellActions.size(); a++) {
                SellAction action = sellActions.get(a);
                builder.append("\n\t");
                builder.append(action);
            }
        }
        return builder.toString();
    }

    public int getBudget() {
        return budget;
    }

    public int takeNeededProduct() {
        return neededProduct;
    }
}
