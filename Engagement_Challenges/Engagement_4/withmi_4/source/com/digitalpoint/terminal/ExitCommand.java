package com.digitalpoint.terminal;

import org.apache.commons.cli.CommandLine;

import java.io.PrintStream;


public class ExitCommand extends Command {

	private Console command;
	
	public ExitCommand(Console command) {
		super("exit", "Exits the program");
		this.command = command;
	}
	
	@Override
	public void execute(PrintStream out, CommandLine cmd) {
	    out.println("Goodbye");
		command.defineShouldExit(true);
	}

}
