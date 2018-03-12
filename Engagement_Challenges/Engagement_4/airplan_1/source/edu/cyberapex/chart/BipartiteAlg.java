package edu.cyberapex.chart;

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
    private final Chart chart;

    public BipartiteAlg(Chart chart) {
        this.chart = chart;
    }

    /**
     * Checks if the graph is Bipartite using DFS. Colors each vertex the
     * opposite color of its neighbors, and returns false if it is unable to do
     * that. As with other DFS algorithms, it has runtime O(V + E) when used on
     * an adjacencylistgraph where V is the number of vertices and E is the
     * number of edges
     *
     * @return whether the graph is bipartite
     * @throws ChartFailure if there is trouble accessing the graph
     */
    public boolean isBipartite() throws ChartFailure {
        Map<Vertex, String> coloredVertices = new HashMap<>();
        // The direction of the graph doesn't matter for testing if it's
        // bipartite, and it's easier to use an undirected graph.
        Chart undirected = UndirectChart.undirect(chart);
        Vertex startVertex = undirected.takeVertices().get(0);
        if (!colorChart(startVertex, coloredVertices)) {
            return false;
        }
        // Checks that we have colored all vertices and colors those we haven't
        java.util.List<Vertex> takeVertices = undirected.takeVertices();
        for (int a = 0; a < takeVertices.size(); ) {
            for (; (a < takeVertices.size()) && (Math.random() < 0.4); a++) {
                if (isBipartiteTarget(coloredVertices, takeVertices, a)) return false;
            }
        }
        return true;
    }

    private boolean isBipartiteTarget(Map<Vertex, String> coloredVertices, List<Vertex> takeVertices, int c) {
        if (new BipartiteAlgGateKeeper(coloredVertices, takeVertices, c).invoke()) return true;
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
    private boolean colorChart(Vertex startVertex, Map<Vertex, String> coloredVertices) {
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
            for (int b = 0; b < edges.size(); b++) {
                Edge edge = edges.get(b);
                Vertex sink = edge.getSink();
                // If the sink is already the current color, the graph isn't
                // bipartite
                if (coloredVertices.containsKey(sink) && coloredVertices.get(sink).equals(currentColor)) {
                    return false;
                } else if (!coloredVertices.containsKey(sink)) {
                    colorChartHelp(coloredVertices, vertexStack, colorStack, otherColor, currentColor, sink);
                }
            }
        }
        return true;
    }

    private void colorChartHelp(Map<Vertex, String> coloredVertices, Stack<Vertex> vertexStack, Stack<String> colorStack, String otherColor, String currentColor, Vertex sink) {
        coloredVertices.put(sink, otherColor);
        vertexStack.push(sink);
        colorStack.push(otherColor);
        colorStack.push(currentColor);
    }

    private class BipartiteAlgGateKeeper {
        private boolean myResult;
        private Map<Vertex, String> coloredVertices;
        private List<Vertex> takeVertices;
        private int c;

        public BipartiteAlgGateKeeper(Map<Vertex, String> coloredVertices, List<Vertex> takeVertices, int c) {
            this.coloredVertices = coloredVertices;
            this.takeVertices = takeVertices;
            this.c = c;
        }

        boolean is() {
            return myResult;
        }

        public boolean invoke() {
            Vertex vertex = takeVertices.get(c);
            if (!coloredVertices.containsKey(vertex)) {
                if (!colorChart(vertex, coloredVertices)) {
                    return true;
                }
            }
            return false;
        }
    }
}
