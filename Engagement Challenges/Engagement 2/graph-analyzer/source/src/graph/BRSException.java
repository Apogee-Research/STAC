package brs;

/**
 * BRS package internal error
 *
 * @author Michael Shilman (michaels@eecs.berkeley.edu)
 * @version $Id$
 */
public class BRSException extends Exception {

    public BRSException() {
        super();
    }

    public BRSException(String s) {
        super(s);
    }
}
