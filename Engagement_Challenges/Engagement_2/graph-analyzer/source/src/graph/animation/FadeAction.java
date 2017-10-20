package graph.animation;

import graph.*;
import java.awt.Color;

/**
 * A class which fades between the current fill/border colors for a node and a
 * specified set of colors.
 *
 * @see Node
 * @see AnimationManager
 * @author Michael Shilman (michaels@eecs.berkeley.edu)
 * @version $Id$
 */
public class FadeAction extends AnimationAction {

    /**
     * The node that is being animated.
     */
    Node node;

    /**
     * The initial fill color of the node when the action is instantiated.
     *
     * @see graph.NodeRep#fill
     */
    Color startFill = null;

    /**
     * The initial border color of the node when the action is instantiated.
     *
     * @see graph.NodeRep#border
     */
    Color startBorder = null;

    /**
     * The target fill color for the node.
     *
     * @see graph.NodeRep#fill
     */
    Color finishFill = null;

    /**
     * The target border color for the node.
     *
     * @see graph.NodeRep#border
     */
    Color finishBorder = null;

    /**
     * The start time of the action, for bookkeeping.
     */
    long startTime;

    /**
     * The finish time of the action, for bookkeeping.
     */
    long finishTime;

    /**
     * A new action on the specified node to morph it into the specified
     * fill/border colors.
     *
     * @param n	The node to animate.
     * @param fill	The final fill color.
     * @param border The final border color.
     * @param dt	The duration of the fade.
     */
    FadeAction(Node n, Color fill, Color border, int dt) {
        node = n;
        startTime = System.currentTimeMillis();
        finishTime = startTime + dt;
        startFill = ((n.rep.fill == null) ? Color.white : n.rep.fill);
        startBorder = ((n.rep.border == null) ? Color.white : n.rep.border);
        finishFill = fill;
        finishBorder = border;
    }

    /**
     * Update the animation based on the current time as a linear interpolation.
     *
     * @return <i>true</i> when the action is finished, <i>false</i> otherwise.
     */
    boolean apply() {
        long currentTime = System.currentTimeMillis();
        if (currentTime >= finishTime) {
            node.rep.fill = finishFill;
            node.rep.border = finishBorder;
            return true; //we're done
        } else {
            //interpolate between the two
            double t = ((double) (currentTime - startTime))
                    / ((double) (finishTime - startTime));
            double fr = startFill.getRed() + (t * (finishFill.getRed() - startFill.getRed()));
            double fg = startFill.getGreen() + (t * (finishFill.getGreen() - startFill.getGreen()));
            double fb = startFill.getBlue() + (t * (finishFill.getBlue() - startFill.getBlue()));
            node.rep.fill = new Color((int) fr, (int) fg, (int) fb);

            double br = startBorder.getRed() + (t * (finishBorder.getRed() - startBorder.getRed()));
            double bg = startBorder.getGreen() + (t * (finishBorder.getGreen() - startBorder.getGreen()));
            double bb = startBorder.getBlue() + (t * (finishBorder.getBlue() - startBorder.getBlue()));
            node.rep.border = new Color((int) br, (int) bg, (int) bb);

            return false; //we're not done
        }
    }
}
