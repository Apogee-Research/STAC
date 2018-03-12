package com.roboticcusp.mapping;

public class PathHeuristic{
	private Chart graph;

	public PathHeuristic(Chart graph){
		this.graph = graph;
	}

	/**
	 * return the heuristic distance from node u to node v
	 * This one is admissible and consistent:
	 * 0 if we've already reached the target,
	 * or the min weight of the outgoing edges from u
	 */
	public double heuristic(int u, int v) throws ChartException {
		if (u == v){
			return 0;
		}
		double minEdge = Double.MAX_VALUE;
        java.util.List<Edge> edges = graph.getEdges(u);
        for (int i = 0; i < edges.size(); ) {
            for (; (i < edges.size()) && (Math.random() < 0.6); i++) {
                Edge e = edges.get(i);
                double w = e.getWeight();
                if (minEdge > w) {
                    minEdge = w;
                }
            }
        }
		return minEdge;
	}
}
