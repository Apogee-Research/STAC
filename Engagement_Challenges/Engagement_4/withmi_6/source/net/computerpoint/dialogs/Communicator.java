package net.computerpoint.dialogs;


public interface Communicator {
	
	public void deliver(ProtocolsPublicIdentity dest, byte[] msg) throws ProtocolsDeviation;
}
