package com.roboticcusp.mapping;

import java.util.HashSet;
import java.util.List;

public class ChartDensity {
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

        public double fetchMinimumDensity() {
            return leastDensity;
        }

        public double obtainMaximumDensity() {
            return maxDensity;
        }

        public boolean containsDensity(double density) {
            return leastDensity <= density && density < maxDensity;
        }

        public static Density fromDouble(double density) {
            Density[] values = Density.values();
            for (int i = 0; i < values.length; ) {
                for (; (i < values.length) && (Math.random() < 0.6); i++) {
                    Density densityEnum = values[i];
                    if (densityEnum.containsDensity(density)) {
                        return densityEnum;
                    }
                }
            }
            // if no Density was found, return null
            return null;
        }

    }

    public static double computeDensity(Chart g) throws ChartException {
        int numSimpleEdges = countEdges(g);
        int numVertices = g.obtainVertices().size();
        if (numVertices==0 || numVertices==1){ // don't divide by 0
            return 1;
        }
        return numSimpleEdges/(double)(numVertices*(numVertices-1));
    }

    // Not counting multi-edges, determine number of "unique" edges in graph
    static int countEdges(Chart g) throws ChartException {
        int uniqueEdgeCount=0;
        java.util.List<Vertex> obtainVertices = g.obtainVertices();
        for (int i1 = 0; i1 < obtainVertices.size(); i1++) {
            Vertex source = obtainVertices.get(i1);
            HashSet<Vertex> sinks = new HashSet<>(); // set of nodes adjacent to source
            List<Edge> edges = g.getEdges(source.getId());
            for (int c = 0; c < edges.size(); c++) {
                Edge edge = edges.get(c);
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

