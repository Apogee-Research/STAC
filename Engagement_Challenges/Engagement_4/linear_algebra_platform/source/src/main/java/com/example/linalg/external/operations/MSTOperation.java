package com.example.linalg.external.operations;

import com.example.linalg.external.serialization.OperationRequest;
import com.example.linalg.external.serialization.OperationResponse;
import com.example.linalg.internal.graph.SpanningTrees;
import com.example.linalg.util.MatrixSerializer;

public class MSTOperation extends LinearAlgebraOperation 
{
	String methodName = "mst";
	int methodId = 4;

	MatrixSerializer MS = new MatrixSerializer();
	
	public OperationResponse compute(OperationRequest req)
	{
//		long start_time = System.currentTimeMillis();
		if (req.numberOfArguments != 1 || req.args.length != 1)
		{
			return new OperationResponse(methodId, false, "Missing arguments\n");
		}		
		try
		{

			double[][] matrixA = MS.readMatrixFromCSV(req.args[0].matrix, req.args[0].rows, req.args[0].cols,  super.MAX_SIZE);			

			if (matrixA.length != matrixA[0].length)
			{
				return new OperationResponse(methodId, false,"Only square matrices are supported");
			}
			
			double[][] MST = SpanningTrees.MST(matrixA);			
//			System.out.println(System.currentTimeMillis() - start_time);
			OperationResponse response = new OperationResponse(this.methodId, true, MatrixSerializer.matrixToCSV(MST));
			return response;

		}
		catch (Exception e)
		{
			e.printStackTrace();
			return new OperationResponse(methodId, false,e.getLocalizedMessage());
		}

	}
}

