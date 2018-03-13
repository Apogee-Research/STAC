package com.example.linalg.internal.multiply;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.LinkedList;

import com.example.linalg.internal.multiply.MultiplicationTaskGenerator.SubMatrixTask;
import com.example.linalg.util.Pair;
import com.google.gson.Gson;

public class RPCClient implements Runnable 
{
	/*
	 * RPCClient is the dot product task worker. It updates the
	 * global state maintained in RPCServer via a JSON RPC
	 * bridge handled on the other side by RPCClientHandler
	 */

	Socket clientSocket;
	Gson GSON = new Gson();

	public void stop()
	{

	}
	public void run()
	{
		DataOutputStream output;
		BufferedReader input;

		/*
		 * CODE REVIEW: use label instead of flag
		 * boolean dontBreak = true; 
		 * 
		 */
		try 
		{
			output = new DataOutputStream(clientSocket.getOutputStream());
			input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			outerloop:
				while (true) // Eliminated flag and used a labeled break
				{
					Request R = new Request("get", null, null);
					output.write(GSON.toJson(R).getBytes());
					output.write("\n".getBytes());
					output.flush();
					String messageFromServer;
					while(( messageFromServer = input.readLine()) != null)
					{
						SubMatrixTask smt = GSON.fromJson(messageFromServer, SubMatrixTask.class);
						if (smt == null)
						{
							//dontBreak = false;
							break  outerloop; 
						}					
						LinkedList<Double> updateValues = new LinkedList<Double>();
						for (Pair<Integer, Integer> task: smt.tasks)
						{
							double[] a = smt.submatrixA.get(task.getElement0());
							double[] b = smt.submatrixB.get(task.getElement1());
							double c = 0;
							for (int i = 0; i < a.length; i++)
							{
								c += a[i] * b[i];
							}
							updateValues.add(c);
						}
						Request updateRequest = new Request("update", smt.tasks, updateValues);
						output.write(GSON.toJson(updateRequest).getBytes());
						output.write("\n".getBytes());
						output.flush();

						break;
					}
				}
			input.close();
			output.close();
			clientSocket.close();

		} 
		catch (IOException e) 
		{
			e.printStackTrace();
			try
			{
				clientSocket.close();				
			}
			catch (Exception e2)
			{
				e2.printStackTrace();
			}
		}   
	}


	public RPCClient(String server, int port)
	{
		try 
		{
			clientSocket = new Socket(server, port);
		} 
		catch (UnknownHostException e) 
		{
			e.printStackTrace();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}	
	}

}
