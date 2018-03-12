package com.virtualpoint.talkers;


public interface Communicator {
	
	public void transfer(DialogsPublicIdentity dest, byte[] msg) throws DialogsTrouble;
}
