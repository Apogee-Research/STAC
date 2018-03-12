package org.digitalapex.trade.deviation;


public class IllegalOperationRaiser extends SelloffRaiser {

	public IllegalOperationRaiser() { super(); }
	public IllegalOperationRaiser(String message) { super(message); }
	public IllegalOperationRaiser(String message, Throwable cause) { super(message, cause); }
  	public IllegalOperationRaiser(Throwable cause) { super(cause); }

	
}
