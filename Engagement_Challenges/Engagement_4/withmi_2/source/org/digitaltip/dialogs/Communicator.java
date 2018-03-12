package org.digitaltip.dialogs;


public interface Communicator {
	
	public void transmit(TalkersPublicIdentity dest, byte[] msg) throws TalkersDeviation;
}
