package org.techpoint.sale.exception;


public class IllegalOperationRaiser extends AuctionRaiser {

	public IllegalOperationRaiser() { super(); }
	public IllegalOperationRaiser(String message) { super(message); }
	public IllegalOperationRaiser(String message, Throwable cause) { super(message, cause); }
  	public IllegalOperationRaiser(Throwable cause) { super(cause); }

	
}
