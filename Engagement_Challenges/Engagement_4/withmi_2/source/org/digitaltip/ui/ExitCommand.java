package org.digitaltip.ui;

import org.apache.commons.cli.CommandLine;

import java.io.PrintStream;


public class ExitCommand extends Command {

	private Display display;
	
	public ExitCommand(Display display) {
		super("exit", "Exits the program");
		this.display = display;
	}
	
	@Override
	public void execute(PrintStream out, CommandLine cmd) {
	    out.println("Goodbye");
		display.fixShouldExit(true);
	}

}
