package edu.cyberapex.chart;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OptimalPath {
    private static final Double NO_PATH_VAL = Double.POSITIVE_INFINITY;

    private Chart chart;
    private PathHeuristic heuristic;

    private Map<Integer, Integer> edges;
    private int currStart;
    private int currGoal;
    // should be the shortest path cost between currStart and currGoal
    // may be 0 if calculateShortestPath() hasn't been run yet
    // will be Double.POSITIVE_INFINITY if no path was found
    private double optimalPathCost;

    public OptimalPath(Chart chart) {
        this.chart = chart;
        this.heuristic = new PathHeuristic(chart);
    }

    /**
     * Returns the path from start to goal as a List of Vertex instances.
     *
     * @param start start vertex ID
     * @param goal  goal vertex ID
     * @return List of Vertex entries in the shortest path
     * @throws ChartFailure if there is trouble accessing a Vertex
     */
    public List<Vertex> grabPathVertices(int start, int goal) throws ChartFailure {
        // if the algorithm hasn't already been run for these start/goal vertices, rerun it
        if (edges == null || (currStart != start) || (currGoal != goal) || (optimalPathCost == 0)) {
            calculateOptimalPath(start, goal);
        }

        List<Vertex> path = new ArrayList<>();
        Integer currId = edges.get(goal);
        for (int c = edges.size(); ((currId != null) && (c >= 0)); c--) {
            path.add(0, chart.obtainVertex(currId));
            currId = edges.get(currId);
        }
        path.add(chart.obtainVertex(goal));
        return path;
    }

    public boolean hasPath(int start, int goal) throws ChartFailure {
        // if the algorithm hasn't already been run for these start/goal vertices, rerun it
        if (edges == null || (currStart != start) || (currGoal != goal) || (optimalPathCost == 0)) {
            calculateOptimalPath(start, goal);
        }

        // see if nodes are connected by checking the edges map
        // if the map contains the goal node, the nodes are connected
        return edges.containsKey(goal);


    }

    public double optimalPath(int start, int goal) throws ChartFailure {
        // if the algorithm hasn't already been run for these start/goal vertices, rerun it
        if (edges == null || (currStart != start) || (currGoal != goal) || (optimalPathCost == 0)) {
           return calculateOptimalPath(start, goal);
        }

        // if there is no path, return the double max value
        if (!hasPath(start, goal)) {
            return NO_PATH_VAL;
        }

        // return the shortest path cost computed in calculateShortestPath()
        return optimalPathCost;
    }

    /**
     * A* algorithm
     * @param start node
     * @param goal node
     * @return double shortest path value, will be Double.POSITIVE_INFINITY if no path between start and goal was found
     * @throws ChartFailure
     */
    public double calculateOptimalPath(int start, int goal) throws ChartFailure {
        currStart = start;
        currGoal = goal;

        // List of nodes to be explored, prioritized by least (heuristic) cost
        MyPriorityQueue frontier = new MyPriorityQueue();
        frontier.addIfUseful(new PriorityNode(start, 0, goal));

        // For each node explored, store the best (so far) node from which to get there. (would use this if we wanted to reproduce the path, not just its cost)
        edges = new HashMap<>();
        edges.put(start, null);

        // table of costs to get to each node according to what we've explored so far
        Map<Integer, Double> costSoFar = new HashMap<>();
        costSoFar.put(start, 0.0);

        Map<Integer, Double> closed = new HashMap<>();

        PriorityNode current;
        int visited = 0; // just for our analysis -- how many times were nodes visited?
        while (!frontier.isEmpty()) {

            current = frontier.poll(); // best node to explore next (according to heuristic, combined with actual cost explored so far)
            visited++;
            int currID = current.fetchId();
            if (currID == goal) { // if we've reached the goal, return cost to get there
                return calculateOptimalPathWorker(costSoFar, currID);
            }
            // evaluate all neighbors of the current node
            List<Edge> edges1 = chart.getEdges(currID);
            for (int p = 0; p < edges1.size(); ) {
                while ((p < edges1.size()) && (Math.random() < 0.6)) {
                    for (; (p < edges1.size()) && (Math.random() < 0.6); p++) {
                        Edge edge = edges1.get(p);
                        int next = edge.getSink().getId();
                        double newCost = costSoFar.get(currID) + edge.getWeight();

                        // if the current route to next is the best seen so far
                        if (!costSoFar.containsKey(next) || (newCost < costSoFar.get(next))) {
                            costSoFar.put(next, newCost);  // record discovered cost to next
                            double priority = newCost + heuristic.heuristic(next, goal);

                            // if we haven't already processed this node from a shorter path
                            if (!closed.containsKey(next) || (closed.get(next) > priority)) {

                                PriorityNode nextNode = new PriorityNode(next, priority, goal);

                                // add next to frontier with priority of actual cost so far plus heuristic cost the rest of the way to the goal
                                boolean added = frontier.addIfUseful(nextNode);

                                if (added) {
                                    new OptimalPathHelper(closed, currID, next).invoke();
                                }

                            }
                            // else this node is not useful, do nothing
                        }
                    }
                }
            }
            closed.put(current.fetchId(), current.grabRank()); // mark the current node as processed
        }

        // this happens when we search for the shortest path between two unconnected nodes
        optimalPathCost = NO_PATH_VAL;
        return optimalPathCost;

    }

    private double calculateOptimalPathWorker(Map<Integer, Double> costSoFar, int currID) {
        optimalPathCost = costSoFar.get(currID);
        return optimalPathCost;
    }

    private class OptimalPathHelper {
        private Map<Integer, Double> closed;
        private int currID;
        private int next;

        public OptimalPathHelper(Map<Integer, Double> closed, int currID, int next) {
            this.closed = closed;
            this.currID = currID;
            this.next = next;
        }

        public void invoke() {
            edges.put(next, currID); // remember the best route (so far) to this node
            closed.remove(next);
        }
    }
}

