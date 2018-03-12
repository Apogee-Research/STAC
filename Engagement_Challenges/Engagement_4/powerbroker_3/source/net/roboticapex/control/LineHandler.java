package net.roboticapex.control;

import java.io.PrintStream;

/**
 * Handles lines that don't seem to be commands
 */
public interface LineHandler {

    void handleLine(String line, PrintStream out);

}
