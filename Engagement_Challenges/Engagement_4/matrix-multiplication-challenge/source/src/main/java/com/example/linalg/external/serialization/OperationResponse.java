package com.example.linalg.external.serialization;

public class OperationResponse {
	
	public boolean success;
	int method;
	String returnValue;
	
	public OperationResponse(int method, boolean success, String returnValue)
	{
		this.success = success;
		this.method = method;
		this.returnValue = returnValue;
	}

}
