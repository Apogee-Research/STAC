package com.example.linalg.internal.multiply;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import com.example.linalg.internal.multiply.MultiplicationTaskGenerator.SubMatrixTask;
import com.example.linalg.util.Pair;

public class RPCServer implements Runnable 
{
	/*
	 * This class maintains the shared result state matrix
	 * and the list of submatrix dot producct tasks to be performed.
	 * 
	 * It also manages the server socket and dispatches new handlers
	 * for each thread worker created
	 * 
	 */

	// REMOVE synchronized blocks

	BlockingQueue<SubMatrixTask> tasks;
	ConcurrentHashMap<Pair<Integer, Integer>, Double> MATRIX_C; 

	int          serverPort   =  8082; // make it throw exception
	ServerSocket serverSocket = null;
	boolean      isStopped    = false;
	Thread       runningThread= null;
	LinkedList<RPCClientHandler> clients = new LinkedList<RPCClientHandler>();


	public void run()
	{
		this.runningThread = Thread.currentThread();
		openServerSocket();
		while(! isStopped())
		{
			Socket clientSocket = null;
			try 
			{
				clientSocket = this.serverSocket.accept();
			} 
			catch (IOException e) 
			{
				if(isStopped()) 
				{
					return;
				}
				throw new RuntimeException("Error accepting client connection", e);
			}
			Thread rpcH = new Thread(new RPCClientHandler(tasks, clientSocket, this));
			rpcH.setName("RPCClientHandler-" + rpcH.getId());
			rpcH.start();

			// We dont add these to a linkedlist

		}
		System.out.println("Server Stopped.") ;
	}
	private boolean isStopped() 
	{
		return this.isStopped;
	}

	public void stop()
	{
		this.isStopped = true;
		try 
		{
			this.serverSocket.close();
		} 
		catch (IOException e) 
		{
			throw new RuntimeException("Error closing server", e);
		}
		for (RPCClientHandler client: this.clients)
		{
			if (client != null)
			{
				try
				{
					client.stop();
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}
	}

	private void openServerSocket() 
	{
		try 
		{
			this.serverSocket = new ServerSocket(this.serverPort);
		}
		catch (IOException e) 
		{
			throw new RuntimeException("Cannot open port " + serverPort, e);
		}
	}


	public RPCServer(Collection<SubMatrixTask> c, ConcurrentHashMap<Pair<Integer, Integer>, Double> MAT, int port)
	{
		this.MATRIX_C = MAT;
		this.serverPort = port;
		tasks = new LinkedBlockingQueue<SubMatrixTask>();
		for (SubMatrixTask t: c)
		{
			tasks.add(t);
		}
	}



	public ConcurrentHashMap<Pair<Integer, Integer>, Double> getResult()
	{

		return this.MATRIX_C;
	}
	public boolean hasTask()
	{
		return !tasks.isEmpty();
	}
	public SubMatrixTask getTask()
	{
		try 
		{
			if (!tasks.isEmpty())
				return tasks.take();
			return null;
		} 
		catch (InterruptedException e) 
		{
			e.printStackTrace();
		}
		return null;

	}
}
