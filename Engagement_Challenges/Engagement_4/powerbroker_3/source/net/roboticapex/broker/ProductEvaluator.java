package net.roboticapex.broker;

public interface ProductEvaluator {
    /**
     * Analyzes the power profile and returns a set of actions to take
     *
     * @param schematic the profile that details our current situation
     * @return the actions to take
     */
    GenerationPlan makeGenerationPlan(ProductSchematic schematic) throws ProductLiaisonDeviation;

    BidPlan generatePromisePlan(GenerationPlan generationPlan, int budget) throws ProductLiaisonDeviation;
}
