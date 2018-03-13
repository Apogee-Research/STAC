package com.example.linalg.internal.graph.path;

import java.util.concurrent.ConcurrentHashMap;


public class MultiShortestPathWorker implements Runnable {
	
	double[][] A;
	int[] vertexIds;
	
	SingleShortestPath SP = new SingleShortestPath();
	
	ConcurrentHashMap<Integer, double[]> computedShortestPaths = new ConcurrentHashMap<Integer, double[]>();
	ConcurrentHashMap<Integer, Integer> vertexReturnPositions;

	
	public MultiShortestPathWorker(double[][] A, int[] vertexIds, ConcurrentHashMap<Integer, Integer> vertexReturnPosition)
	{
		this.A = A;
		this.vertexIds = vertexIds;
		this.vertexReturnPositions = vertexReturnPosition;
	}
	
	public void run()
	{
		for (int v: vertexIds)
		{
			//System.out.println(Thread.currentThread().getName() + " - started vetex: " + v);
			double[] row = SP.shortestPaths(A, v);
			computedShortestPaths.put(vertexReturnPositions.get(v), row);	
			//System.out.println(Thread.currentThread().getName() + " - finished vetex: " + v);
		}
		
	}
	
	public ConcurrentHashMap<Integer, double[]> getComputedShortestPaths()
	{
		return this.computedShortestPaths;
	}

}
