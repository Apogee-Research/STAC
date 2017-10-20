package graph.layout;

import graph.*;
import graph.rep.*;
import java.awt.*;

/**
 * A dummy node which will assist placement algorithms. Algorithms can clean
 * this up in their <i>finish()</i> method.
 *
 * @see Action
 * @author Michael Shilman (michaels@eecs.berkeley.edu)
 * @version $Id$
 */
class DummyNode extends Node {

    public static int s_forceIndex = AttributeManager.NO_INDEX;
    public Action owner;

    public DummyNode() {
        if (s_forceIndex == AttributeManager.NO_INDEX) {
            s_forceIndex = AttributeManager.getIndex("Force");
        }
        rep.type = NodeRep.OVAL;
        rep.fill = Color.red;
    }

	//public void paint(Graphics g) {
    //do nothing
    //}
}
