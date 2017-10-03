package org.tigris.gef.graph;

import java.io.PrintStream;
import java.io.PrintWriter;

/**
 * A wrapper class for exceptions thrown in the graph model layer.
 *
 * @author Bob Tarling
 * @since 29-May-2004
 */
public class GraphModelException extends Exception {

    private static final long serialVersionUID = -6447939901902994312L;
    private Exception cause;

    public GraphModelException(String mess) {
        super(mess);
    }

    public GraphModelException(String mess, Exception e) {
        super(mess);
        cause = e;
    }

    public GraphModelException(Exception e) {
        super();
        cause = e;
    }

    public void printStackTrace() {
        super.printStackTrace();
        if (cause != null) {
            org.graph.commons.logging.LogFactory.getLog(null).info("Caused by:");
            cause.printStackTrace();
        }
    }

    public void printStackTrace(PrintStream s) {
        super.printStackTrace(s);
        if (cause != null) {
            s.println("Caused by:");
            cause.printStackTrace(s);
        }
    }

    public void printStackTrace(PrintWriter s) {
        super.printStackTrace(s);
        if (cause != null) {
            s.println("Caused by:");
            cause.printStackTrace(s);
        }
    }
}
