package org.digitalapex.head;

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
    private Control control;

    public HelpCommand(Control control) {
        super(COMMAND, "Displays help for commands", "help | help <command name>",
                new AggregateCompleter(
                        new ArgumentCompleter(new StringsCompleter(COMMAND),
                                new CommandCompleter(control))));
        takeOptions().addOption(
                Option.builder("b")
                .desc("brief help listing")
                .longOpt("brief")
                .hasArg(false)
                .build());
        this.control = control;
    }
    
    @Override
    public void execute(PrintStream out, CommandLine cmdLine) {
        List<String> argList = cmdLine.getArgList();
        if (argList.size() > 0) {
            for (int a = 0; a < argList.size(); a++) {
                String cmdName = argList.get(a);
                printCommand(out, cmdName, cmdLine);
            }
        } else {
            printAllCommands(out, cmdLine);
        }
    }

    private void printCommand(PrintStream out, String cmdName, CommandLine cmdLine) {
        
        if (!control.hasCommand(cmdName)) {
            out.println("Help: '" + cmdName + "' does not exist");
            return;
        }
        Command cmd = control.getCommand(cmdName);
        
        boolean brief = cmdLine.hasOption('b');
        if (!brief) {
            printCommandGateKeeper(out, cmdName, cmd);
        } else {
            printCommandExecutor(out, cmd);
        }
    }

    private void printCommandExecutor(PrintStream out, Command cmd) {
        out.println(cmd.takeName() + "\t" + cmd.obtainDescription());
    }

    private void printCommandGateKeeper(PrintStream out, String cmdName, Command cmd) {
        out.println(cmdName + " : ");
        out.println("\t" + cmd.obtainDescription());
        PrintWriter printWriter = new PrintWriter(out);
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(printWriter,
                formatter.getWidth(),
                cmd.pullUsage(),
                "",
                cmd.takeOptions(),
                formatter.getLeftPadding(),
                formatter.getDescPadding(),
                "");
        printWriter.flush();
        out.println("");
    }

    private void printAllCommands(PrintStream out, CommandLine cmdLine) {
        List<Command> commands = control.obtainCommands();
        
        // find the length of the longest command
        int longestLength = 0;
        for (int a = 0; a < commands.size(); a++) {
            Command command = commands.get(a);
            if (longestLength < command.takeName().length()) {
                longestLength = command.takeName().length();
            }
        }

        out.println("Commands:");
        out.println("---------");
        boolean brief = cmdLine.hasOption('b');
        for (int p = 0; p < commands.size(); p++) {
            Command command = commands.get(p);
            if (!brief) {
                int sepLength = (longestLength + 3) - command.takeName().length();
                String separator = StringUtils.repeat(' ', sepLength);
                out.println(command.takeName() + separator + command.obtainDescription());
            } else {
                out.println(command.takeName());
            }
        }
        out.println("");
    }

}
