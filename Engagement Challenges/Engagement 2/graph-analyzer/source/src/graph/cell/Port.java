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
public class Port extends Node {

    /**
     * The pad that corresponds to this node.
     */
    Pad pad = null;

    /**
     * Create a completely empty node.
     */
    public Port() {
        super();
    }

    /**
     * Create a completely empty node at the specified position.
     */
    public Port(double x, double y) {
        super(x, y);
    }

    /**
     * Create a completely empty node in the specified bouding box.
     */
    public Port(double x, double y, double w, double h) {
        super(x, y, w, h);
    }

    /**
     *
     */
    public Port(Pad p) {
        super(p.x, p.y, p.w, p.h);
        pad = p;
    }
}
