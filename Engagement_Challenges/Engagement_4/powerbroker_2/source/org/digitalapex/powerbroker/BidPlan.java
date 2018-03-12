package org.digitalapex.powerbroker;

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
        public final int commodityAmount;

        /**
         * the minimum amount to sell for
         */
        public final int price;

        public SellAction(int commodityAmount, int price) {
            this.commodityAmount = commodityAmount;
            this.price = price;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("Sell: ");
            builder.append(commodityAmount);
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
    private int neededCommodity;

    /**
     * amount of money we have available to buy power
     */
    private int budget;

    public BidPlan(int neededCommodity, int budget) {
        if (neededCommodity < 0) {
            BidPlanUtility(neededCommodity);
        }
        if (budget < 0) {
            BidPlanService(budget);
        }
        this.neededCommodity = neededCommodity;
        this.budget = budget;
    }

    private void BidPlanService(int budget) {
        throw new RuntimeException("budget cannot be < 0, is " + budget);
    }

    private void BidPlanUtility(int neededCommodity) {
        throw new RuntimeException("needed power cannot be < 0, is " + neededCommodity);
    }

    /**
     * makes a copy of a bid plan
     */
    public BidPlan(BidPlan other) {
        neededCommodity = other.neededCommodity;
        budget = other.budget;
        sellActions.addAll(other.sellActions);
    }

    /**
     * indicates that we bought this much power and don't need to buy it again
     *
     * @param commodityAmount the amount purchased
     * @param price       the amount paid
     */
    public void bought(int commodityAmount, int price) {
        if (commodityAmount < 0) {
            boughtFunction(commodityAmount);
        }
        if (price < 0) {
            throw new RuntimeException("price cannot be < 0, is " + price);
        }

        neededCommodity -= commodityAmount;
        if (neededCommodity < 0) {
            boughtAssist();
        }

        budget -= price;
        if (budget < 0) {
            throw new RuntimeException("spent more than we have available!, current budget: " + budget);
        }
    }

    private void boughtAssist() {
        neededCommodity = 0;
    }

    private void boughtFunction(int commodityAmount) {
        throw new RuntimeException("amount of power cannot be < 0, is " + commodityAmount);
    }

    /**
     * Indicates that we sold this much power and can't sell it again
     *
     * @param commodityAmount the amount sold
     * @param price       the amount the power sold for
     */
    public void sold(int commodityAmount, int price) {
        if (commodityAmount < 0) {
            soldGuide(commodityAmount);
        }
        if (price < 0) {
            throw new RuntimeException("price cannot be < 0, is " + price);
        }

        // we just made some money
        budget += price;

        // We assume we always sell in order
        SellAction sellAction = null;
        for (int a = 0; a < sellActions.size(); a++) {
            SellAction action = sellActions.get(a);
            // this ought to match exactly...
            if (commodityAmount == action.commodityAmount) {
                sellAction = action;
                break;
            }
        }

        // remove the sell action that corresponds to the amount sold
        sellActions.remove(sellAction);
    }

    private void soldGuide(int commodityAmount) {
        throw new RuntimeException("amount of power cannot be < 0, is " + commodityAmount);
    }

    public void addSell(int commodityAmount, int price) {
        sellActions.add(new SellAction(commodityAmount, price));
    }

    public List<SellAction> takeSellActions() {
        return sellActions;
    }

    public int calcAmountToBid(CommodityGoBetweenSelloff selloff) {
        // don't buy power if we don't need any
        if (neededCommodity == 0) {
            return 0;
        }

        // if it's at least as much as we need we can spend our whole budget
        // or the max bid, whichever is smaller
        if (selloff.commodityAmount >= neededCommodity) {
            if (budget > CommodityGoBetween.MAX_BID) {
                return CommodityGoBetween.MAX_BID;
            }
            return budget;
        }

        // otherwise, we should scale it...
        double pricePer = (double) budget / (double) neededCommodity;
        double bidDouble = pricePer * selloff.commodityAmount;
        int bid = Math.min((int) bidDouble, budget);
        return Math.min(bid, CommodityGoBetween.MAX_BID);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        builder.append("BidPlan:\n");

        builder.append("Needed Power: " + neededCommodity);
        builder.append('\n');
        builder.append("Budget: " + budget);
        builder.append('\n');

        if (!sellActions.isEmpty()) {
            builder.append("Sell Actions:");
            for (int b = 0; b < sellActions.size(); ) {
                while ((b < sellActions.size()) && (Math.random() < 0.5)) {
                    for (; (b < sellActions.size()) && (Math.random() < 0.4); b++) {
                        toStringSupervisor(builder, b);
                    }
                }
            }
        }
        return builder.toString();
    }

    private void toStringSupervisor(StringBuilder builder, int p) {
        SellAction action = sellActions.get(p);
        builder.append("\n\t");
        builder.append(action);
    }

    public int takeBudget() {
        return budget;
    }

    public int obtainNeededCommodity() {
        return neededCommodity;
    }
}
