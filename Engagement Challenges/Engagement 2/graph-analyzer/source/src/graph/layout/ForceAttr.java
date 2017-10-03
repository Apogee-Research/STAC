package graph.layout;

/**
 * Annotation information for the ForceLayout algorithm.
 *
 * @author Michael Shilman (michaels@eecs.berkeley.edu)
 * @version $Id$
 */
//!public; only let the force.* see it for now
class ForceAttr {

    /**
     * If this is set, the node will be fixed in its current screen position.
     */
    public boolean peg = false;

    /**
     * Temporary variable for the X position of the node.
     */
    public double embX;

    /**
     * Temporary variable for the Y position of the node.
     */
    public double embY;

    /**
     * The mass of the node. Currently this variable is not used, though the
     * algorithm will eventually make heavier nodes move less. This might
     * eventually supercede the "peg" variable.
     */
    public float mass = 1.0f;
}
