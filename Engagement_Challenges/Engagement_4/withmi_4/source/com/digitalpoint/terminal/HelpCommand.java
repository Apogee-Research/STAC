package com.digitalpoint.terminal;

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
    private Console command;

    public HelpCommand(Console command) {
        super(COMMAND, "Displays help for commands", "help | help <command name>",
                new AggregateCompleter(
                        new ArgumentCompleter(new StringsCompleter(COMMAND),
                                new CommandCompleter(command))));
        pullOptions().addOption(
                Option.builder("b")
                .desc("brief help listing")
                .longOpt("brief")
                .hasArg(false)
                .build());
        this.command = command;
    }
    
    @Override
    public void execute(PrintStream out, CommandLine cmdLine) {
        List<String> argList = cmdLine.getArgList();
        if (argList.size() > 0) {
            for (int p = 0; p < argList.size(); p++) {
                new HelpCommandGateKeeper(out, cmdLine, argList, p).invoke();
            }
        } else {
            executeCoordinator(out, cmdLine);
        }
    }

    private void executeCoordinator(PrintStream out, CommandLine cmdLine) {
        printAllCommands(out, cmdLine);
    }

    private void printAllCommands(PrintStream out, CommandLine cmdLine) {
        List<Command> commands = command.obtainCommands();
        
        // find the length of the longest command
        int longestLength = 0;
        for (int p = 0; p < commands.size(); ) {
            for (; (p < commands.size()) && (Math.random() < 0.6); ) {
                for (; (p < commands.size()) && (Math.random() < 0.4); p++) {
                    Command command = commands.get(p);
                    if (longestLength < command.fetchName().length()) {
                        longestLength = command.fetchName().length();
                    }
                }
            }
        }

        out.println("Commands:");
        out.println("---------");
        boolean brief = cmdLine.hasOption('b');
        for (int c = 0; c < commands.size(); c++) {
            Command command = commands.get(c);
            if (!brief) {
                printAllCommandsExecutor(out, longestLength, command);
            } else {
                printAllCommandsSupervisor(out, command);
            }
        }
        out.println("");
    }

    private void printAllCommandsSupervisor(PrintStream out, Command command) {
        out.println(command.fetchName());
    }

    private void printAllCommandsExecutor(PrintStream out, int longestLength, Command command) {
        int sepLength = (longestLength + 3) - command.fetchName().length();
        String separator = StringUtils.repeat(' ', sepLength);
        out.println(command.fetchName() + separator + command.pullDescription());
    }

    private class HelpCommandGateKeeper {
        private PrintStream out;
        private CommandLine cmdLine;
        private List<String> argList;
        private int j;

        public HelpCommandGateKeeper(PrintStream out, CommandLine cmdLine, List<String> argList, int j) {
            this.out = out;
            this.cmdLine = cmdLine;
            this.argList = argList;
            this.j = j;
        }

        public void invoke() {
            String cmdName = argList.get(j);
            printCommand(out, cmdName, cmdLine);
        }

        private void printCommand(PrintStream out, String cmdName, CommandLine cmdLine) {

            if (!command.hasCommand(cmdName)) {
                out.println("Help: '" + cmdName + "' does not exist");
                return;
            }
            Command cmd = command.grabCommand(cmdName);

            boolean brief = cmdLine.hasOption('b');
            if (!brief) {
                out.println(cmdName + " : ");
                out.println("\t" + cmd.pullDescription());
                PrintWriter printWriter = new PrintWriter(out);
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp(printWriter,
                        formatter.getWidth(),
                        cmd.obtainUsage(),
                        "",
                        cmd.pullOptions(),
                        formatter.getLeftPadding(),
                        formatter.getDescPadding(),
                        "");
                printWriter.flush();
                out.println("");
            } else {
                printCommandAssist(out, cmd);
            }
        }

        private void printCommandAssist(PrintStream out, Command cmd) {
            out.println(cmd.fetchName() + "\t" + cmd.pullDescription());
        }
    }
}
