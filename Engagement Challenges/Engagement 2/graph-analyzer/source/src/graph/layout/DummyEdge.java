package graph.layout;

import graph.*;
import java.awt.Graphics;

/**
 * A dummy edge which will assist placement algorithms.<p>
 * Algorithms can clean this up in their <i>finish()</i> method.
 *
 * <pre>
 * XXX this is not yet used
 * </pre>
 *
 * @author Michael Shilman (michaels@eecs.berkeley.edu)
 * @version $Id$
 */
class DummyEdge extends Edge {

    public static int s_forceIndex = AttributeManager.NO_INDEX;
    public Action owner;

    public DummyEdge() {
        if (s_forceIndex == AttributeManager.NO_INDEX) {
            s_forceIndex = AttributeManager.getIndex("Force");
        }

        ForceAttr fa = new ForceAttr();
        fa.peg = true;
        setAttr(s_forceIndex, fa);
    }

    public void paint(Graphics g) {
        //do nothing
    }
}
