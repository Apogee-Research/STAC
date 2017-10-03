package graph.cell;

import graph.rep.*;
import graph.*;

/**
 * A port/terminal on a CAD-style component.
 *
 * @see Node
 * @author Michael Shilman (michaels@eecs.berkeley.edu)
 * @version $Id$
 */
public class Pad extends Node {

    /**
     * The port that corresponds to this pad.
     */
    Port port = null;

    /**
     * Create a completely empty node.
     */
    public Pad() {
        super();
    }

    /**
     * Create a completely empty node at the specified position.
     */
    public Pad(double x, double y) {
        super(x, y);
    }

    /**
     * Create a completely empty node in the specified bouding box.
     */
    public Pad(double x, double y, double w, double h) {
        super(x, y, w, h);
    }

    /**
     *
     *
     * public Pad(Port p) { super(p.x, p.y, p.w, p.h); port = p; }
     */
}
