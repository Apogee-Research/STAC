package com.cyberpointllc.stac.console;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.List;
import jline.console.completer.AggregateCompleter;
import jline.console.completer.ArgumentCompleter;
import jline.console.completer.StringsCompleter;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.lang3.StringUtils;

public class HelpCommand extends Command {

    private static final String COMMAND = "help";

    private Console console;

    public HelpCommand(Console console) {
        super(COMMAND, "Displays help for commands", "help | help <command name>", new  AggregateCompleter(new  ArgumentCompleter(new  StringsCompleter(COMMAND), new  CommandCompleter(console))));
        getOptions().addOption(Option.builder("b").desc("brief help listing").longOpt("brief").hasArg(false).build());
        this.console = console;
    }

    @Override
    public void execute(PrintStream out, CommandLine cmdLine) {
        executeHelper(cmdLine, out);
    }

    private void printCommand(PrintStream out, String cmdName, CommandLine cmdLine) {
        if (!console.hasCommand(cmdName)) {
            out.println("Help: '" + cmdName + "' does not exist");
            return;
        }
        Command cmd = console.getCommand(cmdName);
        boolean brief = cmdLine.hasOption('b');
        if (!brief) {
            printCommandHelper(cmd, cmdName, out);
        } else {
            out.println(cmd.getName() + "\t" + cmd.getDescription());
        }
    }

    private void printAllCommands(PrintStream out, CommandLine cmdLine) {
        List<Command> commands = console.getCommands();
        // find the length of the longest command
        int longestLength = 0;
        for (Command command : commands) {
            if (longestLength < command.getName().length()) {
                longestLength = command.getName().length();
            }
        }
        out.println("Commands:");
        out.println("---------");
        boolean brief = cmdLine.hasOption('b');
        for (Command command : commands) {
            printAllCommandsHelper(command, longestLength, brief, out);
        }
        out.println("");
    }

    private void executeHelper(CommandLine cmdLine, PrintStream out) {
        List<String> argList = cmdLine.getArgList();
        int conditionObj0 = 0;
        if (argList.size() > conditionObj0) {
            for (String cmdName : argList) {
                printCommand(out, cmdName, cmdLine);
            }
        } else {
            printAllCommands(out, cmdLine);
        }
    }

    private void printCommandHelper(Command cmd, String cmdName, PrintStream out) {
        out.println(cmdName + " : ");
        out.println("\t" + cmd.getDescription());
        PrintWriter printWriter = new  PrintWriter(out);
        HelpFormatter formatter = new  HelpFormatter();
        formatter.printHelp(printWriter, formatter.getWidth(), cmd.getUsage(), "", cmd.getOptions(), formatter.getLeftPadding(), formatter.getDescPadding(), "");
        printWriter.flush();
        out.println("");
    }

    private void printAllCommandsHelper(Command command, int longestLength, boolean brief, PrintStream out) {
        if (!brief) {
            int sepLength = (longestLength + 3) - command.getName().length();
            String separator = StringUtils.repeat(' ', sepLength);
            out.println(command.getName() + separator + command.getDescription());
        } else {
            out.println(command.getName());
        }
    }
}
