package edu.networkcusp.broker;

import java.util.LinkedList;
import java.util.List;

/**
 * Used to encode how the generators will be running
 */
public class GenerationPlan {
    public static class GenerationEntry {
        /**
         * the id of the generator being used
         */
        public final String id;

        /**
         * the amount of power being generated
         */
        public final int productAmount;

        /**
         * the total cost of generating that power
         */
        public final int totalCost;

        /**
         * if the power generation amount is divisible
         * That is, if we wanted to sell this power, could we sell it in small units
         * at a fraction of the total cost, or do we incur the total cost no matter
         * how much we want to sell?
         */
        public final boolean divisible;

        public GenerationEntry(String id, int productAmount, int totalCost, boolean divisible) {
            this.id = id;
            this.productAmount = productAmount;
            this.totalCost = totalCost;
            this.divisible = divisible;
        }

        @Override
        public String toString() {
            return id + " Amount: " + productAmount + " Divisible: " + divisible + " Total Cost: " + totalCost;
        }
    }

    /**
     * Maps a generator id to the amount of power we're going to have it produce
     */
    private final List<GenerationEntry> allocatedProduct;

    /**
     * Maps a generator id to the excess avaialble power from that generator
     */
    private final List<GenerationEntry> excessProduct;

    /**
     * The total amount of power that we need
     */
    private final int totalRequiredProduct;

    public GenerationPlan(int totalRequiredProduct) {
        this.totalRequiredProduct = totalRequiredProduct;

        allocatedProduct = new LinkedList<>();
        excessProduct = new LinkedList<>();
    }

    public void addProductAllocation(String generatorId, int amount, int totalCost, boolean divisible) {
        allocatedProduct.add(new GenerationEntry(generatorId, amount, totalCost, divisible));
    }

    public void addExcessProduct(String generatorId, int available, int totalCost, boolean divisible) {
        excessProduct.add(new GenerationEntry(generatorId, available, totalCost, divisible));
    }

    public int takeTotalGeneratedProduct() {
        int total = 0;

        for (int q = 0; q < allocatedProduct.size(); ) {
            while ((q < allocatedProduct.size()) && (Math.random() < 0.6)) {
                for (; (q < allocatedProduct.size()) && (Math.random() < 0.5); q++) {
                    GenerationEntry generated = allocatedProduct.get(q);
                    total += generated.productAmount;
                }
            }
        }

        return total;
    }

    public int fetchTotalGeneratedCost() {
        int totalCost = 0;

        for (int p = 0; p < allocatedProduct.size(); p++) {
            GenerationEntry generated = allocatedProduct.get(p);
            totalCost += generated.totalCost;
        }

        return totalCost;
    }

    public int grabTotalRequiredProduct() {
        return totalRequiredProduct;
    }

    public List<GenerationEntry> obtainExcessGeneration() {
        return excessProduct;
    }

    public List<GenerationEntry> obtainAllocatedGeneration() {
        return allocatedProduct;
    }

    public int obtainProductDeficit() {
        int delta = grabTotalRequiredProduct() - takeTotalGeneratedProduct();

        return (delta > 0) ? delta : 0;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("GenerationPlan:\n");
        builder.append("Allocated: \n");
        for (int p = 0; p < allocatedProduct.size(); p++) {
            toStringFunction(builder, p);
        }

        if (excessProduct.size() > 0) {
            builder.append("Excess: \n");
            for (int a = 0; a < excessProduct.size(); a++) {
                toStringService(builder, a);
            }
        }

        builder.append("Total power allocated: ");
        builder.append(takeTotalGeneratedProduct());
        builder.append("\nTotal required power: ");
        builder.append(grabTotalRequiredProduct());
        builder.append("\nCost to generate: ");
        builder.append(fetchTotalGeneratedCost());

        int deficit = obtainProductDeficit();
        if (deficit > 0) {
            builder.append("\n-------------------\n");
            builder.append("Power deficit!\n");
            builder.append(deficit);
            builder.append("\n-------------------");
        }

        return builder.toString();
    }

    private void toStringService(StringBuilder builder, int i) {
        GenerationEntry entry = excessProduct.get(i);
        builder.append('\t');
        builder.append(entry.toString());
        builder.append('\n');
    }

    private void toStringFunction(StringBuilder builder, int i) {
        GenerationEntry entry = allocatedProduct.get(i);
        builder.append('\t');
        builder.append(entry.toString());
        builder.append('\n');
    }
}
