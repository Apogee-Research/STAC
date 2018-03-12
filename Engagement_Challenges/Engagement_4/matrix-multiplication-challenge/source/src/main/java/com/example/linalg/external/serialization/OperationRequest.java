package com.example.linalg.external.serialization;

public class OperationRequest {
	
	public int operation = -1;
	public int numberOfArguments = -1;
	
	public class Argument
	{
		public int rows;
		public int cols;
		public String matrix;
	}
	
	public Argument[] args;
	

}
