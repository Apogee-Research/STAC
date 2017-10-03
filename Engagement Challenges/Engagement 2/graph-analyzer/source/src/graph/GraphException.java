package graph;

/**
 * Graph package internal error of any kind.
 *
 * @see FileFormatException
 * @author Michael Shilman (michaels@eecs.berkeley.edu)
 * @version $Id$
 */
public class GraphException extends Exception {

    public GraphException() {
        super();
    }

    public GraphException(String s) {
        super(s);
    }
}
