package com.digitalpoint.dialogs;


public interface Communicator {
	
	public void send(SenderReceiversPublicIdentity dest, byte[] msg) throws SenderReceiversException;
}
