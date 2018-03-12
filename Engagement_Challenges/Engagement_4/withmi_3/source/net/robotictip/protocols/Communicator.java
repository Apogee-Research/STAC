package net.robotictip.protocols;


public interface Communicator {
	
	public void transfer(SenderReceiversPublicIdentity dest, byte[] msg) throws SenderReceiversTrouble;
}
