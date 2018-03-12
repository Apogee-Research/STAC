package edu.networkcusp.console;

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
        takeOptions().addOption(
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
            for (int k = 0; k < argList.size(); k++) {
                new HelpCommandUtility(out, cmdLine, argList, k).invoke();
            }
        } else {
            new HelpCommandAid(out, cmdLine).invoke();
        }
    }

    private class HelpCommandUtility {
        private PrintStream out;
        private CommandLine cmdLine;
        private List<String> argList;
        private int a;

        public HelpCommandUtility(PrintStream out, CommandLine cmdLine, List<String> argList, int a) {
            this.out = out;
            this.cmdLine = cmdLine;
            this.argList = argList;
            this.a = a;
        }

        public void invoke() {
            String cmdName = argList.get(a);
            printCommand(out, cmdName, cmdLine);
        }

        private void printCommand(PrintStream out, String cmdName, CommandLine cmdLine) {

            if (!console.hasCommand(cmdName)) {
                out.println("Help: '" + cmdName + "' does not exist");
                return;
            }
            Command cmd = console.grabCommand(cmdName);

            boolean brief = cmdLine.hasOption('b');
            if (!brief) {
                out.println(cmdName + " : ");
                out.println("\t" + cmd.fetchDescription());
                PrintWriter printWriter = new PrintWriter(out);
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp(printWriter,
                        formatter.getWidth(),
                        cmd.getUsage(),
                        "",
                        cmd.takeOptions(),
                        formatter.getLeftPadding(),
                        formatter.getDescPadding(),
                        "");
                printWriter.flush();
                out.println("");
            } else {
                out.println(cmd.grabName() + "\t" + cmd.fetchDescription());
            }
        }
    }

    private class HelpCommandAid {
        private PrintStream out;
        private CommandLine cmdLine;

        public HelpCommandAid(PrintStream out, CommandLine cmdLine) {
            this.out = out;
            this.cmdLine = cmdLine;
        }

        public void invoke() {
            printAllCommands(out, cmdLine);
        }

        private void printAllCommands(PrintStream out, CommandLine cmdLine) {
            List<Command> commands = console.obtainCommands();

            // find the length of the longest command
            int longestLength = 0;
            for (int c = 0; c < commands.size(); c++) {
                Command command = commands.get(c);
                if (longestLength < command.grabName().length()) {
                    longestLength = command.grabName().length();
                }
            }

            out.println("Commands:");
            out.println("---------");
            boolean brief = cmdLine.hasOption('b');
            for (int p = 0; p < commands.size(); p++) {
                Command command = commands.get(p);
                if (!brief) {
                    printAllCommandsGateKeeper(out, longestLength, command);
                } else {
                    printAllCommandsFunction(out, command);
                }
            }
            out.println("");
        }

        private void printAllCommandsFunction(PrintStream out, Command command) {
            out.println(command.grabName());
        }

        private void printAllCommandsGateKeeper(PrintStream out, int longestLength, Command command) {
            int sepLength = (longestLength + 3) - command.grabName().length();
            String separator = StringUtils.repeat(' ', sepLength);
            out.println(command.grabName() + separator + command.fetchDescription());
        }
    }
}
