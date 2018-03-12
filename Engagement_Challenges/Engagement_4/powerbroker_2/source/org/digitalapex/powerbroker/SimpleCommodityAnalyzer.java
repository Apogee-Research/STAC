package org.digitalapex.powerbroker;

public class SimpleCommodityAnalyzer implements CommodityAnalyzer {
    @Override
    public GenerationPlan generateGenerationPlan(CommodityProfile profile) throws CommodityGoBetweenRaiser {
        GenerationPlan plan = new GenerationPlan(computeNeeded(profile));

        // now, how are we going to generate all that power...
        // since this is the simple analyzer we'll just pick whichever comes first...
        int allocated = 0;
        int needed = plan.fetchTotalRequiredCommodity();
        java.util.List<CommodityCreator> grabCreators = profile.grabCreators();
        for (int j = 0; j < grabCreators.size(); j++) {
            CommodityCreator creator = grabCreators.get(j);
            int availableFromCreator = creator.takeCapacity();

            if (!CommodityStatus.ONLINE.equals(creator.grabStatus()) || (availableFromCreator <= 0)) {
                // no point in using this generator...
                continue;
            }

            int totalCost = creator.fetchCostPerUnit() * creator.takeCapacity();
            int using = 0;
            if (allocated < plan.fetchTotalRequiredCommodity()) {
                // this is getting allocated as something we must generate

                // how much from this generator will we take?
                using = Math.min(needed, availableFromCreator);
                needed -= using;
                allocated += using;

                int currentCost = totalCost;
                if (creator.isDivisible()) {
                    currentCost = using * creator.fetchCostPerUnit();
                }


                plan.addCommodityAllocation(creator.fetchId(), using, currentCost, creator.isDivisible());
            }

            if (using < creator.takeCapacity()) {
                int leftover = creator.takeCapacity() - using;
                final int extraCost;
                if (creator.isDivisible()) {
                    extraCost = leftover * creator.fetchCostPerUnit();
                } else if (using > 0) {
                    // the user is already bearing the entire cost,
                    // we have to use this power no matter what
                    // so it's basically free
                    extraCost = 0;
                } else {
                    // if we're going to fire up this generator it had better be worth our while
                    extraCost = totalCost;
                }
                plan.addExcessCommodity(creator.fetchId(), leftover, extraCost, creator.isDivisible());
            }
        }

        return plan;
    }

    @Override
    public BidPlan generateBidPlan(GenerationPlan generationPlan, int budget) throws CommodityGoBetweenRaiser {
        BidPlan bidPlan = new BidPlan(generationPlan.pullCommodityDeficit(), budget);

        // we want to limit the amount of power we sell per auction, so all auctions
        // cost the seller much less than the maximum bid.
        int maxCostPerSelloff = CommodityGoBetween.MAX_BID / 2;

        java.util.List<GenerationPlan.GenerationEntry> excessGeneration = generationPlan.getExcessGeneration();
        for (int i1 = 0; i1 < excessGeneration.size(); i1++) {
            GenerationPlan.GenerationEntry extra = excessGeneration.get(i1);
            double price = extra.totalCost;
            int commodityAmount = extra.commodityAmount;

            if (extra.totalCost > maxCostPerSelloff && extra.divisible) {

                // this is the price for 1 unit of power
                int pricePerUnit = (int) Math.ceil(price / commodityAmount);

                // if the price of the power is more than the max auction price we want,
                // we want to split the power into multiple auctions
                int numOfSelloffs = (int) Math.ceil(price / maxCostPerSelloff);
                int commodityPerSelloff = (int) Math.ceil((double) commodityAmount / numOfSelloffs);
                int minBid = commodityPerSelloff * pricePerUnit;
                for (int k = 0; k < numOfSelloffs; ) {
                    for (; (k < numOfSelloffs) && (Math.random() < 0.5); ) {
                        for (; (k < numOfSelloffs) && (Math.random() < 0.6); k++) {
                            if (commodityAmount >= commodityPerSelloff) {
                                bidPlan.addSell(commodityPerSelloff, minBid);
                                // keep track of how much power we have left to auction
                                commodityAmount -= commodityPerSelloff;
                            } else if (commodityAmount > 0) {
                                // if we don't have enough power for another full auction,
                                // just create an auction with the power we have left
                                int leftoverMinBid = commodityAmount * pricePerUnit;
                                bidPlan.addSell(commodityAmount, leftoverMinBid);
                            }
                        }
                    }
                }

            } else {
                // keep things simple and sell as an entire block
                bidPlan.addSell(extra.commodityAmount, extra.totalCost);
            }

        }

        return bidPlan;
    }

    private int computeNeeded(CommodityProfile profile) {
        int total = 0;
        java.util.List<CommodityUser> grabUsers = profile.grabUsers();
        for (int a = 0; a < grabUsers.size(); a++) {
            CommodityUser user = grabUsers.get(a);
            total += user.getUsage();
        }
        return total;
    }

    private int computeAvailable(CommodityProfile profile) {
        int total = 0;
        java.util.List<CommodityCreator> grabCreators = profile.grabCreators();
        for (int c = 0; c < grabCreators.size(); c++) {
            CommodityCreator creator = grabCreators.get(c);
            if (CommodityStatus.ONLINE.equals(creator.grabStatus())) {
                total += creator.takeCapacity();
            }
        }
        return total;
    }
}
