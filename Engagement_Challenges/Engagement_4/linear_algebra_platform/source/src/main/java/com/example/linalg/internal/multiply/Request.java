package com.example.linalg.internal.multiply;

import java.util.List;

import com.example.linalg.util.Pair;

public class Request {
	/*
	 * Class wrapper used for JSON RPC
	 */
	public String call;
	public List<Pair<Integer, Integer>> updatePoints;
	public List<Double> updates;
	
	public Request(String call, List<Pair<Integer, Integer>> updatePoints, List<Double> updates)
	{
		this.call = call;
		this.updatePoints = updatePoints;
		this.updates = updates;
	}

}
