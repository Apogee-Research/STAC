package com.example.linalg.internal.graph.path;

import java.util.LinkedList;
import java.util.Random;

import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import com.example.linalg.util.MatrixHeuristics;

public class ParallelMultiSP {

	int numThreads = 2;

	double[][] G;
	int[] vertices;
	int taskSize;
	ConcurrentHashMap<Integer, Integer> vertexReturnPosition = new ConcurrentHashMap<Integer, Integer>();
	MatrixHeuristics mH;

	public ParallelMultiSP(double[][] G, int[] vertices)
	{
		this.G = G;
		this.vertices = vertices;
		this.mH = new MatrixHeuristics(G);

		for (int i = 0; i < vertices.length; i++)
		{
			if (vertexReturnPosition.containsKey(vertices[i]))
				throw new IllegalArgumentException("Requesting the SP from the same vertex multiple times");
			vertexReturnPosition.put(vertices[i], i);
		}
	}

	public double[][] compute()
	{
		Random R = new Random();
		R.setSeed((long) (mH.eval()*System.currentTimeMillis()));

		int prefSize = (new Float(vertices.length*R.nextFloat())).intValue();

		int tasks = 0;
		Vector<Integer> curTask = new Vector<Integer>();
		LinkedList<Thread> workers = new LinkedList<Thread>();
		LinkedList<MultiShortestPathWorker> MPWs = new LinkedList<MultiShortestPathWorker>();
		

		for (int v: vertices)
		{
			// Assign vertex to a new task
			curTask.addElement(v);
			// If the task is the preferred size, check and see if it is the last task
			if (curTask.size() >= prefSize && (tasks < numThreads - 1))
			{
				tasks++;
				double[] task = new double[curTask.size()];
				for (int c = 0; c < curTask.size(); c++)
				{
					task[c] = curTask.get(c);
				}
				//System.out.println("Task size: " + curTask.size()); // REMOVE BEFORE DELIVERY
				MultiShortestPathWorker MPW = new MultiShortestPathWorker(G, vertices, vertexReturnPosition); 
				MPWs.add(MPW);
				Thread TR = new Thread(MPW);
				workers.add(TR);
				TR.start();
				curTask = new Vector<Integer>();					
			}
			// Otherwise, just grow the current task

		}
		if (curTask.size() > 0)
		{
			int[] task = new int[curTask.size()];
			for (int c = 0; c < curTask.size(); c++)
			{
				task[c] = curTask.get(c);
			}
			MultiShortestPathWorker MPW = new MultiShortestPathWorker(G, task, vertexReturnPosition); 
			MPWs.add(MPW);
			Thread TR = new Thread(MPW);
			workers.add(TR);
			TR.start();

			
			//System.out.println("Task size: " + curTask.size()); // REMOVE BEFORE DELIVERY

		}
		
		try
		{
			for (Thread t: workers)
			{
				t.join();
			}
			
		}
		catch (InterruptedException e)
		{
			return null;
			
		}
		double[][] shortestPths = new double[vertices.length][G[0].length];
		for (MultiShortestPathWorker MPW: MPWs)
		{
			ConcurrentHashMap<Integer, double[]> computedShortestPaths = MPW.getComputedShortestPaths();
			for (Integer v: computedShortestPaths.keySet())
			{
				shortestPths[v] = computedShortestPaths.get(v);
			}
		}
		return shortestPths;
	}


}
