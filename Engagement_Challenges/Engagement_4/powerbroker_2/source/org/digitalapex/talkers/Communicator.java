package org.digitalapex.talkers;


public interface Communicator {
	
	public void transmit(TalkersPublicIdentity dest, byte[] msg) throws TalkersRaiser;
}
