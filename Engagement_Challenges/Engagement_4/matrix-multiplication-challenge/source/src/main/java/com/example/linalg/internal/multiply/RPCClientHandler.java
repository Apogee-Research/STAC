package com.example.linalg.internal.multiply;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;

import com.example.linalg.internal.multiply.MultiplicationTaskGenerator.SubMatrixTask;
import com.example.linalg.util.Pair;
import com.google.gson.Gson;

public class RPCClientHandler implements Runnable 
{
	/*
	 * RPCClientHandler handles task delivery to thread workers
	 * It uses a very simple json rpc protocol to either deliver
	 * a new task to a worker, or merge the results of a SubMatrixTask
	 * back into the shared result matrix.
	 */
	BlockingQueue<SubMatrixTask> tasks;
	protected Socket clientSocket = null;
	
	// Callback to the RPCServer which maintains the result matrix
	RPCServer callback;
	Gson gson = new Gson();
	

	public void stop()
	{
		try
		{
			clientSocket.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	public void run() 
	{
		try 
		{
			InputStream input  =  clientSocket.getInputStream();
			OutputStream output = clientSocket.getOutputStream();
			BufferedReader bIn = new BufferedReader(new InputStreamReader(input));

			String messageFromServer;
			while(( messageFromServer = bIn.readLine()) != null)
			{
				Request req = gson.fromJson(messageFromServer, Request.class);
				
				// Get a new matrix task if one exists
				if (req.call.equalsIgnoreCase("GET"))
				{
					SubMatrixTask smt = callback.getTask();
					String resp = gson.toJson(smt);
					output.write(resp.getBytes());
					output.write("\n".getBytes());
					output.flush();
				}
				// Place results back in shared state matrix
				if (req.call.equalsIgnoreCase("update"))
				{
					assert(req.updatePoints.size() == req.updates.size());
					for (int i = 0; i < req.updatePoints.size(); i++)
					{
						try
						{
							Pair<Integer, Integer> p = req.updatePoints.get(i);
							Double f = req.updates.get(i);							
							callback.MATRIX_C.put(p,  f);
						}
						catch (Exception e)
						{
							e.printStackTrace();
						}

					}
				}
			}

			output.close();
			input.close();
		}

		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}		


	public RPCClientHandler(BlockingQueue<SubMatrixTask> tasks, Socket socket, RPCServer callback)
	{
		this.clientSocket = socket;
		this.tasks = tasks;
		this.callback = callback;

	}
}
