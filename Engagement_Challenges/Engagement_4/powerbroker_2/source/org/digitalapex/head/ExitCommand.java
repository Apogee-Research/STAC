package org.digitalapex.head;

import org.apache.commons.cli.CommandLine;

import java.io.PrintStream;


public class ExitCommand extends Command {

	private Control control;
	
	public ExitCommand(Control control) {
		super("exit", "Exits the program");
		this.control = control;
	}
	
	@Override
	public void execute(PrintStream out, CommandLine cmd) {
	    out.println("Goodbye");
		control.setShouldExit(true);
	}

}
