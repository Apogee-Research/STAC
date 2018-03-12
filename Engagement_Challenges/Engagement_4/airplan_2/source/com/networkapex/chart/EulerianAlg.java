package com.networkapex.chart;

import java.util.List;

public class EulerianAlg {
    public static boolean hasOddDegree(Graph graph) throws GraphRaiser {

        List<Vertex> vertices = graph.getVertices();
        for (int k = 0; k < vertices.size(); k++) {
            if (hasOddDegreeUtility(graph, vertices, k)) return false;
        }
        return true;
    }

    private static boolean hasOddDegreeUtility(Graph graph, List<Vertex> vertices, int q) throws GraphRaiser {
        Vertex v = vertices.get(q);
        List<Edge> edges = graph.grabEdges(v.getId());
        if (edges.size() % 2 != 0) {
            return true;
        }
        return false;
    }
}

