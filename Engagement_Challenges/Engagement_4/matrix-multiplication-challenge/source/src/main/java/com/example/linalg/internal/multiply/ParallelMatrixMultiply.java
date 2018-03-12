package com.example.linalg.internal.multiply;


import java.util.concurrent.ConcurrentHashMap;

import com.example.linalg.util.Pair;

public class ParallelMatrixMultiply {

	double[][] A; 
	double[][] B; 
	ConcurrentHashMap<Pair<Integer, Integer>, Double> C; 

	int numThreads;
	public RPCServer RPC;
	MultiplicationTaskGenerator taskGenerator;
	int port;
	int partitionSize;
	
	
	/* 
	 * ParallelMatrixMultiply(A, B, t = number_of_threads, k = rows_per_task)
	 * 
	 * Use bulk synchronous parallel paradigm to do matrix multiplication.
	 * 	- Multiplying A*B has n^2 dot product tasks to be performed.
	 *  - These n^2 tasks are distributed among the t processors
	 *  - The n^2 tasks are batched into groups of size k, and then distributed
	 * 
	 * As k -> n, there will be only one large task, taking a lot of IO time
	 * As k -> 1, there will be n^2 tasks, taking a large amount of computation time
	 * 
	 * */

	

	public ParallelMatrixMultiply(double[][] A, double[][] B, int numThreads, int partitionSize, int port)
	{
		// We only handle square matrices in this case
		
		assert !((A[0].length != A.length || B[0].length != B.length || A.length != B.length));
		this.A = A;
		this.B = B;
		this.port = port;
		this.partitionSize = partitionSize;

		// C Holds the result matrix in a hashmap form
		this.C = new ConcurrentHashMap<Pair<Integer, Integer>, Double>();

		this.numThreads = numThreads;
		
		taskGenerator = new MultiplicationTaskGenerator(A,B);


	}

	public double[][] multiply() throws InterruptedException
	{
		this.RPC = new RPCServer(taskGenerator.partitionMatrix(partitionSize), C, port);
		Thread t1 = new Thread(RPC);
		t1.start();
		try
		{
			Thread.sleep(100);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		
		Thread[] threads = new Thread[numThreads];
		for (int t = 0; t < numThreads; t++)
		{
			Thread t2 = new Thread(new RPCClient("localhost", port));
			t2.setName("RPCClient-" + t);
			threads[t] = t2;
			t2.start();
		}
		
		for (Thread t: threads)
		{
			t.join();
		}

		if (C.size() != A.length * A[0].length)
		{
			throw new InterruptedException("Result matrix corrupted");
		}

		double[][] C_r = new double[A.length][A[0].length]; 
		for (Pair<Integer, Integer> p: C.keySet())
		{
			C_r[p.getElement0()][p.getElement1()] = C.get(p);
		}
		return C_r;
	}
}
