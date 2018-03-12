package com.example.linalg.internal.graph.path;


import com.example.linalg.internal.graph.util.DijkstraSP;
import com.example.linalg.internal.graph.util.EdgeWeightedDigraph;
import com.example.linalg.internal.graph.util.Matrix;

public class SingleShortestPath {
	
	public double[] shortestPaths(double[][] A, int vertexId)
	{
		EdgeWeightedDigraph G = Matrix.toDiGraph(A);
		return shortestPaths(G, vertexId);
	}
	
	public double[][] shortestPaths(double[][] A, int[] vertexIds)
	{
		EdgeWeightedDigraph G = Matrix.toDiGraph(A);
		return shortestPaths(G, vertexIds);
	}
	
	public double[] shortestPaths(EdgeWeightedDigraph G, int vertexId)
	{
		DijkstraSP paths = new DijkstraSP(G, vertexId);
		double[] distances = new double[G.V()];
		
		for (int i = 0; i < G.V(); i++)
		{
			distances[i] = (paths.hasPathTo(i)) ? paths.distTo(i) : -1;			
		}
		return distances;
	}
	public double[][] shortestPaths(EdgeWeightedDigraph G, int[] vertexIds)
	{
		double[][] distances = new double[vertexIds.length][G.V()];
		
		for (int i = 0; i < vertexIds.length; i++)
		{
			double[] local_distances = shortestPaths(G, i);
			
			for (int j = 0; j < local_distances.length; j++)
			{
				distances[i][j] = local_distances[j];
			}
			
		}
		return distances;
		
	}
}
