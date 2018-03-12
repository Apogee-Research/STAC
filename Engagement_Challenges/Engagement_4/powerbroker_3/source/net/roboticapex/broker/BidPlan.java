package net.roboticapex.broker;

import java.util.ArrayList;
import java.util.List;

/**
 * Indicates what we need to buy and what we need to sell.
 */
public class BidPlan {
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

    public BidPlan(int neededProduct, int budget) {
        if (neededProduct < 0) {
            PromisePlanFunction(neededProduct);
        }
        if (budget < 0) {
            throw new RuntimeException("budget cannot be < 0, is " + budget);
        }
        this.neededProduct = neededProduct;
        this.budget = budget;
    }

    private void PromisePlanFunction(int neededProduct) {
        throw new RuntimeException("needed power cannot be < 0, is " + neededProduct);
    }

    /**
     * makes a copy of a bid plan
     */
    public BidPlan(BidPlan other) {
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
            boughtSupervisor(productAmount);
        }
        if (price < 0) {
            throw new RuntimeException("price cannot be < 0, is " + price);
        }

        neededProduct -= productAmount;
        if (neededProduct < 0) {
            neededProduct = 0;
        }

        budget -= price;
        if (budget < 0) {
            throw new RuntimeException("spent more than we have available!, current budget: " + budget);
        }
    }

    private void boughtSupervisor(int productAmount) {
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
            soldGuide(price);
        }

        // we just made some money
        budget += price;

        // We assume we always sell in order
        SellAction sellAction = null;
        for (int p = 0; p < sellActions.size(); p++) {
            SellAction action = sellActions.get(p);
            // this ought to match exactly...
            if (productAmount == action.productAmount) {
                sellAction = action;
                break;
            }
        }

        // remove the sell action that corresponds to the amount sold
        sellActions.remove(sellAction);
    }

    private void soldGuide(int price) {
        throw new RuntimeException("price cannot be < 0, is " + price);
    }

    public void addSell(int productAmount, int price) {
        sellActions.add(new SellAction(productAmount, price));
    }

    public List<SellAction> fetchSellActions() {
        return sellActions;
    }

    public int calcAmountToPromise(ProductLiaisonTrade trade) {
        // don't buy power if we don't need any
        if (neededProduct == 0) {
            return 0;
        }

        // if it's at least as much as we need we can spend our whole budget
        // or the max bid, whichever is smaller
        if (trade.productAmount >= neededProduct) {
            if (budget > ProductLiaison.MAX_BID) {
                return ProductLiaison.MAX_BID;
            }
            return budget;
        }

        // otherwise, we should scale it...
        double pricePer = (double) budget / (double) neededProduct;
        double promiseDouble = pricePer * trade.productAmount;
        int promise = Math.min((int) promiseDouble, budget);
        return Math.min(promise, ProductLiaison.MAX_BID);
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
            for (int p = 0; p < sellActions.size(); p++) {
                SellAction action = sellActions.get(p);
                builder.append("\n\t");
                builder.append(action);
            }
        }
        return builder.toString();
    }

    public int obtainBudget() {
        return budget;
    }

    public int obtainNeededProduct() {
        return neededProduct;
    }
}
