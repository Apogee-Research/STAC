package org.digitalapex.powerbroker;

public interface CommodityAnalyzer {
    /**
     * Analyzes the power profile and returns a set of actions to take
     *
     * @param profile the profile that details our current situation
     * @return the actions to take
     */
    GenerationPlan generateGenerationPlan(CommodityProfile profile) throws CommodityGoBetweenRaiser;

    BidPlan generateBidPlan(GenerationPlan generationPlan, int budget) throws CommodityGoBetweenRaiser;
}
