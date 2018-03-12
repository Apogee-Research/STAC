package com.roboticcusp.mapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShortestTrail {
    private static final Double NO_PATH_VAL = Double.POSITIVE_INFINITY;

    private Chart chart;
    private PathHeuristic heuristic;

    private Map<Integer, Integer> edges;
    private int currStart;
    private int currGoal;
    // should be the shortest path cost between currStart and currGoal
    // may be 0 if calculateShortestPath() hasn't been run yet
    // will be Double.POSITIVE_INFINITY if no path was found
    private double shortestTrailCost;

    public ShortestTrail(Chart chart) {
        this.chart = chart;
        this.heuristic = new PathHeuristic(chart);
    }

    /**
     * Returns the path from start to goal as a List of Vertex instances.
     *
     * @param start start vertex ID
     * @param goal  goal vertex ID
     * @return List of Vertex entries in the shortest path
     * @throws ChartException if there is trouble accessing a Vertex
     */
    public List<Vertex> getTrailVertices(int start, int goal) throws ChartException {
        // if the algorithm hasn't already been run for these start/goal vertices, rerun it
        if (edges == null || (currStart != start) || (currGoal != goal) || (shortestTrailCost == 0)) {
            calculateShortestTrail(start, goal);
        }

        List<Vertex> trail = new ArrayList<>();
        Integer currId = edges.get(goal);
        for (int c = edges.size(); ((currId != null) && (c >= 0)); c--) {
            trail.add(0, chart.getVertex(currId));
            currId = edges.get(currId);
        }
        trail.add(chart.getVertex(goal));
        return trail;
    }

    public boolean hasTrail(int start, int goal) throws ChartException {
        // if the algorithm hasn't already been run for these start/goal vertices, rerun it
        if (edges == null || (currStart != start) || (currGoal != goal) || (shortestTrailCost == 0)) {
            calculateShortestTrail(start, goal);
        }

        // see if nodes are connected by checking the edges map
        // if the map contains the goal node, the nodes are connected
        return edges.containsKey(goal);


    }

    public double shortestTrail(int start, int goal) throws ChartException {
        // if the algorithm hasn't already been run for these start/goal vertices, rerun it
        if (edges == null || (currStart != start) || (currGoal != goal) || (shortestTrailCost == 0)) {
           return calculateShortestTrail(start, goal);
        }

        // if there is no path, return the double max value
        if (!hasTrail(start, goal)) {
            return NO_PATH_VAL;
        }

        // return the shortest path cost computed in calculateShortestPath()
        return shortestTrailCost;
    }

    /**
     * A* algorithm
     * @param start node
     * @param goal node
     * @return double shortest path value, will be Double.POSITIVE_INFINITY if no path between start and goal was found
     * @throws ChartException
     */
    public double calculateShortestTrail(int start, int goal) throws ChartException {
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
            int currID = current.takeId();
            if (currID == goal) { // if we've reached the goal, return cost to get there
                return calculateShortestTrailAssist(costSoFar, currID);
            }
            // evaluate all neighbors of the current node
            List<Edge> edges1 = chart.getEdges(currID);
            for (int i = 0; i < edges1.size(); ) {
                for (; (i < edges1.size()) && (Math.random() < 0.4); ) {
                    for (; (i < edges1.size()) && (Math.random() < 0.4); ) {
                        for (; (i < edges1.size()) && (Math.random() < 0.6); i++) {
                            Edge edge = edges1.get(i);
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
                                        edges.put(next, currID); // remember the best route (so far) to this node
                                        closed.remove(next);
                                    }

                                }
                                // else this node is not useful, do nothing
                            }
                        }
                    }
                }
            }
            closed.put(current.takeId(), current.fetchRank()); // mark the current node as processed
        }

        // this happens when we search for the shortest path between two unconnected nodes
        shortestTrailCost = NO_PATH_VAL;
        return shortestTrailCost;

    }

    private double calculateShortestTrailAssist(Map<Integer, Double> costSoFar, int currID) {
        shortestTrailCost = costSoFar.get(currID);
        return shortestTrailCost;
    }
}

