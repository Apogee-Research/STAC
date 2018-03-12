package com.virtualpoint.barter.failure;

/***
 * Parent Exception class that all BidPal exceptions should subclass.
 * BidPal operations should throw the appropriate subclass exception.
 */
public class BarterTrouble extends Exception {
	public BarterTrouble() { super(); }
	public BarterTrouble(String message) { super(message); }
	public BarterTrouble(String message, Throwable cause) { super(message, cause); }
  	public BarterTrouble(Throwable cause) { super(cause); }
}

