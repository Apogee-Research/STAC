package org.techpoint.origin;

import org.techpoint.buystuff.ProposalAppCommsManager;
import org.techpoint.communications.CommsIdentity;
import org.techpoint.mathematic.CryptoSystemPrivateKey;

import java.math.BigInteger;
import java.util.Random;

public class StacMain{

	private static CryptoSystemPrivateKey privateKey = null;
	private static ProposalAppCommsManager manager;
	
	public static void main(String[] args) throws Exception{
		if (args.length!=2){
			System.out.println("Please enter a port number and username");
		}
		Random random = new Random();
		random.setSeed(0);
		BigInteger p = BigInteger.probablePrime(256, random);
		BigInteger q = BigInteger.probablePrime(256, random);

		privateKey = new CryptoSystemPrivateKey(p,q);
		int port = Integer.parseInt(args[0]);
		String name = args[1];
		int maxProposal =500;
		CommsIdentity identity = new CommsIdentity(name, privateKey);
		
		manager = new ProposalAppCommsManager(identity, port, maxProposal);
		manager.run();
		ProposalAppPlace place = new ProposalAppPlace(manager.fetchDirector(), manager);
		place.run();

	}
	
	
}