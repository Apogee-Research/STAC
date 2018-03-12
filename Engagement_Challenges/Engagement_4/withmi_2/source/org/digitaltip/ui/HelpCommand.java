package org.digitaltip.ui;

import jline.console.completer.AggregateCompleter;
import jline.console.completer.ArgumentCompleter;
import jline.console.completer.StringsCompleter;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.lang3.StringUtils;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.List;

public class HelpCommand extends Command {

    private static final String COMMAND = "help";
    private Display display;

    public HelpCommand(Display display) {
        super(COMMAND, "Displays help for commands", "help | help <command name>",
                new AggregateCompleter(
                        new ArgumentCompleter(new StringsCompleter(COMMAND),
                                new CommandCompleter(display))));
        takeOptions().addOption(
                Option.builder("b")
                .desc("brief help listing")
                .longOpt("brief")
                .hasArg(false)
                .build());
        this.display = display;
    }
    
    @Override
    public void execute(PrintStream out, CommandLine cmdLine) {
        List<String> argList = cmdLine.getArgList();
        if (argList.size() > 0) {
            for (int q = 0; q < argList.size(); q++) {
                String cmdName = argList.get(q);
                printCommand(out, cmdName, cmdLine);
            }
        } else {
            printAllCommands(out, cmdLine);
        }
    }

    private void printCommand(PrintStream out, String cmdName, CommandLine cmdLine) {
        
        if (!display.hasCommand(cmdName)) {
            out.println("Help: '" + cmdName + "' does not exist");
            return;
        }
        Command cmd = display.fetchCommand(cmdName);
        
        boolean brief = cmdLine.hasOption('b');
        if (!brief) {
            printCommandFunction(out, cmdName, cmd);
        } else {
            out.println(cmd.grabName() + "\t" + cmd.takeDescription());
        }
    }

    private void printCommandFunction(PrintStream out, String cmdName, Command cmd) {
        out.println(cmdName + " : ");
        out.println("\t" + cmd.takeDescription());
        PrintWriter printWriter = new PrintWriter(out);
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(printWriter,
                formatter.getWidth(),
                cmd.grabUsage(),
                "",
                cmd.takeOptions(),
                formatter.getLeftPadding(),
                formatter.getDescPadding(),
                "");
        printWriter.flush();
        out.println("");
    }

    private void printAllCommands(PrintStream out, CommandLine cmdLine) {
        List<Command> commands = display.getCommands();
        
        // find the length of the longest command
        int longestLength = 0;
        for (int i = 0; i < commands.size(); i++) {
            Command command = commands.get(i);
            if (longestLength < command.grabName().length()) {
                longestLength = command.grabName().length();
            }
        }

        out.println("Commands:");
        out.println("---------");
        boolean brief = cmdLine.hasOption('b');
        for (int j = 0; j < commands.size(); j++) {
            Command command = commands.get(j);
            if (!brief) {
                int sepLength = (longestLength + 3) - command.grabName().length();
                String separator = StringUtils.repeat(' ', sepLength);
                out.println(command.grabName() + separator + command.takeDescription());
            } else {
                out.println(command.grabName());
            }
        }
        out.println("");
    }

}
