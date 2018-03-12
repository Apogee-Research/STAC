package org.digitalapex.powerbroker;

import org.digitalapex.head.Command;
import jline.console.completer.AggregateCompleter;
import jline.console.completer.ArgumentCompleter;
import jline.console.completer.FileNameCompleter;
import jline.console.completer.StringsCompleter;
import org.apache.commons.cli.CommandLine;

import java.io.File;
import java.io.PrintStream;
import java.util.List;

/**
 * This command tells PowerBroker who it should connect to and what power it has available.
 */
public class BeginCommand extends Command {

    private static final String COMMAND = "begin";
    private static final String USAGE = COMMAND + "<connect file> <powerprofile file>";
    private final CommodityGoBetween commodityGoBetween;

    public BeginCommand(CommodityGoBetween commodityGoBetween) {
        super(COMMAND, "begins powerbroker auction process with the nodes specified in the configuration file and"
                + " the given powerprofile", USAGE, new AggregateCompleter(new ArgumentCompleter(
                new StringsCompleter(COMMAND), new FileNameCompleter())));
        this.commodityGoBetween = commodityGoBetween;

    }

    @Override
    public void execute(PrintStream out, CommandLine cmdLine) {
        try {
            List<String> argList = cmdLine.getArgList();
            if (argList.size() != 2) {
                out.println(USAGE);
            } else {
                if (executeHerder(out, argList)) return;


            }
        } catch (Exception e) {
            out.println("Problem processing command: " + e.getMessage());
        }

    }

    private boolean executeHerder(PrintStream out, List<String> argList) throws CommodityGoBetweenRaiser {
        String connectName = argList.get(0);
        String commodityName = argList.get(1);

        File connectFile = new File(connectName);
        File commodityFile = new File(commodityName);

        if (!connectFile.exists() || !connectFile.canRead()) {
            return executeHerderFunction(out, connectName);
        }

        if (!commodityFile.exists() || !commodityFile.canRead()) {
            out.println("Cannot read " + commodityFile);
            return true;
        }

        // give powerBroker the connect file and the powerprofile
        this.commodityGoBetween.start(connectFile, commodityFile);
        return false;
    }

    private boolean executeHerderFunction(PrintStream out, String connectName) {
        out.println("Cannot read " + connectName);
        return true;
    }
}
