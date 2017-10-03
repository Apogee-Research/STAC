package graph.animation;

import graph.*;
import java.awt.Color;

/**
 * A class which animates the linear scaling of a node.
 *
 * @see Node
 * @see AnimationManager
 * @author Michael Shilman (michaels@eecs.berkeley.edu)
 * @version $Id$
 */
public class ScaleAction extends AnimationAction {

    /**
     * The node that is being animated.
     */
    Node node;

    /**
     * The initial <i>width</i> of the node when the action is instantiated.
     */
    double startW;

    /**
     * The initial <i>height</i> of the node when the action is instantiated.
     */
    double startH;

    /**
     * The target <i>width</i> for the node.
     */
    double finishW;

    /**
     * The target <i>height</i> for the node.
     */
    double finishH;

    /**
     * Whether or not to scale around the center of the node.
     */
    boolean center = false;

    /**
     * The start time of the action, for bookkeeping.
     */
    long startTime;

    /**
     * The finish time of the action, for bookkeeping.
     */
    long finishTime;

    /**
     * A new action on the specified node to scale it to the specified size.
     *
     * @param n	The node to animate.
     * @param w	The final width.
     * @param w	The final height.
     * @param dt	The duration of the scale.
     */
    ScaleAction(Node n, double w, double h, int dt) {
        node = n;
        startTime = System.currentTimeMillis();
        finishTime = startTime + dt;
        startW = n.w;
        startH = n.h;
        finishW = w;
        finishH = h;
    }

    /**
     * Update the animation based on the current time as a linear interpolation.
     *
     * @return <i>true</i> when the action is finished, <i>false</i> otherwise.
     */
    boolean apply() {
        long currentTime = System.currentTimeMillis();
        if (currentTime >= finishTime) {
            node.w = finishW;//XXX
            node.h = finishH;//XXX
            return true; //we're done
        } else {
            //interpolate between the two
            double t = ((double) (currentTime - startTime))
                    / ((double) (finishTime - startTime));
            node.w = (startW + (t * (finishW - startW)));
            node.h = (startH + (t * (finishH - startH)));
            return false; //we're not done
        }
    }
}
