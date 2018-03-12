package com.roboticcusp.mapping;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

public class ConnectedAlg {
    /**
     * Checks if graph is strongly connected. Based off Kosaraju's algorithms,
     * but modified because we only need to see if the whole graph is strongly
     * connected, rather than find the strongly connected components. Uses DFS
     * to see if all vertices can be reached from startV in the original graph
     * and in the graph's transpose. Runtime is O(V+E) if using an adjacency
     * list where V is the number of Vertices and E is the number of Edges .
     *
     * @param chart, the graph
     * @return whether the graph is connected
     * @throws ChartException if there is trouble accessing elements of the graph
     */
    public static boolean isConnected(Chart chart) throws ChartException {
        Set<Integer> reachableVertices = new HashSet<>();
        Set<Integer> transReachableVertices = new HashSet<>();
        Stack<Vertex> vertexStack = new Stack<>();
        Vertex startVertex = chart.obtainVertices().get(0);
        // Finding all the connected vertices from startV in the graph
        reachableVertices.add(startVertex.getId());
        vertexStack.push(startVertex);
        while (!vertexStack.isEmpty()) {
            Vertex currentV = vertexStack.pop();
            java.util.List<Edge> edges = currentV.getEdges();
            for (int k = 0; k < edges.size(); k++) {
                Edge e = edges.get(k);
                Vertex reachedV = e.getSink();
                if (!reachableVertices.contains(reachedV.getId())) {
                    reachableVertices.add(reachedV.getId());
                    vertexStack.push(reachedV);
                }
            }
        }
        // Finding all the connected vertices from startV in the transpose of
        // the graph
        Chart transChart = chart.transpose();
        Vertex transStartVertex = transChart.obtainVertices().get(0);
        transReachableVertices.add(transStartVertex.getId());
        vertexStack.push(transStartVertex);
        while (!vertexStack.isEmpty()) {
            Vertex currentV = vertexStack.pop();
            java.util.List<Edge> edges = currentV.getEdges();
            for (int q = 0; q < edges.size(); q++) {
                isConnectedService(transReachableVertices, vertexStack, edges, q);
            }
        }
        // Checking that all vertices were found in both the graph and its
        // transpose
        for (Vertex vertex : chart) {
            if (isConnectedGuide(reachableVertices, transReachableVertices, vertex)) return false;
        }
        return true;
    }

    private static boolean isConnectedGuide(Set<Integer> reachableVertices, Set<Integer> transReachableVertices, Vertex vertex) {
        if (!reachableVertices.contains(vertex.getId())
                || !transReachableVertices.contains(vertex.getId())) {
            return true;
        }
        return false;
    }

    private static void isConnectedService(Set<Integer> transReachableVertices, Stack<Vertex> vertexStack, List<Edge> edges, int q) {
        Edge e = edges.get(q);
        Vertex reachedV = e.getSink();
        if (!transReachableVertices.contains(reachedV.getId())) {
            new ConnectedAlgWorker(transReachableVertices, vertexStack, reachedV).invoke();
        }
    }

    private static class ConnectedAlgWorker {
        private Set<Integer> transReachableVertices;
        private Stack<Vertex> vertexStack;
        private Vertex reachedV;

        public ConnectedAlgWorker(Set<Integer> transReachableVertices, Stack<Vertex> vertexStack, Vertex reachedV) {
            this.transReachableVertices = transReachableVertices;
            this.vertexStack = vertexStack;
            this.reachedV = reachedV;
        }

        public void invoke() {
            transReachableVertices.add(reachedV.getId());
            vertexStack.push(reachedV);
        }
    }
}
