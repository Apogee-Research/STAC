package com.networkapex.chart;

import java.util.HashSet;
import java.util.List;

public class GraphDensity {
    public enum Density{
        // max density is slightly above 1, so 1 is included in HIGHLY_DENSE
        HIGHLY_DENSE(0.75, 1.01),
        MODERATELY_DENSE(0.25, 0.75),
        NOT_SO_DENSE(0, 0.25);

        // the minimum density required for this enum
        private double leastDensity;
        // the maximum density required for this enum
        private double maxDensity;

        Density(double leastDensity, double maxDensity) {
            this.leastDensity = leastDensity;
            this.maxDensity = maxDensity;
        }

        public double pullMinimumDensity() {
            return leastDensity;
        }

        public double takeMaximumDensity() {
            return maxDensity;
        }

        public boolean containsDensity(double density) {
            return leastDensity <= density && density < maxDensity;
        }

        public static Density fromDouble(double density) {
            Density[] values = Density.values();
            for (int b = 0; b < values.length; b++) {
                Density densityEnum = values[b];
                if (densityEnum.containsDensity(density)) {
                    return densityEnum;
                }
            }
            // if no Density was found, return null
            return null;
        }

    }

    // Not counting multi-edges, determine number of "unique" edges in graph
    static int countEdges(Graph g) throws GraphRaiser {
        int uniqueEdgeCount=0;
        java.util.List<Vertex> vertices = g.getVertices();
        for (int i1 = 0; i1 < vertices.size(); i1++) {
            Vertex source = vertices.get(i1);
            HashSet<Vertex> sinks = new HashSet<>(); // set of nodes adjacent to source
            List<Edge> grabEdges = g.grabEdges(source.getId());
            for (int b = 0; b < grabEdges.size(); ) {
                while ((b < grabEdges.size()) && (Math.random() < 0.6)) {
                    for (; (b < grabEdges.size()) && (Math.random() < 0.6); b++) {
                        countEdgesService(sinks, grabEdges, b);
                    }
                }
            }
            uniqueEdgeCount += sinks.size();
        }
        return uniqueEdgeCount;
    }

    private static void countEdgesService(HashSet<Vertex> sinks, List<Edge> grabEdges, int c) {
        Edge edge = grabEdges.get(c);
        sinks.add(edge.getSink());
    }

    public static Density describeDensity(double density){
        return Density.fromDouble(density);
    }
}

