package edu.computerapex.buyOp.bad;

/***
 * Parent Exception class that all BidPal exceptions should subclass.
 * BidPal operations should throw the appropriate subclass exception.
 */
public class BarterDeviation extends Exception {
	public BarterDeviation() { super(); }
	public BarterDeviation(String message) { super(message); }
	public BarterDeviation(String message, Throwable cause) { super(message, cause); }
  	public BarterDeviation(Throwable cause) { super(cause); }
}

