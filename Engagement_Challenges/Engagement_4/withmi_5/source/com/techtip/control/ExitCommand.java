package com.techtip.control;

import org.apache.commons.cli.CommandLine;

import java.io.PrintStream;


public class ExitCommand extends Command {

	private Ui ui;
	
	public ExitCommand(Ui ui) {
		super("exit", "Exits the program");
		this.ui = ui;
	}
	
	@Override
	public void execute(PrintStream out, CommandLine cmd) {
	    out.println("Goodbye");
		ui.assignShouldExit(true);
	}

}
