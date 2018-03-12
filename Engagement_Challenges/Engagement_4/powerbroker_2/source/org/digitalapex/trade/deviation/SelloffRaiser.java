package org.digitalapex.trade.deviation;

/***
 * Parent Exception class that all BidPal exceptions should subclass.
 * BidPal operations should throw the appropriate subclass exception.
 */
public class SelloffRaiser extends Exception {
	public SelloffRaiser() { super(); }
	public SelloffRaiser(String message) { super(message); }
	public SelloffRaiser(String message, Throwable cause) { super(message, cause); }
  	public SelloffRaiser(Throwable cause) { super(cause); }
}

