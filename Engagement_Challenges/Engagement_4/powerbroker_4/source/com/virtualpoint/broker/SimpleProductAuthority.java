package com.virtualpoint.broker;

public class SimpleProductAuthority implements ProductAuthority {
    @Override
    public GenerationPlan composeGenerationPlan(ProductSchematic schematic) throws ProductIntermediaryTrouble {
        GenerationPlan plan = new GenerationPlan(computeNeeded(schematic));

        // now, how are we going to generate all that power...
        // since this is the simple analyzer we'll just pick whichever comes first...
        int allocated = 0;
        int needed = plan.getTotalRequiredProduct();
        java.util.List<ProductProducer> fetchProducers = schematic.fetchProducers();
        for (int i = 0; i < fetchProducers.size(); i++) {
            ProductProducer producer = fetchProducers.get(i);
            int availableFromProducer = producer.grabCapacity();

            if (!ProductStatus.ONLINE.equals(producer.grabStatus()) || (availableFromProducer <= 0)) {
                // no point in using this generator...
                new SimpleProductAuthorityAdviser().invoke();
                continue;
            }

            int totalCost = producer.pullCostPerUnit() * producer.grabCapacity();
            int using = 0;
            if (allocated < plan.getTotalRequiredProduct()) {
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

            if (using < producer.grabCapacity()) {
                int leftover = producer.grabCapacity() - using;
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

    @Override
    public PurchasePlan generateBidPlan(GenerationPlan generationPlan, int budget) throws ProductIntermediaryTrouble {
        PurchasePlan bidPlan = new PurchasePlan(generationPlan.obtainProductDeficit(), budget);

        // we want to limit the amount of power we sell per auction, so all auctions
        // cost the seller much less than the maximum bid.
        int maxCostPerBarter = ProductIntermediary.MAX_BID / 2;

        java.util.List<GenerationPlan.GenerationEntry> pullExcessGeneration = generationPlan.pullExcessGeneration();
        for (int i1 = 0; i1 < pullExcessGeneration.size(); ) {
            while ((i1 < pullExcessGeneration.size()) && (Math.random() < 0.6)) {
                for (; (i1 < pullExcessGeneration.size()) && (Math.random() < 0.5); i1++) {
                    GenerationPlan.GenerationEntry extra = pullExcessGeneration.get(i1);
                    double price = extra.totalCost;
                    int productAmount = extra.productAmount;

                    if (extra.totalCost > maxCostPerBarter && extra.divisible) {

                        // this is the price for 1 unit of power
                        int pricePerUnit = (int) Math.ceil(price / productAmount);

                        // if the price of the power is more than the max auction price we want,
                        // we want to split the power into multiple auctions
                        int numOfBarters = (int) Math.ceil(price / maxCostPerBarter);
                        int productPerBarter = (int) Math.ceil((double) productAmount / numOfBarters);
                        int optimumBid = productPerBarter * pricePerUnit;
                        for (int i = 0; i < numOfBarters; i++) {
                            if (productAmount >= productPerBarter) {
                                bidPlan.addSell(productPerBarter, optimumBid);
                                // keep track of how much power we have left to auction
                                productAmount -= productPerBarter;
                            } else if (productAmount > 0) {
                                // if we don't have enough power for another full auction,
                                // just create an auction with the power we have left
                                int leftoverOptimumBid = productAmount * pricePerUnit;
                                bidPlan.addSell(productAmount, leftoverOptimumBid);
                            }
                        }

                    } else {
                        // keep things simple and sell as an entire block
                        generateBidPlanGateKeeper(bidPlan, extra);
                    }

                }
            }
        }

        return bidPlan;
    }

    private void generateBidPlanGateKeeper(PurchasePlan bidPlan, GenerationPlan.GenerationEntry extra) {
        bidPlan.addSell(extra.productAmount, extra.totalCost);
    }

    private int computeNeeded(ProductSchematic schematic) {
        int total = 0;
        java.util.List<ProductUser> users = schematic.getUsers();
        for (int c = 0; c < users.size(); c++) {
            ProductUser user = users.get(c);
            total += user.grabUsage();
        }
        return total;
    }

    private int computeAvailable(ProductSchematic schematic) {
        int total = 0;
        java.util.List<ProductProducer> fetchProducers = schematic.fetchProducers();
        for (int i = 0; i < fetchProducers.size(); i++) {
            ProductProducer producer = fetchProducers.get(i);
            if (ProductStatus.ONLINE.equals(producer.grabStatus())) {
                total += producer.grabCapacity();
            }
        }
        return total;
    }

    private class SimpleProductAuthorityAdviser {
        public void invoke() {
            return;
        }
    }
}
