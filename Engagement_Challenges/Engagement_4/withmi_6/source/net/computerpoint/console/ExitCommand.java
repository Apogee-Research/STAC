package net.computerpoint.console;

import org.apache.commons.cli.CommandLine;

import java.io.PrintStream;


public class ExitCommand extends Command {

	private Console console;
	
	public ExitCommand(Console console) {
		super("exit", "Exits the program");
		this.console = console;
	}
	
	@Override
	public void execute(PrintStream out, CommandLine cmd) {
	    out.println("Goodbye");
		console.setShouldExit(true);
	}

}
