package net.roboticapex.broker;

public class SimpleProductEvaluator implements ProductEvaluator {
    @Override
    public GenerationPlan makeGenerationPlan(ProductSchematic schematic) throws ProductLiaisonDeviation {
        GenerationPlan plan = new GenerationPlanBuilder().fixTotalRequiredProduct(computeNeeded(schematic)).makeGenerationPlan();

        // now, how are we going to generate all that power...
        // since this is the simple analyzer we'll just pick whichever comes first...
        int allocated = 0;
        int needed = plan.grabTotalRequiredProduct();
        java.util.List<ProductProducer> grabProducers = schematic.grabProducers();
        for (int q = 0; q < grabProducers.size(); q++) {
            ProductProducer producer = grabProducers.get(q);
            int availableFromProducer = producer.pullAccommodation();

            if (!ProductStatus.ONLINE.equals(producer.fetchStatus()) || (availableFromProducer <= 0)) {
                // no point in using this generator...
                makeGenerationPlanEntity();
                continue;
            }

            int totalCost = producer.pullCostPerUnit() * producer.pullAccommodation();
            int using = 0;
            if (allocated < plan.grabTotalRequiredProduct()) {
                // this is getting allocated as something we must generate

                // how much from this generator will we take?
                using = Math.min(needed, availableFromProducer);
                needed -= using;
                allocated += using;

                int currentCost = totalCost;
                if (producer.isDivisible()) {
                    currentCost = using * producer.pullCostPerUnit();
                }


                plan.addProductAllocation(producer.takeId(), using, currentCost, producer.isDivisible());
            }

            if (using < producer.pullAccommodation()) {
                int leftover = producer.pullAccommodation() - using;
                final int extraCost;
                if (producer.isDivisible()) {
                    extraCost = leftover * producer.pullCostPerUnit();
                } else if (using > 0) {
                    // the user is already bearing the entire cost,
                    // we have to use this power no matter what
                    // so it's basically free
                    extraCost = 0;
                } else {
                    // if we're going to fire up this generator it had better be worth our while
                    extraCost = totalCost;
                }
                plan.addExcessProduct(producer.takeId(), leftover, extraCost, producer.isDivisible());
            }
        }

        return plan;
    }

    private void makeGenerationPlanEntity() {
        return;
    }

    @Override
    public BidPlan generatePromisePlan(GenerationPlan generationPlan, int budget) throws ProductLiaisonDeviation {
        BidPlan promisePlan = new BidPlan(generationPlan.takeProductDeficit(), budget);

        // we want to limit the amount of power we sell per auction, so all auctions
        // cost the seller much less than the maximum bid.
        int maxCostPerTrade = ProductLiaison.MAX_BID / 2;

        java.util.List<GenerationPlan.GenerationEntry> pullExcessGeneration = generationPlan.pullExcessGeneration();
        for (int i1 = 0; i1 < pullExcessGeneration.size(); ) {
            while ((i1 < pullExcessGeneration.size()) && (Math.random() < 0.5)) {
                for (; (i1 < pullExcessGeneration.size()) && (Math.random() < 0.4); i1++) {
                    GenerationPlan.GenerationEntry extra = pullExcessGeneration.get(i1);
                    double price = extra.totalCost;
                    int productAmount = extra.productAmount;

                    if (extra.totalCost > maxCostPerTrade && extra.divisible) {

                        // this is the price for 1 unit of power
                        int pricePerUnit = (int) Math.ceil(price / productAmount);

                        // if the price of the power is more than the max auction price we want,
                        // we want to split the power into multiple auctions
                        int numOfTrades = (int) Math.ceil(price / maxCostPerTrade);
                        int productPerTrade = (int) Math.ceil((double) productAmount / numOfTrades);
                        int optimumPromise = productPerTrade * pricePerUnit;
                        for (int a = 0; a < numOfTrades; a++) {
                            if (productAmount >= productPerTrade) {
                                promisePlan.addSell(productPerTrade, optimumPromise);
                                // keep track of how much power we have left to auction
                                productAmount -= productPerTrade;
                            } else if (productAmount > 0) {
                                // if we don't have enough power for another full auction,
                                // just create an auction with the power we have left
                                int leftoverOptimumPromise = productAmount * pricePerUnit;
                                promisePlan.addSell(productAmount, leftoverOptimumPromise);
                            }
                        }

                    } else {
                        // keep things simple and sell as an entire block
                        promisePlan.addSell(extra.productAmount, extra.totalCost);
                    }

                }
            }
        }

        return promisePlan;
    }

    private int computeNeeded(ProductSchematic schematic) {
        int total = 0;
        java.util.List<ProductUser> obtainUsers = schematic.obtainUsers();
        for (int i = 0; i < obtainUsers.size(); i++) {
            ProductUser user = obtainUsers.get(i);
            total += user.getUsage();
        }
        return total;
    }

    private int computeAvailable(ProductSchematic schematic) {
        int total = 0;
        java.util.List<ProductProducer> grabProducers = schematic.grabProducers();
        for (int c = 0; c < grabProducers.size(); c++) {
            ProductProducer producer = grabProducers.get(c);
            if (ProductStatus.ONLINE.equals(producer.fetchStatus())) {
                total += producer.pullAccommodation();
            }
        }
        return total;
    }
}
