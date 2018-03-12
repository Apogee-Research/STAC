package edu.networkcusp.broker;

import edu.networkcusp.console.Command;
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
    private final ProductIntermediary productIntermediary;

    public BeginCommand(ProductIntermediary productIntermediary) {
        super(COMMAND, "begins powerbroker auction process with the nodes specified in the configuration file and"
                + " the given powerprofile", USAGE, new AggregateCompleter(new ArgumentCompleter(
                new StringsCompleter(COMMAND), new FileNameCompleter())));
        this.productIntermediary = productIntermediary;

    }

    @Override
    public void execute(PrintStream out, CommandLine cmdLine) {
        try {
            List<String> argList = cmdLine.getArgList();
            if (argList.size() != 2) {
                out.println(USAGE);
            } else {
                if (executeTarget(out, argList)) return;


            }
        } catch (Exception e) {
            out.println("Problem processing command: " + e.getMessage());
        }

    }

    private boolean executeTarget(PrintStream out, List<String> argList) throws ProductIntermediaryRaiser {
        String connectName = argList.get(0);
        String productName = argList.get(1);

        File connectFile = new File(connectName);
        File productFile = new File(productName);

        if (!connectFile.exists() || !connectFile.canRead()) {
            out.println("Cannot read " + connectName);
            return true;
        }

        if (!productFile.exists() || !productFile.canRead()) {
            return executeTargetCoordinator(out, productFile);
        }

        // give powerBroker the connect file and the powerprofile
        this.productIntermediary.start(connectFile, productFile);
        return false;
    }

    private boolean executeTargetCoordinator(PrintStream out, File productFile) {
        out.println("Cannot read " + productFile);
        return true;
    }
}
