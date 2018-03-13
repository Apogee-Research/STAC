package com.example.linalg.external.operations;

import com.example.linalg.external.serialization.OperationRequest;
import com.example.linalg.external.serialization.OperationResponse;
import com.example.linalg.internal.graph.path.ParallelMultiSP;
import com.example.linalg.util.MatrixSerializer;

public class ShortestPathOperation extends LinearAlgebraOperation 
{
	String methodName = "cpsp";
	int methodId = 2;
	MatrixSerializer MS = new MatrixSerializer();

	public OperationResponse compute(OperationRequest req)
	{
//		System.out.println("Running algorithm");
		if (req.numberOfArguments != 2 || req.args.length != 2)
		{
			return new OperationResponse(methodId, false, "Missing arguments\n");
		}		
		try
		{

			double[][] matrixA = MS.readMatrixFromCSV(req.args[0].matrix, req.args[0].rows, req.args[0].cols, super.MAX_SIZE);			

			if (matrixA.length != matrixA[0].length)
			{
				return new OperationResponse(methodId, false,"Only square matrices are supported");
			}
			double[][] targetNodes = MS.readMatrixFromCSV(req.args[1].matrix, req.args[1].rows, req.args[1].cols, super.MAX_SIZE, false, false);			
			if (targetNodes.length != 1)
			{
				return new OperationResponse(methodId, false,"Second argument should be a vector");
			}

			if (targetNodes[0].length > Math.log((double) matrixA.length))
			{
				// Arbitrary limit, still needs some testing
				return new OperationResponse(methodId, false,"Too many path operations");

			}

			int[] targets = new int[targetNodes[0].length];
			for (int i = 0; i < targetNodes[0].length; i++)
			{
				targets[i] = ((Double) targetNodes[0][i]).intValue();
			}
//			SingleShortestPath SP = new SingleShortestPath();
//			double[][] apsp = SP.shortestPaths(matrixA, targets);    

			ParallelMultiSP SP = new ParallelMultiSP(matrixA, targets);
			double[][] apsp = SP.compute();
			
			OperationResponse response = new OperationResponse(this.methodId, true, MatrixSerializer.matrixToCSV(apsp) );
			return response;

		}
		catch (Exception e)
		{
			e.printStackTrace();
			return new OperationResponse(methodId, false,e.getLocalizedMessage());
		}

	}
}
