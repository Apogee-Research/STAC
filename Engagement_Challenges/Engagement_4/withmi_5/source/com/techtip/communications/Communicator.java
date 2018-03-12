package com.techtip.communications;


public interface Communicator {
	
	public void transmit(DialogsPublicIdentity dest, byte[] msg) throws DialogsDeviation;
}
