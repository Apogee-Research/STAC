package org.techpoint.communications;


public interface Communicator {
	
	public void transmit(CommsPublicIdentity dest, byte[] msg) throws CommsRaiser;
}
