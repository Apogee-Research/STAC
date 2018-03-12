package net.computerpoint.console;

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
    private Console console;

    public HelpCommand(Console console) {
        super(COMMAND, "Displays help for commands", "help | help <command name>",
                new AggregateCompleter(
                        new ArgumentCompleter(new StringsCompleter(COMMAND),
                                new CommandCompleter(console))));
        fetchOptions().addOption(
                Option.builder("b")
                .desc("brief help listing")
                .longOpt("brief")
                .hasArg(false)
                .build());
        this.console = console;
    }
    
    @Override
    public void execute(PrintStream out, CommandLine cmdLine) {
        List<String> argList = cmdLine.getArgList();
        if (argList.size() > 0) {
            for (int q = 0; q < argList.size(); q++) {
                executeHelp(out, cmdLine, argList, q);
            }
        } else {
            printAllCommands(out, cmdLine);
        }
    }

    private void executeHelp(PrintStream out, CommandLine cmdLine, List<String> argList, int a) {
        new HelpCommandWorker(out, cmdLine, argList, a).invoke();
    }

    private void printAllCommands(PrintStream out, CommandLine cmdLine) {
        List<Command> commands = console.grabCommands();
        
        // find the length of the longest command
        int longestLength = 0;
        for (int i = 0; i < commands.size(); i++) {
            Command command = commands.get(i);
            if (longestLength < command.takeName().length()) {
                longestLength = command.takeName().length();
            }
        }

        out.println("Commands:");
        out.println("---------");
        boolean brief = cmdLine.hasOption('b');
        for (int c = 0; c < commands.size(); ) {
            while ((c < commands.size()) && (Math.random() < 0.4)) {
                for (; (c < commands.size()) && (Math.random() < 0.4); c++) {
                    Command command = commands.get(c);
                    if (!brief) {
                        printAllCommandsAid(out, longestLength, command);
                    } else {
                        printAllCommandsFunction(out, command);
                    }
                }
            }
        }
        out.println("");
    }

    private void printAllCommandsFunction(PrintStream out, Command command) {
        out.println(command.takeName());
    }

    private void printAllCommandsAid(PrintStream out, int longestLength, Command command) {
        new HelpCommandAid(out, longestLength, command).invoke();
    }

    private class HelpCommandWorker {
        private PrintStream out;
        private CommandLine cmdLine;
        private List<String> argList;
        private int p;

        public HelpCommandWorker(PrintStream out, CommandLine cmdLine, List<String> argList, int p) {
            this.out = out;
            this.cmdLine = cmdLine;
            this.argList = argList;
            this.p = p;
        }

        public void invoke() {
            String cmdName = argList.get(p);
            printCommand(out, cmdName, cmdLine);
        }

        private void printCommand(PrintStream out, String cmdName, CommandLine cmdLine) {

            if (!console.hasCommand(cmdName)) {
                printCommandEngine(out, cmdName);
                return;
            }
            Command cmd = console.pullCommand(cmdName);

            boolean brief = cmdLine.hasOption('b');
            if (!brief) {
                out.println(cmdName + " : ");
                out.println("\t" + cmd.obtainDescription());
                PrintWriter printWriter = new PrintWriter(out);
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp(printWriter,
                        formatter.getWidth(),
                        cmd.takeUsage(),
                        "",
                        cmd.fetchOptions(),
                        formatter.getLeftPadding(),
                        formatter.getDescPadding(),
                        "");
                printWriter.flush();
                out.println("");
            } else {
                out.println(cmd.takeName() + "\t" + cmd.obtainDescription());
            }
        }

        private void printCommandEngine(PrintStream out, String cmdName) {
            out.println("Help: '" + cmdName + "' does not exist");
            return;
        }
    }

    private class HelpCommandAid {
        private PrintStream out;
        private int longestLength;
        private Command command;

        public HelpCommandAid(PrintStream out, int longestLength, Command command) {
            this.out = out;
            this.longestLength = longestLength;
            this.command = command;
        }

        public void invoke() {
            int sepLength = (longestLength + 3) - command.takeName().length();
            String separator = StringUtils.repeat(' ', sepLength);
            out.println(command.takeName() + separator + command.obtainDescription());
        }
    }
}
