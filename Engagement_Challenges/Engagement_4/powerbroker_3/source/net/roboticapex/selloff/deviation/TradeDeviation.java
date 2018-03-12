package net.roboticapex.selloff.deviation;

/***
 * Parent Exception class that all BidPal exceptions should subclass.
 * BidPal operations should throw the appropriate subclass exception.
 */
public class TradeDeviation extends Exception {
	public TradeDeviation() { super(); }
	public TradeDeviation(String message) { super(message); }
	public TradeDeviation(String message, Throwable cause) { super(message, cause); }
  	public TradeDeviation(Throwable cause) { super(cause); }
}

