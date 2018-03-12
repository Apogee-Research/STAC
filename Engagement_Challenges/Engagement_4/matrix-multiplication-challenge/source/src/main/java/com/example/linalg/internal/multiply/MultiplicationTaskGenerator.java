package com.example.linalg.internal.multiply;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import com.example.linalg.util.Pair;

public class MultiplicationTaskGenerator {

	public double[][] MATRIX_A;
	public double[][] MATRIX_B;

	/*
	 * For testing we wrote a few matrix generators 
	 */

	
	// Generates a random matrix
	public static double[][] generateRandomMatrix(int n)
	{
		double[][] A = new double[n][n];
		Random R = new Random();
		R.setSeed(System.currentTimeMillis());

		for (int i = 0; i < n; i++)
		{
			for (int j = 0; j < n; j++)
			{
				A[i][j] = R.nextDouble();
			}
		}
		return A;	
	}

	// Generates an n x n identity matrix
	
	public static double[][] eye(int n)
	{
		double[][] A = new double[n][n];

		for (int i = 0; i < n; i++)
		{
			for (int j = 0; j < n; j++)
			{
				if (i == j)
					A[i][j] = 1;
			}
		}
		return A;

	}
	// Generates a constant matrix of size n
	public static double[][] constMat(int n, double k)
	{
		double[][] A = new double[n][n];

		for (int i = 0; i < n; i++)
		{
			for (int j = 0; j < n; j++)
			{
				A[i][j] = k;
			}
		}
		return A;

	}

	public static void printMatrix(double[][] m){
		try{
			int rows = m.length;
			int columns = m[0].length;
			String str = "|\t";

			for(int i=0;i<rows;i++){
				for(int j=0;j<columns;j++){
					str += m[i][j] + "\t";
				}

				System.out.println(str + "|");
				str = "|\t";
			}

		}catch(Exception e){System.out.println("Matrix is empty!!");}
	}


	// In-place matrix transposition
	public void transpose(double[][] matrix)
	{
		for(int i = 0; i < matrix.length; i++) {
			for(int j = i+1; j < matrix[i].length; j++) {
				double temp = matrix[i][j];
				matrix[i][j] = matrix[j][i];
				matrix[j][i] = temp;
			}
		}
	}
	public MultiplicationTaskGenerator(double[][] matrixA, double[][] matrixB)
	{
		this.MATRIX_A = matrixA;
		this.MATRIX_B = matrixB;
		
		/*
		 * (A*B)[i][j] is the dot product of the ith row 
		 * of A and the jth column of B.
		 *  
		 *  The columns of B are the rows of B', and indexing
		 *  by row here is a lot easier than indexing by column
		 *  so we just transpose and use row indexing
		 */
		
		transpose(matrixB);
	}

	List<SubMatrixTask> partitionMatrix(int partitionSize)
	{
		
		/*
		 * There are n^2 dot product tasks to be completed,
		 * so we naively split them into buckets of size partitionSize
		 * 
		 */
		LinkedList<SubMatrixTask> R = new LinkedList<SubMatrixTask>();

		List<Pair<Integer, Integer>> tasks = new LinkedList<Pair<Integer, Integer>>();
		for (int i = 0; i < MATRIX_A.length; i++)
		{
			for (int j = 0; j < MATRIX_B[i].length; j++)
			{
				tasks.add(new Pair<Integer, Integer>(i, j));
			}
		}
		int c = 0;
		SubMatrixTask S = new SubMatrixTask();

		for (Pair<Integer, Integer> task: tasks)
		{
			c++;
			if (c > partitionSize)
			{
				R.add(S);
				c = 0;
				S = new SubMatrixTask();
			}
			S.addRowA(task.getElement0(), MATRIX_A[task.getElement0()]);
			S.addColumnB(task.getElement1(), MATRIX_B[task.getElement1()]);
			S.addTask(task);
			
		}
		// If there are remaining dot products, add them to the tasklist
		if (S.tasks.size() > 0)
		{
			R.add(S);
		}
		return R;

	}

	public class SubMatrixTask
	{
		// Maps subset of rows in A to their contents
		ConcurrentHashMap<Integer, double[]> submatrixA;

		// Maps subset of columns in B to their contents

		ConcurrentHashMap<Integer, double[]> submatrixB;
		
		// List of dot product tasks to perform

		List<Pair<Integer, Integer>> tasks;

		public String toString()
		{
			StringBuilder sb = new StringBuilder();
			for (Pair<Integer, Integer> p: tasks)
			{
				sb.append(p);
				sb.append("\n");
			}
			sb.append("--------");
			
			return sb.toString();
			
		}
		public SubMatrixTask()
		{
			submatrixA = new ConcurrentHashMap<Integer, double[]>();
			submatrixB = new ConcurrentHashMap<Integer, double[]>();
			tasks = new LinkedList<Pair<Integer, Integer>>();
		}

		public void addTask(Pair<Integer, Integer> p, double[] a, double[] b)
		{
			addRowA(p.getElement0(), a);
			addColumnB(p.getElement1(), b);
			addTask(p);
		}

		public void addTask(int i, double[] a, int j, double[] b)
		{
			addRowA(i, a);
			addColumnB(j, b);
			addTask(i, j);
		}
		public void addTask(Pair<Integer, Integer> p)
		{
			tasks.add(p);
		}

		public void addTask(int i, int j)
		{
			tasks.add(new Pair<Integer,Integer>(i, j));
		}
		public void addRowA(int idx, double[] row)
		{
			submatrixA.put(idx, row);
		}
		public void addColumnB(int idx, double[] col)
		{
			submatrixB.put(idx, col);
		}

	}

}
