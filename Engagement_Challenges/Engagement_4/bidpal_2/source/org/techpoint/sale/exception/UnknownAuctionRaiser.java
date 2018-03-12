package org.techpoint.sale.exception;


public class UnknownAuctionRaiser extends AuctionRaiser {

	public UnknownAuctionRaiser() { super(); }
	public UnknownAuctionRaiser(String message) { super(message); }
	public UnknownAuctionRaiser(String message, Throwable cause) { super(message, cause); }
  	public UnknownAuctionRaiser(Throwable cause) { super(cause); }

	
}
