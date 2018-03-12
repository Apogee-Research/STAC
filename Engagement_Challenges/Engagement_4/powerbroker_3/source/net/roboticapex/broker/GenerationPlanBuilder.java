package net.roboticapex.broker;

public class GenerationPlanBuilder {
    private int totalRequiredProduct;

    public GenerationPlanBuilder fixTotalRequiredProduct(int totalRequiredProduct) {
        this.totalRequiredProduct = totalRequiredProduct;
        return this;
    }

    public GenerationPlan makeGenerationPlan() {
        return new GenerationPlan(totalRequiredProduct);
    }
}