package com.example.linalg.internal.graph;

import com.example.linalg.internal.graph.util.Edge;
import com.example.linalg.internal.graph.util.EdgeWeightedGraph;
import com.example.linalg.internal.graph.util.Matrix;
import com.example.linalg.internal.graph.util.PrimMST;

public class SpanningTrees {

	public static double[][] MST(double[][] A)
	{
		EdgeWeightedGraph G = Matrix.toGraph(A);
		return MST(G);
	}	
	public static double[][] MST(EdgeWeightedGraph G)
	{
		PrimMST mst = new PrimMST(G);
		double[][] MST = new double[G.V()][G.V()];
	
		for (Edge e: mst.edges())
		{
			int from = e.either();
			int to = e.other(from);
			MST[from][to] = e.weight();
			MST[to][from] = e.weight();
		}
		return MST;
	}

}
