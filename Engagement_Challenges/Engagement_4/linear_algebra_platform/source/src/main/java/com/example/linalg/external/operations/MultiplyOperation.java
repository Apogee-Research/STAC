package com.example.linalg.external.operations;

import java.util.Random;

import com.example.linalg.external.serialization.OperationRequest;
import com.example.linalg.external.serialization.OperationResponse;
import com.example.linalg.internal.multiply.ParallelMatrixMultiply;
import com.example.linalg.util.MatrixHeuristics;
import com.example.linalg.util.MatrixSerializer;

public class MultiplyOperation extends LinearAlgebraOperation 
{

	String methodName = "multiply";
	int methodId = 1;
	int numThreads = 2;
	MatrixSerializer MS = new MatrixSerializer();
	
	public OperationResponse compute(OperationRequest req)
	{
		if (req.numberOfArguments != 2 || req.args.length != 2)
		{
			return new OperationResponse(methodId, false, "Missing arguments\n");
		}		
		try
		{
			double[][] matrixA = MS.readMatrixFromCSV(req.args[0].matrix, req.args[0].rows, req.args[0].cols, super.MAX_SIZE);			
			double[][] matrixB = MS.readMatrixFromCSV(req.args[1].matrix, req.args[1].rows, req.args[1].cols,   super.MAX_SIZE);				


			if ((matrixA.length != matrixB.length) || (matrixA[0].length != matrixB[0].length))
			{
				return new OperationResponse(methodId, false,"Only square matrices are supported");

			}
			if ((matrixA.length != req.args[0].rows) || (matrixA[0].length != req.args[0].cols) || (matrixB.length != req.args[1].rows) || (matrixB[0].length != req.args[1].cols))
			{
				String s = "[" + matrixA.length + "," + matrixA[0].length + "] ~= [" + req.args[0].rows + ", " + req.args[0].cols + "] "; 
				String t = "[" + matrixB.length + "," + matrixB[0].length + "] ~= [" + req.args[1].rows + ", " + req.args[1].cols + "] "; 

				return new OperationResponse(methodId, false, "Request is inconsistent with data supplied:\n\t" + s + "\n\t" + t);
			}

			
//			long whole_start = System.currentTimeMillis(); // REMOVE BEFORE DELIVERY


			int n = matrixA.length; // size of n x n matrix

			/*
			 * We use the average skewness as a heuristic to break up task sizes.
			 * It's a bit contrived, but it works.
			 * 
			 * For most randomly generated matrices, we expect teams to use a symmetric
			 * random number generator (gaussian or uniform). For these distributions
			 * the skewness will be zero. 
			 * 
			 */
			
			// .69314718056 = ln(2)
			
			double S = Math.abs(MatrixHeuristics.evalMatrix(matrixA)) -.69314718056;
			
			
			// HERE IS THE VULNERABILITY:

			int k = (int) Math.max((n*n)/this.numThreads,n*n * Math.exp(S)); 
			
			/* 
			 * We set the task group size K = MAX(n^2/2, n^2 * e^S).
			 * For a symmetric matrix, the skewness will be around zero.
			 * So e^S = e^(-ln(2)) => K ~= n^2 /2 for a symmetric matrix
			 */


//			System.out.println("Number of states: " + n);
//			System.out.println("K: " + (double) k/(n*n)); // REMOVE BEFORE DELIVERY
//			System.out.println("S: " + S); // REMOVE BEFORE DELIVERY
			Random R = new Random();

//			long start = System.currentTimeMillis();  // REMOVE BEFORE DELIVERY
			
			// FIX THIS TO ENSURE PORT IS OPEN
			
			int port = R.nextInt((65536 - 1025) + 1) + 1025;

			ParallelMatrixMultiply PM = new ParallelMatrixMultiply(matrixA, matrixB, numThreads, k, port);
			double[][] C = PM.multiply(); 
//			long stop = System.currentTimeMillis();  // REMOVE BEFORE DELIVERY
//			System.out.println((stop-start)); // REMOVE BEFORE DELIVERY
			PM.RPC.stop();
			
			System.gc(); 
			
			OperationResponse response = new OperationResponse(this.methodId, true, MatrixSerializer.matrixToCSV(C));

//			long whole_stop = System.currentTimeMillis(); // REMOVE BEFORE DELIVERY
//			System.out.println("Total time elapsed: " + (whole_stop - whole_start)); // REMOVE BEFORE DELIVERY
			return response;
			

		}
		catch (Exception e)
		{
			return new OperationResponse(methodId, false, e.getMessage());
		}

	}

}
