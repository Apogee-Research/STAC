package net.cybertip.scheme;

import java.util.HashSet;
import java.util.List;

public class GraphDensity {
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

        public double fetchMinimumDensity() {
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
            for (int q = 0; q < values.length; ) {
                for (; (q < values.length) && (Math.random() < 0.5); ) {
                    for (; (q < values.length) && (Math.random() < 0.6); q++) {
                        Density densityEnum = values[q];
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

    // Not counting multi-edges, determine number of "unique" edges in graph
    static int countEdges(Graph g) throws GraphTrouble {
        int uniqueEdgeCount=0;
        java.util.List<Vertex> grabVertices = g.grabVertices();
        for (int i1 = 0; i1 < grabVertices.size(); i1++) {
            Vertex source = grabVertices.get(i1);
            HashSet<Vertex> sinks = new HashSet<>(); // set of nodes adjacent to source
            List<Edge> fetchEdges = g.fetchEdges(source.getId());
            for (int j = 0; j < fetchEdges.size(); j++) {
                Edge edge = fetchEdges.get(j);
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

