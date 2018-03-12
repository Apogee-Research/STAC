package net.computerpoint.console;

import java.io.PrintStream;

/**
 * Handles lines that don't seem to be commands
 */
public interface LineManager {

    void handleLine(String line, PrintStream out);

}
