package edu.computerapex.dialogs;


public interface Communicator {
	
	public void deliver(CommunicationsPublicIdentity dest, byte[] msg) throws CommunicationsDeviation;
}
