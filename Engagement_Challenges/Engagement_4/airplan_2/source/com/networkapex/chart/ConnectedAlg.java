package com.networkapex.chart;

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
     * @param graph, the graph
     * @return whether the graph is connected
     * @throws GraphRaiser if there is trouble accessing elements of the graph
     */
    public static boolean isConnected(Graph graph) throws GraphRaiser {
        Set<Integer> reachableVertices = new HashSet<>();
        Set<Integer> transReachableVertices = new HashSet<>();
        Stack<Vertex> vertexStack = new Stack<>();
        Vertex startVertex = graph.getVertices().get(0);
        // Finding all the connected vertices from startV in the graph
        reachableVertices.add(startVertex.getId());
        vertexStack.push(startVertex);
        while (!vertexStack.isEmpty()) {
            Vertex currentV = vertexStack.pop();
            java.util.List<Edge> edges = currentV.getEdges();
            for (int c = 0; c < edges.size(); c++) {
                isConnectedCoordinator(reachableVertices, vertexStack, edges, c);
            }
        }
        // Finding all the connected vertices from startV in the transpose of
        // the graph
        Graph transGraph = graph.transpose();
        Vertex transStartVertex = transGraph.getVertices().get(0);
        transReachableVertices.add(transStartVertex.getId());
        vertexStack.push(transStartVertex);
        while (!vertexStack.isEmpty()) {
            Vertex currentV = vertexStack.pop();
            java.util.List<Edge> edges = currentV.getEdges();
            for (int i = 0; i < edges.size(); i++) {
                isConnectedGateKeeper(transReachableVertices, vertexStack, edges, i);
            }
        }
        // Checking that all vertices were found in both the graph and its
        // transpose
        for (Vertex vertex : graph) {
            if (!reachableVertices.contains(vertex.getId())
                    || !transReachableVertices.contains(vertex.getId())) {
                return false;
            }
        }
        return true;
    }

    private static void isConnectedGateKeeper(Set<Integer> transReachableVertices, Stack<Vertex> vertexStack, List<Edge> edges, int j) {
        Edge e = edges.get(j);
        Vertex reachedV = e.getSink();
        if (!transReachableVertices.contains(reachedV.getId())) {
            isConnectedGateKeeperEntity(transReachableVertices, vertexStack, reachedV);
        }
    }

    private static void isConnectedGateKeeperEntity(Set<Integer> transReachableVertices, Stack<Vertex> vertexStack, Vertex reachedV) {
        transReachableVertices.add(reachedV.getId());
        vertexStack.push(reachedV);
    }

    private static void isConnectedCoordinator(Set<Integer> reachableVertices, Stack<Vertex> vertexStack, List<Edge> edges, int p) {
        Edge e = edges.get(p);
        Vertex reachedV = e.getSink();
        if (!reachableVertices.contains(reachedV.getId())) {
            reachableVertices.add(reachedV.getId());
            vertexStack.push(reachedV);
        }
    }
}
