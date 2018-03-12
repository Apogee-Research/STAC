package edu.networkcusp.console;

import java.io.PrintStream;

/**
 * Handles lines that don't seem to be commands
 */
public interface LineHandler {

    void handleLine(String line, PrintStream out);

}
