package graph.dot;

import graph.*;
import java.awt.Color;
import java.util.Hashtable;
import java.util.Enumeration;
import java.awt.Frame;

/**
 * Take DOT attributes and map them onto the display NOTE: For now this
 * implementation assumes that the data is only read in once from a file, and
 * then it caches it for the rest of the life of the filter.
 *
 * @author Michael Shilman (michaels@eecs.berkeley.edu)
 * @version $Id$
 */
public class DotInfo {

    public Hashtable props = null;
    int x = 0;
    int y = 0;
    int w = 0;
    int h = 0;
    Color color;
    boolean firstTime = true;
}
