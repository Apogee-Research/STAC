package org.techpoint.sale.exception;

public class UnexpectedWinningProposalRaiser extends AuctionRaiser {
	public UnexpectedWinningProposalRaiser() { super(); }
	public UnexpectedWinningProposalRaiser(String message) { super(message); }
	public UnexpectedWinningProposalRaiser(String message, Throwable cause) { super(message, cause); }
  	public UnexpectedWinningProposalRaiser(Throwable cause) { super(cause); }
}
