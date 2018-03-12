package com.virtualpoint.broker;

public interface ProductAuthority {
    /**
     * Analyzes the power profile and returns a set of actions to take
     *
     * @param schematic the profile that details our current situation
     * @return the actions to take
     */
    GenerationPlan composeGenerationPlan(ProductSchematic schematic) throws ProductIntermediaryTrouble;

    PurchasePlan generateBidPlan(GenerationPlan generationPlan, int budget) throws ProductIntermediaryTrouble;
}
