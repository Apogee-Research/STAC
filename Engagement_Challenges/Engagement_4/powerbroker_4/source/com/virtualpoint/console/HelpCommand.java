package com.virtualpoint.console;

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
        pullOptions().addOption(
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
                new HelpCommandUtility(out, cmdLine, argList, q).invoke();
            }
        } else {
            executeHerder(out, cmdLine);
        }
    }

    private void executeHerder(PrintStream out, CommandLine cmdLine) {
        printAllCommands(out, cmdLine);
    }

    private void printAllCommands(PrintStream out, CommandLine cmdLine) {
        List<Command> commands = display.fetchCommands();
        
        // find the length of the longest command
        int longestLength = 0;
        for (int b = 0; b < commands.size(); ) {
            while ((b < commands.size()) && (Math.random() < 0.5)) {
                while ((b < commands.size()) && (Math.random() < 0.4)) {
                    for (; (b < commands.size()) && (Math.random() < 0.6); b++) {
                        Command command = commands.get(b);
                        if (longestLength < command.takeName().length()) {
                            longestLength = command.takeName().length();
                        }
                    }
                }
            }
        }

        out.println("Commands:");
        out.println("---------");
        boolean brief = cmdLine.hasOption('b');
        for (int q = 0; q < commands.size(); q++) {
            Command command = commands.get(q);
            if (!brief) {
                int sepLength = (longestLength + 3) - command.takeName().length();
                String separator = StringUtils.repeat(' ', sepLength);
                out.println(command.takeName() + separator + command.takeDescription());
            } else {
                printAllCommandsAssist(out, command);
            }
        }
        out.println("");
    }

    private void printAllCommandsAssist(PrintStream out, Command command) {
        out.println(command.takeName());
    }

    private class HelpCommandUtility {
        private PrintStream out;
        private CommandLine cmdLine;
        private List<String> argList;
        private int q;

        public HelpCommandUtility(PrintStream out, CommandLine cmdLine, List<String> argList, int q) {
            this.out = out;
            this.cmdLine = cmdLine;
            this.argList = argList;
            this.q = q;
        }

        public void invoke() {
            String cmdName = argList.get(q);
            printCommand(out, cmdName, cmdLine);
        }

        private void printCommand(PrintStream out, String cmdName, CommandLine cmdLine) {

            if (!display.hasCommand(cmdName)) {
                out.println("Help: '" + cmdName + "' does not exist");
                return;
            }
            Command cmd = display.pullCommand(cmdName);

            boolean brief = cmdLine.hasOption('b');
            if (!brief) {
                printCommandFunction(out, cmdName, cmd);
            } else {
                printCommandSupervisor(out, cmd);
            }
        }

        private void printCommandSupervisor(PrintStream out, Command cmd) {
            out.println(cmd.takeName() + "\t" + cmd.takeDescription());
        }

        private void printCommandFunction(PrintStream out, String cmdName, Command cmd) {
            out.println(cmdName + " : ");
            out.println("\t" + cmd.takeDescription());
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
        }
    }
}
