package edu.networkcusp.protocols;


public interface Communicator {
	
	public void deliver(CommunicationsPublicIdentity dest, byte[] msg) throws CommunicationsFailure;
}
