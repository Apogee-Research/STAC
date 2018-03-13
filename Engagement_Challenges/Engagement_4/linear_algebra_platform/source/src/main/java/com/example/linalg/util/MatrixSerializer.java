package com.example.linalg.util;

import java.util.IllegalFormatException;
import java.util.MissingFormatArgumentException;

// Make these methods non static and do a GSON style thing

public class MatrixSerializer {

	/*
	 *  MatrixSerializer serializes and deserializes Matrix
	 * objects from the filesystem 
	 */


	static int MIN_WIDTH = 16;
	
	public MatrixSerializer()
	{
		
	}


	public double[][] readMatrixFromCSV(String filecontents, int numRows, int numCols, int maxsize) throws IllegalFormatException 
	{
		return readMatrixFromCSV(filecontents, numRows, numCols, maxsize, true, true);
	}
	
	public  double[][] readMatrixFromCSV(String filecontents, int numRows, int numCols, int maxsize, boolean enforceWidth, boolean enforceSize) throws IllegalFormatException
	{
		if (enforceSize)
		{
			if (numRows > maxsize || numCols > maxsize)
				throw new IllegalArgumentException("Matrix too large");
		}

		for (int i = 0; i < filecontents.length(); i++)
		{
			if (Character.isDigit(filecontents.charAt(i)))
				continue;
			if (Character.isSpaceChar(filecontents.charAt(i)))
				continue;
			if (filecontents.charAt(i) == '\n')
				continue;
			if (filecontents.charAt(i) == '\r')
				continue;
			if (filecontents.charAt(i) == '.')
				continue;
			if (filecontents.charAt(i) == ',')
				continue;
			throw new IllegalArgumentException("Non-allowed character detected in input matrix");
		}

//		Pattern p = Pattern.compile("[^0-9\n\r,\\.]");
//		Matcher mP = p.matcher(filecontents);
//		if (mP.find())
//		{
//			throw new IllegalArgumentException("Non-allowed character detected in input matrix");
//		}
//		if (!filecontents.matches("[0-9\n\r,\\.]+"))
//		{
//			throw new IllegalArgumentException("Non-allowed character detected in input matrix");
//		}

		double[][] matrix = new double[numRows][numCols];
		int i = 0;

		String[] rows = filecontents.split("\n");
		
		if (rows.length != numRows)
			throw new IllegalArgumentException("Number of rows detected " + rows.length + " does not match expected " + numRows);

		for (String row: rows) 
		{
			String[] vals = row.split(",");

			if (vals.length != numCols)
				throw new IllegalArgumentException("Number of column detected " + vals.length + " does not match expected " + numCols);

			
			for (int j = 0; j < vals.length; j++)
			{
				if (enforceWidth)
				{
					if (vals[j].length() <= MIN_WIDTH)
					{
						System.out.println("col " + j + " = " + vals[j] + "(" + vals[j].length() + ")");
//						System.out.println(row);
						throw new MissingFormatArgumentException("Matrix entries are not fixed width");					
					}
					
				}

				matrix[i][j] = Double.parseDouble(vals[j]);
			}
			i++;

		}
		return matrix;
	}

	public static String matrixToCSV(double[][] A)
	{
		/* 
		 * BufferedWriters are probably the fastest way to do this considering
		 * we are doing a lot of single character prints
		 */

		try
		{
			// Replace with StringBuilder
			
			
			StringBuffer sb = new StringBuffer();
			//PrintWriter sb = new PrintWriter(new BufferedWriter(new FileWriter(p.toFile())));

			for (int i = 0; i < A.length; i++)
			{
				for (int j = 0; j < A[i].length; j++)
				{
					sb.append(String.format("%e", A[i][j]));
					if (j ==  A[i].length - 1)
						sb.append('\n');
					else
						sb.append(',');

				}
			}

			return sb.toString();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}

}
