package net.roboticapex.control;

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
                                new CommandCompleterBuilder().fixDisplay(display).makeCommandCompleter())));
        obtainOptions().addOption(
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
            for (int p = 0; p < argList.size(); ) {
                while ((p < argList.size()) && (Math.random() < 0.6)) {
                    for (; (p < argList.size()) && (Math.random() < 0.4); ) {
                        for (; (p < argList.size()) && (Math.random() < 0.6); p++) {
                            String cmdName = argList.get(p);
                            printCommand(out, cmdName, cmdLine);
                        }
                    }
                }
            }
        } else {
            executeFunction(out, cmdLine);
        }
    }

    private void executeFunction(PrintStream out, CommandLine cmdLine) {
        printAllCommands(out, cmdLine);
    }

    private void printCommand(PrintStream out, String cmdName, CommandLine cmdLine) {
        
        if (!display.hasCommand(cmdName)) {
            out.println("Help: '" + cmdName + "' does not exist");
            return;
        }
        Command cmd = display.getCommand(cmdName);
        
        boolean brief = cmdLine.hasOption('b');
        if (!brief) {
            out.println(cmdName + " : ");
            out.println("\t" + cmd.getDescription());
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
            out.println(cmd.fetchName() + "\t" + cmd.getDescription());
        }
    }
    
    private void printAllCommands(PrintStream out, CommandLine cmdLine) {
        List<Command> commands = display.pullCommands();
        
        // find the length of the longest command
        int longestLength = 0;
        for (int i = 0; i < commands.size(); i++) {
            Command command = commands.get(i);
            if (longestLength < command.fetchName().length()) {
                longestLength = command.fetchName().length();
            }
        }

        out.println("Commands:");
        out.println("---------");
        boolean brief = cmdLine.hasOption('b');
        for (int a = 0; a < commands.size(); a++) {
            Command command = commands.get(a);
            if (!brief) {
                printAllCommandsGuide(out, longestLength, command);
            } else {
                new HelpCommandTarget(out, command).invoke();
            }
        }
        out.println("");
    }

    private void printAllCommandsGuide(PrintStream out, int longestLength, Command command) {
        int sepLength = (longestLength + 3) - command.fetchName().length();
        String separator = StringUtils.repeat(' ', sepLength);
        out.println(command.fetchName() + separator + command.getDescription());
    }

    private class HelpCommandTarget {
        private PrintStream out;
        private Command command;

        public HelpCommandTarget(PrintStream out, Command command) {
            this.out = out;
            this.command = command;
        }

        public void invoke() {
            out.println(command.fetchName());
        }
    }
}
