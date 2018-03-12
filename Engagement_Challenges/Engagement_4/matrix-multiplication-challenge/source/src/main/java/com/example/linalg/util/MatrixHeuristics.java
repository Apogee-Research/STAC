package com.example.linalg.util;


public class MatrixHeuristics {


	double[][] matrix;
	static double EPSILON = 0.01;
	
	public MatrixHeuristics(double[][] Matrix)
	{
		this.matrix = Matrix;
	}
	// Sample mean
	public static double mean(double[] m)
	{
		double s = 0;
		for (double d: m)
		{
			double temp = s + d;
			if (Math.abs(temp - s) > (Math.abs(d) + EPSILON))
			{
				// Double precision error
				return 0;
			}
			s += d;
		}
		return s/m.length;
	}

	// Sample moments 
	public static double moment(double[] m, double u,  double pow)
	{
		double s = 0;
		for (double d: m)
		{
			double item = Math.pow(d-u, pow);
			double temp = s + item;
			if (Math.abs(temp - s) > (Math.abs(item) + EPSILON))
			{
				// Double precision error
				return 0;
			}
			s += Math.pow(d-u,pow);
		}
		return s/m.length;
	}
	
	// Sample standard deviation
	public static double sstd(double[] m, double u)
	{
		double s = 0;
		for (double d: m)
		{
			double temp = s + d;
			if (Math.abs(temp - s) > (Math.abs(d) + EPSILON))
			{
				// Double precision error
				return 0;
			}
			s += Math.pow(d-u,2);
		}
		return s/(m.length-1);
	}
	
	public double eval()
	{
		return evalMatrix(this.matrix);
	}

	public static double evalMatrix(double[][] Matrix)
	{
		double[] m = new double[Matrix.length];

		for (int i = 0; i < Matrix.length; i++)
		{
			double u = mean(Matrix[i]);
			double o = sstd(Matrix[i], u);
			double m3 = moment(Matrix[i], u, 3);
			double k = m3/Math.pow(o, 1.5);

			if (Math.abs(o) < EPSILON || Math.abs(m3) < EPSILON)
			{
				m[i] = 0;
			}
			else
			{
				m[i] = k;
			}
		}

		double s = 0;
		for (double d: m)
		{
			double temp = s + d;
			if (Math.abs(temp - s) > (Math.abs(d) + EPSILON))
			{
				// Double precision error
				return 0;
			}
			s += d;
		}
		return s/Matrix.length;

	}

}
