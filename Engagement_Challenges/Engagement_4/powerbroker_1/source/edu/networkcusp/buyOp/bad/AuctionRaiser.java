package edu.networkcusp.buyOp.bad;

/***
 * Parent Exception class that all BidPal exceptions should subclass.
 * BidPal operations should throw the appropriate subclass exception.
 */
public class AuctionRaiser extends Exception {
	public AuctionRaiser() { super(); }
	public AuctionRaiser(String message) { super(message); }
	public AuctionRaiser(String message, Throwable cause) { super(message, cause); }
  	public AuctionRaiser(Throwable cause) { super(cause); }
}

