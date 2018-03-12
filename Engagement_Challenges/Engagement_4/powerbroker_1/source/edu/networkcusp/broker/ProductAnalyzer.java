package edu.networkcusp.broker;

public interface ProductAnalyzer {
    /**
     * Analyzes the power profile and returns a set of actions to take
     *
     * @param outline the profile that details our current situation
     * @return the actions to take
     */
    GenerationPlan formGenerationPlan(ProductOutline outline) throws ProductIntermediaryRaiser;

    PurchasePlan generateOfferPlan(GenerationPlan generationPlan, int budget) throws ProductIntermediaryRaiser;
}
