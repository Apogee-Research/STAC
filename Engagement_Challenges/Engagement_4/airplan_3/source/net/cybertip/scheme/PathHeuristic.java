package net.cybertip.scheme;

public class PathHeuristic{
	private Graph graph;

	public PathHeuristic(Graph graph){
		this.graph = graph;
	}

	/**
	 * return the heuristic distance from node u to node v
	 * This one is admissible and consistent:
	 * 0 if we've already reached the target,
	 * or the min weight of the outgoing edges from u
	 */
	public double heuristic(int u, int v) throws GraphTrouble {
		if (u == v){
			return 0;
		}
		double minEdge = Double.MAX_VALUE;
        java.util.List<Edge> fetchEdges = graph.fetchEdges(u);
        for (int i = 0; i < fetchEdges.size(); i++) {
            Edge e = fetchEdges.get(i);
            double w = e.getWeight();
            if (minEdge > w) {
                minEdge = w;
            }
        }
		return minEdge;
	}
}
