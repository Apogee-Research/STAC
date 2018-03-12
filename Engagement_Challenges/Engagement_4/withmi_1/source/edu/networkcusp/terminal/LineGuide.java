package edu.networkcusp.terminal;

import java.io.PrintStream;

/**
 * Handles lines that don't seem to be commands
 */
public interface LineGuide {

    void handleLine(String line, PrintStream out);

}
