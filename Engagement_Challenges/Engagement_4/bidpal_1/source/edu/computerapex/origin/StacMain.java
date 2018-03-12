package edu.computerapex.origin;

import edu.computerapex.buystuff.BidPalCommunicationsHandler;
import edu.computerapex.buystuff.BidPalCommunicationsHandlerBuilder;
import edu.computerapex.dialogs.CommunicationsIdentity;
import edu.computerapex.math.EncryptionPrivateKey;

import java.math.BigInteger;
import java.util.Random;

public class StacMain{

	private static EncryptionPrivateKey privateKey = null;
	private static BidPalCommunicationsHandler handler;
	
	public static void main(String[] args) throws Exception{
		if (args.length!=2){
            mainEngine();
        }
		Random random = new Random();
		random.setSeed(0);
		BigInteger p = BigInteger.probablePrime(256, random);
		BigInteger q = BigInteger.probablePrime(256, random);

		privateKey = new EncryptionPrivateKey(p,q);
		int port = Integer.parseInt(args[0]);
		String name = args[1];
		int maxBid=500;
		CommunicationsIdentity identity = new CommunicationsIdentity(name, privateKey);
		
		handler = new BidPalCommunicationsHandlerBuilder().fixIdentity(identity).assignPort(port).setMaxBid(maxBid).generateBidPalCommunicationsHandler();
		handler.run();
		BidPalHost host = new BidPalHost(handler.pullDriver(), handler);
		host.run();

	}

    private static void mainEngine() {
        StacMainSupervisor.invoke();
    }


    private static class StacMainSupervisor {
        private static void invoke() {
            System.out.println("Please enter a port number and username");
        }
    }
}