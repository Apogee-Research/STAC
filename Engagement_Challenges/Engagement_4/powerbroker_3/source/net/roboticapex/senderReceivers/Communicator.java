package net.roboticapex.senderReceivers;


public interface Communicator {
	
	public void transfer(SenderReceiversPublicIdentity dest, byte[] msg) throws SenderReceiversDeviation;
}
