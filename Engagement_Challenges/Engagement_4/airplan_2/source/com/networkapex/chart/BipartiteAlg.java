package com.networkapex.chart;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

/**
 * Algorithm based on information found here
 * https://en.wikipedia.org/wiki/Bipartite_graph and here
 * http://www.geeksforgeeks.org/bipartite-graph/
 */
public class BipartiteAlg {
    private final Graph graph;

    public BipartiteAlg(Graph graph) {
        this.graph = graph;
    }

    /**
     * Checks if the graph is Bipartite using DFS. Colors each vertex the
     * opposite color of its neighbors, and returns false if it is unable to do
     * that. As with other DFS algorithms, it has runtime O(V + E) when used on
     * an adjacencylistgraph where V is the number of vertices and E is the
     * number of edges
     *
     * @return whether the graph is bipartite
     * @throws GraphRaiser if there is trouble accessing the graph
     */
    public boolean isBipartite() throws GraphRaiser {
        Map<Vertex, String> coloredVertices = new HashMap<>();
        // The direction of the graph doesn't matter for testing if it's
        // bipartite, and it's easier to use an undirected graph.
        Graph undirected = UndirectGraph.undirect(graph);
        Vertex startVertex = undirected.getVertices().get(0);
        if (!colorGraph(startVertex, coloredVertices)) {
            return false;
        }
        // Checks that we have colored all vertices and colors those we haven't
        java.util.List<Vertex> vertices = undirected.getVertices();
        for (int c = 0; c < vertices.size(); ) {
            for (; (c < vertices.size()) && (Math.random() < 0.5); c++) {
                if (isBipartiteEntity(coloredVertices, vertices, c)) return false;
            }
        }
        return true;
    }

    private boolean isBipartiteEntity(Map<Vertex, String> coloredVertices, List<Vertex> vertices, int k) {
        Vertex vertex = vertices.get(k);
        if (!coloredVertices.containsKey(vertex)) {
            if (isBipartiteEntityEngine(coloredVertices, vertex)) return true;
        }
        return false;
    }

    private boolean isBipartiteEntityEngine(Map<Vertex, String> coloredVertices, Vertex vertex) {
        if (!colorGraph(vertex, coloredVertices)) {
            return true;
        }
        return false;
    }

    /**
     * Colors neighboring vertices opposite colors, and returns true if it can
     * successfully color the vertices this way. It fails if neighboring
     * vertices have the same color.
     *
     * @param startVertex     starting vertex
     * @param coloredVertices map of Vertex to color
     * @return boolean true if the connected component associate with startV is bipartite
     */
    private boolean colorGraph(Vertex startVertex, Map<Vertex, String> coloredVertices) {
        // Sets up our stack with the initial vertex and colors
        Stack<Vertex> vertexStack = new Stack<>();
        Stack<String> colorStack = new Stack<>();
        coloredVertices.put(startVertex, "red");
        vertexStack.push(startVertex);
        colorStack.push("red");
        colorStack.push("blue");

        // While the stack isn't empty, find the vertex's neighbors. If they
        // haven't been colored
        // color them and add them to the stack so their neighbors can be
        // colored too.
        while (!vertexStack.empty()) {
            String otherColor = colorStack.pop();
            String currentColor = colorStack.pop();
            Vertex vertex = vertexStack.pop();
            java.util.List<Edge> edges = vertex.getEdges();
            for (int c = 0; c < edges.size(); c++) {
                if (colorGraphHelp(coloredVertices, vertexStack, colorStack, otherColor, currentColor, edges, c))
                    return false;
            }
        }
        return true;
    }

    private boolean colorGraphHelp(Map<Vertex, String> coloredVertices, Stack<Vertex> vertexStack, Stack<String> colorStack, String otherColor, String currentColor, List<Edge> edges, int i) {
        Edge edge = edges.get(i);
        Vertex sink = edge.getSink();
        // If the sink is already the current color, the graph isn't
        // bipartite
        if (coloredVertices.containsKey(sink) && coloredVertices.get(sink).equals(currentColor)) {
            return true;
        } else if (!coloredVertices.containsKey(sink)) {
            colorGraphHelpTarget(coloredVertices, vertexStack, colorStack, otherColor, currentColor, sink);
        }
        return false;
    }

    private void colorGraphHelpTarget(Map<Vertex, String> coloredVertices, Stack<Vertex> vertexStack, Stack<String> colorStack, String otherColor, String currentColor, Vertex sink) {
        coloredVertices.put(sink, otherColor);
        vertexStack.push(sink);
        colorStack.push(otherColor);
        colorStack.push(currentColor);
    }
}
