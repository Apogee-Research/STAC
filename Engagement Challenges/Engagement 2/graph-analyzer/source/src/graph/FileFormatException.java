package graph;

/**
 * Error in the format of a Dot (<i>.dot</i>) or Adjacency (<i>.adj</i> file.
 *
 * @author Michael Shilman (michaels@eecs.berkeley.edu)
 * @version $Id$
 */
public class FileFormatException extends GraphException {

    public FileFormatException() {
        super();
    }

    public FileFormatException(String s) {
        super(s);
    }
}
