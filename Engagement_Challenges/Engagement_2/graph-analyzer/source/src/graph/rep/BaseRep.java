package graph.rep;

import graph.*;
import java.awt.*;

/**
 * A data structure for storing and painting the graphical representation of an
 * Element. This is stored in a separate object which makes it easier to extend
 * or remove.<p>
 *
 * There is also a "selected" representation which is useful for most
 * interactive programs.
 *
 * @see NodeRep
 * @see EdgeRep
 * @see LabelRep
 * @author Michael Shilman (michaels@eecs.berkeley.edu)
 * @version $Id$
 */
public abstract class BaseRep {

    //XXX do we need these?
    public static final int NO_WIDTH = -1;
    public static final int NO_HEIGHT = -1;

    /**
     * To allow for two representations: one when the object is selected, one
     * when it is not selected.
     */
    public boolean selected = false;

    /**
     * If false, don't paint this.
     */
    public boolean show = true;

    /**
     * Paint this at the specified location (overridden by the "show" member
     * variable).
     */
    public abstract void paint(Graphics g, double x, double y);

    /**
     * Paint this in the specified bounding box (overridden by the "show" member
     * variable).
     */
    public abstract void paint(Graphics g, double x, double y, double w, double h);

    /**
     * Select or deselect the element that this represents.
     */
    public void select(boolean val) {
        selected = val;
    }
}
