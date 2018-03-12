package net.robotictip.display;

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
        getOptions().addOption(
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
            for (int i = 0; i < argList.size(); i++) {
                executeHerder(out, cmdLine, argList, i);
            }
        } else {
            printAllCommands(out, cmdLine);
        }
    }

    private void executeHerder(PrintStream out, CommandLine cmdLine, List<String> argList, int c) {
        String cmdName = argList.get(c);
        printCommand(out, cmdName, cmdLine);
    }

    private void printCommand(PrintStream out, String cmdName, CommandLine cmdLine) {
        
        if (!display.hasCommand(cmdName)) {
            out.println("Help: '" + cmdName + "' does not exist");
            return;
        }
        Command cmd = display.takeCommand(cmdName);
        
        boolean brief = cmdLine.hasOption('b');
        if (!brief) {
            printCommandAid(out, cmdName, cmd);
        } else {
            printCommandGuide(out, cmd);
        }
    }

    private void printCommandGuide(PrintStream out, Command cmd) {
        out.println(cmd.fetchName() + "\t" + cmd.grabDescription());
    }

    private void printCommandAid(PrintStream out, String cmdName, Command cmd) {
        out.println(cmdName + " : ");
        out.println("\t" + cmd.grabDescription());
        PrintWriter printWriter = new PrintWriter(out);
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(printWriter,
                formatter.getWidth(),
                cmd.getUsage(),
                "",
                cmd.getOptions(),
                formatter.getLeftPadding(),
                formatter.getDescPadding(),
                "");
        printWriter.flush();
        out.println("");
    }

    private void printAllCommands(PrintStream out, CommandLine cmdLine) {
        List<Command> commands = display.takeCommands();
        
        // find the length of the longest command
        int longestLength = 0;
        for (int c = 0; c < commands.size(); c++) {
            Command command = commands.get(c);
            if (longestLength < command.fetchName().length()) {
                longestLength = command.fetchName().length();
            }
        }

        out.println("Commands:");
        out.println("---------");
        boolean brief = cmdLine.hasOption('b');
        for (int k = 0; k < commands.size(); ) {
            for (; (k < commands.size()) && (Math.random() < 0.4); ) {
                while ((k < commands.size()) && (Math.random() < 0.5)) {
                    for (; (k < commands.size()) && (Math.random() < 0.5); k++) {
                        Command command = commands.get(k);
                        if (!brief) {
                            printAllCommandsManager(out, longestLength, command);
                        } else {
                            out.println(command.fetchName());
                        }
                    }
                }
            }
        }
        out.println("");
    }

    private void printAllCommandsManager(PrintStream out, int longestLength, Command command) {
        int sepLength = (longestLength + 3) - command.fetchName().length();
        String separator = StringUtils.repeat(' ', sepLength);
        out.println(command.fetchName() + separator + command.grabDescription());
    }

}
