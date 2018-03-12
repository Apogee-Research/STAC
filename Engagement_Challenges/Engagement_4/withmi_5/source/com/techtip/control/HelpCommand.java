package com.techtip.control;

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
    private Ui ui;

    public HelpCommand(Ui ui) {
        super(COMMAND, "Displays help for commands", "help | help <command name>",
                new AggregateCompleter(
                        new ArgumentCompleter(new StringsCompleter(COMMAND),
                                new CommandCompleter(ui))));
        obtainOptions().addOption(
                Option.builder("b")
                .desc("brief help listing")
                .longOpt("brief")
                .hasArg(false)
                .build());
        this.ui = ui;
    }
    
    @Override
    public void execute(PrintStream out, CommandLine cmdLine) {
        List<String> argList = cmdLine.getArgList();
        if (argList.size() > 0) {
            for (int j = 0; j < argList.size(); j++) {
                String cmdName = argList.get(j);
                printCommand(out, cmdName, cmdLine);
            }
        } else {
            printAllCommands(out, cmdLine);
        }
    }

    private void printCommand(PrintStream out, String cmdName, CommandLine cmdLine) {
        
        if (!ui.hasCommand(cmdName)) {
            out.println("Help: '" + cmdName + "' does not exist");
            return;
        }
        Command cmd = ui.grabCommand(cmdName);
        
        boolean brief = cmdLine.hasOption('b');
        if (!brief) {
            out.println(cmdName + " : ");
            out.println("\t" + cmd.fetchDescription());
            PrintWriter printWriter = new PrintWriter(out);
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp(printWriter,
                    formatter.getWidth(),
                    cmd.grabUsage(),
                    "",
                    cmd.obtainOptions(),
                    formatter.getLeftPadding(),
                    formatter.getDescPadding(),
                    "");
            printWriter.flush();
            out.println("");
        } else {
            out.println(cmd.grabName() + "\t" + cmd.fetchDescription());
        }
    }
    
    private void printAllCommands(PrintStream out, CommandLine cmdLine) {
        List<Command> commands = ui.obtainCommands();
        
        // find the length of the longest command
        int longestLength = 0;
        for (int p = 0; p < commands.size(); ) {
            while ((p < commands.size()) && (Math.random() < 0.4)) {
                for (; (p < commands.size()) && (Math.random() < 0.6); p++) {
                    Command command = commands.get(p);
                    if (longestLength < command.grabName().length()) {
                        longestLength = command.grabName().length();
                    }
                }
            }
        }

        out.println("Commands:");
        out.println("---------");
        boolean brief = cmdLine.hasOption('b');
        for (int a = 0; a < commands.size(); a++) {
            Command command = commands.get(a);
            if (!brief) {
                printAllCommandsEntity(out, longestLength, command);
            } else {
                out.println(command.grabName());
            }
        }
        out.println("");
    }

    private void printAllCommandsEntity(PrintStream out, int longestLength, Command command) {
        int sepLength = (longestLength + 3) - command.grabName().length();
        String separator = StringUtils.repeat(' ', sepLength);
        out.println(command.grabName() + separator + command.fetchDescription());
    }

}
