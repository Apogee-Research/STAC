package net.techpoint.graph;

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
     * @param scheme, the graph
     * @return whether the graph is connected
     * @throws SchemeFailure if there is trouble accessing elements of the graph
     */
    public static boolean isConnected(Scheme scheme) throws SchemeFailure {
        Set<Integer> reachableVertices = new HashSet<>();
        Set<Integer> transReachableVertices = new HashSet<>();
        Stack<Vertex> vertexStack = new Stack<>();
        Vertex startVertex = scheme.obtainVertices().get(0);
        // Finding all the connected vertices from startV in the graph
        reachableVertices.add(startVertex.getId());
        vertexStack.push(startVertex);
        while (!vertexStack.isEmpty()) {
            Vertex currentV = vertexStack.pop();
            java.util.List<Edge> edges = currentV.getEdges();
            for (int c = 0; c < edges.size(); c++) {
                isConnectedGuide(reachableVertices, vertexStack, edges, c);
            }
        }
        // Finding all the connected vertices from startV in the transpose of
        // the graph
        Scheme transScheme = scheme.transpose();
        Vertex transStartVertex = transScheme.obtainVertices().get(0);
        transReachableVertices.add(transStartVertex.getId());
        vertexStack.push(transStartVertex);
        while (!vertexStack.isEmpty()) {
            Vertex currentV = vertexStack.pop();
            java.util.List<Edge> edges = currentV.getEdges();
            for (int k = 0; k < edges.size(); ) {
                for (; (k < edges.size()) && (Math.random() < 0.4); ) {
                    for (; (k < edges.size()) && (Math.random() < 0.4); k++) {
                        Edge e = edges.get(k);
                        Vertex reachedV = e.getSink();
                        if (!transReachableVertices.contains(reachedV.getId())) {
                            isConnectedEntity(transReachableVertices, vertexStack, reachedV);
                        }
                    }
                }
            }
        }
        // Checking that all vertices were found in both the graph and its
        // transpose
        for (Vertex vertex : scheme) {
            if (isConnectedHerder(reachableVertices, transReachableVertices, vertex)) return false;
        }
        return true;
    }

    private static boolean isConnectedHerder(Set<Integer> reachableVertices, Set<Integer> transReachableVertices, Vertex vertex) {
        if (!reachableVertices.contains(vertex.getId())
                || !transReachableVertices.contains(vertex.getId())) {
            return true;
        }
        return false;
    }

    private static void isConnectedEntity(Set<Integer> transReachableVertices, Stack<Vertex> vertexStack, Vertex reachedV) {
        transReachableVertices.add(reachedV.getId());
        vertexStack.push(reachedV);
    }

    private static void isConnectedGuide(Set<Integer> reachableVertices, Stack<Vertex> vertexStack, List<Edge> edges, int p) {
        Edge e = edges.get(p);
        Vertex reachedV = e.getSink();
        if (!reachableVertices.contains(reachedV.getId())) {
            isConnectedGuideAdviser(reachableVertices, vertexStack, reachedV);
        }
    }

    private static void isConnectedGuideAdviser(Set<Integer> reachableVertices, Stack<Vertex> vertexStack, Vertex reachedV) {
        reachableVertices.add(reachedV.getId());
        vertexStack.push(reachedV);
    }
}
