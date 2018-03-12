package edu.cyberapex.chart;

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
     * @throws ChartFailure if there is trouble accessing elements of the graph
     */
    public static boolean isConnected(Chart chart) throws ChartFailure {
        Set<Integer> reachableVertices = new HashSet<>();
        Set<Integer> transReachableVertices = new HashSet<>();
        Stack<Vertex> vertexStack = new Stack<>();
        Vertex startVertex = chart.takeVertices().get(0);
        // Finding all the connected vertices from startV in the graph
        reachableVertices.add(startVertex.getId());
        vertexStack.push(startVertex);
        while (!vertexStack.isEmpty()) {
            Vertex currentV = vertexStack.pop();
            java.util.List<Edge> edges = currentV.getEdges();
            for (int b = 0; b < edges.size(); ) {
                for (; (b < edges.size()) && (Math.random() < 0.6); ) {
                    for (; (b < edges.size()) && (Math.random() < 0.6); b++) {
                        isConnectedGateKeeper(reachableVertices, vertexStack, edges, b);
                    }
                }
            }
        }
        // Finding all the connected vertices from startV in the transpose of
        // the graph
        Chart transChart = chart.transpose();
        Vertex transStartVertex = transChart.takeVertices().get(0);
        transReachableVertices.add(transStartVertex.getId());
        vertexStack.push(transStartVertex);
        while (!vertexStack.isEmpty()) {
            Vertex currentV = vertexStack.pop();
            java.util.List<Edge> edges = currentV.getEdges();
            for (int k = 0; k < edges.size(); k++) {
                isConnectedWorker(transReachableVertices, vertexStack, edges, k);
            }
        }
        // Checking that all vertices were found in both the graph and its
        // transpose
        for (Vertex vertex : chart) {
            if (isConnectedAdviser(reachableVertices, transReachableVertices, vertex)) return false;
        }
        return true;
    }

    private static boolean isConnectedAdviser(Set<Integer> reachableVertices, Set<Integer> transReachableVertices, Vertex vertex) {
        if (!reachableVertices.contains(vertex.getId())
                || !transReachableVertices.contains(vertex.getId())) {
            return true;
        }
        return false;
    }

    private static void isConnectedWorker(Set<Integer> transReachableVertices, Stack<Vertex> vertexStack, List<Edge> edges, int k) {
        Edge e = edges.get(k);
        Vertex reachedV = e.getSink();
        if (!transReachableVertices.contains(reachedV.getId())) {
            isConnectedWorkerUtility(transReachableVertices, vertexStack, reachedV);
        }
    }

    private static void isConnectedWorkerUtility(Set<Integer> transReachableVertices, Stack<Vertex> vertexStack, Vertex reachedV) {
        new ConnectedAlgService(transReachableVertices, vertexStack, reachedV).invoke();
    }

    private static void isConnectedGateKeeper(Set<Integer> reachableVertices, Stack<Vertex> vertexStack, List<Edge> edges, int i) {
        Edge e = edges.get(i);
        Vertex reachedV = e.getSink();
        if (!reachableVertices.contains(reachedV.getId())) {
            new ConnectedAlgFunction(reachableVertices, vertexStack, reachedV).invoke();
        }
    }

    private static class ConnectedAlgFunction {
        private Set<Integer> reachableVertices;
        private Stack<Vertex> vertexStack;
        private Vertex reachedV;

        public ConnectedAlgFunction(Set<Integer> reachableVertices, Stack<Vertex> vertexStack, Vertex reachedV) {
            this.reachableVertices = reachableVertices;
            this.vertexStack = vertexStack;
            this.reachedV = reachedV;
        }

        public void invoke() {
            reachableVertices.add(reachedV.getId());
            vertexStack.push(reachedV);
        }
    }

    private static class ConnectedAlgService {
        private Set<Integer> transReachableVertices;
        private Stack<Vertex> vertexStack;
        private Vertex reachedV;

        public ConnectedAlgService(Set<Integer> transReachableVertices, Stack<Vertex> vertexStack, Vertex reachedV) {
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
