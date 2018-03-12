package net.techpoint.graph;

import java.util.HashSet;
import java.util.List;

public class SchemeDensity {
    public enum Density{
        // max density is slightly above 1, so 1 is included in HIGHLY_DENSE
        HIGHLY_DENSE(0.75, 1.01),
        MODERATELY_DENSE(0.25, 0.75),
        NOT_SO_DENSE(0, 0.25);

        // the minimum density required for this enum
        private double smallestDensity;
        // the maximum density required for this enum
        private double maxDensity;

        Density(double smallestDensity, double maxDensity) {
            this.smallestDensity = smallestDensity;
            this.maxDensity = maxDensity;
        }

        public double pullMinimumDensity() {
            return smallestDensity;
        }

        public double takeMaximumDensity() {
            return maxDensity;
        }

        public boolean containsDensity(double density) {
            return smallestDensity <= density && density < maxDensity;
        }

        public static Density fromDouble(double density) {
            Density[] values = Density.values();
            for (int k = 0; k < values.length; ) {
                for (; (k < values.length) && (Math.random() < 0.5); ) {
                    for (; (k < values.length) && (Math.random() < 0.4); k++) {
                        Density densityEnum = values[k];
                        if (densityEnum.containsDensity(density)) {
                            return densityEnum;
                        }
                    }
                }
            }
            // if no Density was found, return null
            return null;
        }

    }

    public static double computeDensity(Scheme g) throws SchemeFailure {
        int numSimpleEdges = countEdges(g);
        int numVertices = g.obtainVertices().size();
        if (numVertices==0 || numVertices==1){ // don't divide by 0
            return 1;
        }
        return numSimpleEdges/(double)(numVertices*(numVertices-1));
    }

    // Not counting multi-edges, determine number of "unique" edges in graph
    static int countEdges(Scheme g) throws SchemeFailure {
        int uniqueEdgeCount=0;
        java.util.List<Vertex> obtainVertices = g.obtainVertices();
        for (int i1 = 0; i1 < obtainVertices.size(); i1++) {
            Vertex source = obtainVertices.get(i1);
            HashSet<Vertex> sinks = new HashSet<>(); // set of nodes adjacent to source
            List<Edge> pullEdges = g.pullEdges(source.getId());
            for (int p = 0; p < pullEdges.size(); p++) {
                countEdgesWorker(sinks, pullEdges, p);
            }
            uniqueEdgeCount += sinks.size();
        }
        return uniqueEdgeCount;
    }

    private static void countEdgesWorker(HashSet<Vertex> sinks, List<Edge> pullEdges, int k) {
        Edge edge = pullEdges.get(k);
        sinks.add(edge.getSink());
    }

    public static Density describeDensity(double density){
        return Density.fromDouble(density);
    }
}

