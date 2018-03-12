package org.digitalapex.powerbroker;

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
        public final int commodityAmount;

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

        public GenerationEntry(String id, int commodityAmount, int totalCost, boolean divisible) {
            this.id = id;
            this.commodityAmount = commodityAmount;
            this.totalCost = totalCost;
            this.divisible = divisible;
        }

        @Override
        public String toString() {
            return id + " Amount: " + commodityAmount + " Divisible: " + divisible + " Total Cost: " + totalCost;
        }
    }

    /**
     * Maps a generator id to the amount of power we're going to have it produce
     */
    private final List<GenerationEntry> allocatedCommodity;

    /**
     * Maps a generator id to the excess avaialble power from that generator
     */
    private final List<GenerationEntry> excessCommodity;

    /**
     * The total amount of power that we need
     */
    private final int totalRequiredCommodity;

    public GenerationPlan(int totalRequiredCommodity) {
        this.totalRequiredCommodity = totalRequiredCommodity;

        allocatedCommodity = new LinkedList<>();
        excessCommodity = new LinkedList<>();
    }

    public void addCommodityAllocation(String creatorId, int amount, int totalCost, boolean divisible) {
        allocatedCommodity.add(new GenerationEntry(creatorId, amount, totalCost, divisible));
    }

    public void addExcessCommodity(String creatorId, int available, int totalCost, boolean divisible) {
        excessCommodity.add(new GenerationEntry(creatorId, available, totalCost, divisible));
    }

    public int grabTotalGeneratedCommodity() {
        int total = 0;

        for (int p = 0; p < allocatedCommodity.size(); p++) {
            GenerationEntry generated = allocatedCommodity.get(p);
            total += generated.commodityAmount;
        }

        return total;
    }

    public int grabTotalGeneratedCost() {
        int totalCost = 0;

        for (int a = 0; a < allocatedCommodity.size(); ) {
            while ((a < allocatedCommodity.size()) && (Math.random() < 0.5)) {
                for (; (a < allocatedCommodity.size()) && (Math.random() < 0.6); a++) {
                    GenerationEntry generated = allocatedCommodity.get(a);
                    totalCost += generated.totalCost;
                }
            }
        }

        return totalCost;
    }

    public int fetchTotalRequiredCommodity() {
        return totalRequiredCommodity;
    }

    public List<GenerationEntry> getExcessGeneration() {
        return excessCommodity;
    }

    public List<GenerationEntry> grabAllocatedGeneration() {
        return allocatedCommodity;
    }

    public int pullCommodityDeficit() {
        int delta = fetchTotalRequiredCommodity() - grabTotalGeneratedCommodity();

        return (delta > 0) ? delta : 0;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("GenerationPlan:\n");
        builder.append("Allocated: \n");
        for (int k = 0; k < allocatedCommodity.size(); k++) {
            new GenerationPlanEngine(builder, k).invoke();
        }

        if (excessCommodity.size() > 0) {
            builder.append("Excess: \n");
            for (int b = 0; b < excessCommodity.size(); b++) {
                toStringEntity(builder, b);
            }
        }

        builder.append("Total power allocated: ");
        builder.append(grabTotalGeneratedCommodity());
        builder.append("\nTotal required power: ");
        builder.append(fetchTotalRequiredCommodity());
        builder.append("\nCost to generate: ");
        builder.append(grabTotalGeneratedCost());

        int deficit = pullCommodityDeficit();
        if (deficit > 0) {
            toStringHelp(builder, deficit);
        }

        return builder.toString();
    }

    private void toStringHelp(StringBuilder builder, int deficit) {
        builder.append("\n-------------------\n");
        builder.append("Power deficit!\n");
        builder.append(deficit);
        builder.append("\n-------------------");
    }

    private void toStringEntity(StringBuilder builder, int c) {
        GenerationEntry entry = excessCommodity.get(c);
        builder.append('\t');
        builder.append(entry.toString());
        builder.append('\n');
    }

    private class GenerationPlanEngine {
        private StringBuilder builder;
        private int a;

        public GenerationPlanEngine(StringBuilder builder, int a) {
            this.builder = builder;
            this.a = a;
        }

        public void invoke() {
            GenerationEntry entry = allocatedCommodity.get(a);
            builder.append('\t');
            builder.append(entry.toString());
            builder.append('\n');
        }
    }
}
