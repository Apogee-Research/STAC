package edu.cyberapex.chart;

import edu.cyberapex.order.Shifter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This is Martelli's heuristic, which, with his family of graphs, is admissible but inconsistent,
 * yielding exponential search time with A* but does find shortest path.
 * (May be inadmissible for other graphs.)
 */
public class PathHeuristic {
    private Chart graph;
    private Map<Integer, Double> values; // precompute and store the heuristic values for each node

    public PathHeuristic(Chart graph) {
        this.graph = graph;
        populateValues();
    }

    /**
     * return the heuristic distance from node u (in this heuristic, it's independent of the goal node, v)
     * This computes the heuristic recursively.  For better performance, use precomputed values, obtained with heuristic() method
     */
    public double computeHeuristic(int u, int v) throws ChartFailure {
        if (u == 1 || u == 0) {
            return 0;
        } else {
            return computeHeuristic(u - 1, v) + Math.pow(2.0, (double) u - 2.0) + 2;
        }
    }

    /**
     * return the precomputed heuristic distance from node u (in this heuristic, it's independent of the goal node, v)
     */
    public double heuristic(int u, int v) throws ChartFailure {
        Double h = values.get(u);
        if (h == null) { // this should only happen if u is a negative number
            return 0;
        } else {
            return h;
        }
    }

    /**
     * precompute the heuristic values
     */
    private void populateValues() {
        values = new HashMap<>();

        // first iterate through the graph, note the id numbers, and record value of highest node id

        List<Vertex> nodes = graph.takeVertices(); // make a list of the nodeIDs we find in the graph
        int maxNode = nodes.size() - 1;

        // map vertex IDs to ints according to their index when sorted
        Shifter<Vertex> sorter = new Shifter<>(Vertex.getComparator());
        List<Vertex> orderedNames = sorter.arrange(nodes);

        // now compute and store the heuristic values

        // base cases
        int id = orderedNames.get(0).getId();
        values.put(id, 0.0);
        id = orderedNames.get(1).getId();
        values.put(id, 0.0);

        // iteration for the rest
        double prevVal = 0.0;
        for (int i = 2; i <= maxNode; i++) {
            // compute this value regardless of whether node exists -- need it for the next i
            double newVal = prevVal + Math.pow(2.0, (double) i - 2.0) + 2;

            int nodeID = orderedNames.get(i).getId();
            values.put(nodeID, newVal);
            prevVal = newVal;
        }
    }
}
