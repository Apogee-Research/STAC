package edu.networkcusp.senderReceivers;


public interface Communicator {
	
	public void send(ProtocolsPublicIdentity dest, byte[] msg) throws ProtocolsRaiser;
}
