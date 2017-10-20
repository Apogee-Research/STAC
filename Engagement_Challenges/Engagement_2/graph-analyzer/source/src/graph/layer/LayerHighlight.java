package graph.layer;

import graph.filter.*;
import graph.*;
import java.awt.*;

/**
 * A class for highlighting layers
 *
 * XXX all of class
 *
 * @author Michael Shilman (michaels@eecs.berkeley.edu)
 * @version $Id$
 */
public class LayerHighlight implements Filter {

    /**
     * Apply this filter to the node, modifying its "show" display field.
     */
    public void apply(Node n) {
    }

    public String getName() {
        return "Layer Highlight";
    }

    /**
     * Build a dialog by which the user can easily configure parameters to the
     * Filter.
     */
    public Frame buildGUI() {
        return new Frame("Layer Highlight");
    }

    public Component buildThumbnail() {
        return null;
    }
}
