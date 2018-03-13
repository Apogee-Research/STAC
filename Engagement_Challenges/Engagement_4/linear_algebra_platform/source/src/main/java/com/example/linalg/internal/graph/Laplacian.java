package com.example.linalg.internal.graph;

public class Laplacian {
	
	double[][] A;
	double[][] D;
	
	public Laplacian(double[][] A)
	{
		this.A = A;
		this.D = new double[A.length][A.length];
		
	}
	
	
	public double[][] laplacian()
	{
				
		for (int i = 0; i < A.length; i++)
		{
			int count = 0;
			for (int j = 0; j < A[i].length; j++)
			{
				if (A[i][j] != 0)
					count++;
			}
			D[i][i] = count;
			for (int j = 0; j < A[i].length; j++)
			{
				D[i][j] = D[i][j] - A[i][j];
			}
		}
		return D;
	}

}
