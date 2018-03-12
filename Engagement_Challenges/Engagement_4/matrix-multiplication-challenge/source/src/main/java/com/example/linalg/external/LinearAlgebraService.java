package com.example.linalg.external;

import java.io.IOException;
import java.io.InputStreamReader;

import com.example.linalg.external.operations.LaplacianOperation;
import com.example.linalg.external.operations.LinearAlgebraOperation;
import com.example.linalg.external.operations.MSTOperation;
import com.example.linalg.external.operations.MultiplyOperation;
import com.example.linalg.external.operations.ShortestPathOperation;
import com.example.linalg.external.serialization.OperationRequest;
import com.example.linalg.external.serialization.OperationResponse;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import fi.iki.elonen.NanoHTTPD;

public class LinearAlgebraService extends NanoHTTPD
{
	Gson GSON = new Gson();

	public LinearAlgebraService(int p) throws IOException
	{
		super(p);
		start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
		System.out.println("Service has been started, running on port " + p);
	}

	@Override
	public Response serve(IHTTPSession session) 
	{	
		OperationResponse resp = null;
		LinearAlgebraOperation operation = null;
		OperationRequest req = null;
		try
		{
			req = GSON.fromJson(new JsonReader(new InputStreamReader(session.getInputStream())), OperationRequest.class);
			if (req.operation < 0 || req.numberOfArguments < 0)
			{
				return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, NanoHTTPD.MIME_PLAINTEXT, "Missing arguments, method\n");														
			}
			switch (req.operation)
			{
			case 1:
				operation = new MultiplyOperation();
				break;
			case 2:
				operation = new ShortestPathOperation();
				break;
			case 3:
				operation = new LaplacianOperation();
				break;
			case 4:
				operation = new MSTOperation();
				break;

			default:
				break;
			
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, NanoHTTPD.MIME_PLAINTEXT, "Could not parse input request\n");						
		}
		
		if (operation == null || req == null)
		{
			return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, NanoHTTPD.MIME_PLAINTEXT, "Could not find supported operation\n");						
			
		}
		try
		{
			resp = operation.compute(req);
			if (resp.success)
			{
				return newFixedLengthResponse(Response.Status.OK, NanoHTTPD.MIME_PLAINTEXT, GSON.toJson(resp));
			}
			else
			{
				return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, NanoHTTPD.MIME_PLAINTEXT, GSON.toJson(resp));
				
			}
		}
		catch (Exception e)
		{
			return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, NanoHTTPD.MIME_PLAINTEXT, "Unknown error");			
		}


	}


}
