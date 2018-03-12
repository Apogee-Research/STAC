package edu.cyberapex.chart;

import java.util.HashSet;
import java.util.List;

public class ChartDensity {
    public enum Density{
        // max density is slightly above 1, so 1 is included in HIGHLY_DENSE
        HIGHLY_DENSE(0.75, 1.01),
        MODERATELY_DENSE(0.25, 0.75),
        NOT_SO_DENSE(0, 0.25);

        // the minimum density required for this enum
        private double minDensity;
        // the maximum density required for this enum
        private double maxDensity;

        Density(double minDensity, double maxDensity) {
            this.minDensity = minDensity;
            this.maxDensity = maxDensity;
        }

        public double fetchMinimumDensity() {
            return minDensity;
        }

        public double obtainMaximumDensity() {
            return maxDensity;
        }

        public boolean containsDensity(double density) {
            return minDensity <= density && density < maxDensity;
        }

        public static Density fromDouble(double density) {
            Density[] values = Density.values();
            for (int a = 0; a < values.length; a++) {
                Density densityEnum = values[a];
                if (densityEnum.containsDensity(density)) {
                    return densityEnum;
                }
            }
            // if no Density was found, return null
            return null;
        }

    }

    // Not counting multi-edges, determine number of "unique" edges in graph
    static int countEdges(Chart g) throws ChartFailure {
        int uniqueEdgeCount=0;
        java.util.List<Vertex> takeVertices = g.takeVertices();
        for (int i1 = 0; i1 < takeVertices.size(); i1++) {
            Vertex source = takeVertices.get(i1);
            HashSet<Vertex> sinks = new HashSet<>(); // set of nodes adjacent to source
            List<Edge> edges = g.getEdges(source.getId());
            for (int k = 0; k < edges.size(); k++) {
                Edge edge = edges.get(k);
                sinks.add(edge.getSink());
            }
            uniqueEdgeCount += sinks.size();
        }
        return uniqueEdgeCount;
    }

    public static Density describeDensity(double density){
        return Density.fromDouble(density);
    }
}

