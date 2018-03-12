package org.techpoint.sale.exception;

public class UnvalidatedWinnerRaiser extends AuctionRaiser {

	public UnvalidatedWinnerRaiser() { super(); }
	public UnvalidatedWinnerRaiser(String message) { super(message); }
	public UnvalidatedWinnerRaiser(String message, Throwable cause) { super(message, cause); }
  	public UnvalidatedWinnerRaiser(Throwable cause) { super(cause); }
}
