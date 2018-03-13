package com.example.linalg.external.operations;

import com.example.linalg.external.serialization.OperationRequest;
import com.example.linalg.external.serialization.OperationResponse;

public abstract class LinearAlgebraOperation {
	public final int MAX_SIZE = 1000;
	public String methodName;
	public int methodId;

	public abstract OperationResponse compute(OperationRequest request);


}
