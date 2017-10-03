package graph.animation;

import graph.*;
import java.awt.Color;

/**
 * A class which animates a node from one position to another along a straight
 * path.
 *
 * @see Node
 * @see AnimationManager
 * @author Michael Shilman (michaels@eecs.berkeley.edu)
 * @version $Id$
 */
public class MoveAction extends AnimationAction {

    /**
     * The node that is being animated.
     */
    Node node;

    /**
     * The initial X coordinate of the node when the action is instantiated.
     */
    double startX;

    /**
     * The initial Y coordinate of the node when the action is instantiated.
     */
    double startY;

    /**
     * The target X coordinate for the node.
     */
    double finishX;

    /**
     * The target Y coordinate for the node.
     */
    double finishY;

    /**
     * The start time of the action, for bookkeeping.
     */
    long startTime;

    /**
     * The finish time of the action, for bookkeeping.
     */
    long finishTime;

    /**
     * A new action on the specified node to move it to the specified position.
     *
     * @param n	The node to animate.
     * @param x	The final X coordinate.
     * @param y	The final Y coordinate.
     * @param dt	The duration of the move.
     */
    public MoveAction(Node n, double x, double y, int dt) {
        node = n;
        startTime = System.currentTimeMillis();
        finishTime = startTime + dt;
        startX = n.x;
        startY = n.y;
        finishX = x;
        finishY = y;
    }

    /**
     * Update the animation based on the current time as a linear interpolation.
     *
     * @return <i>true</i> when the action is finished, <i>false</i> otherwise.
     */
    boolean apply() {
        long currentTime = System.currentTimeMillis();
        if (currentTime >= finishTime) {
            node.x = finishX;//XXX
            node.y = finishY;//XXX
            //org.graph.commons.logging.LogFactory.getLog(null).info("done!");
            return true; //we're done
        } else {
            //interpolate between the two
            double t = ((double) (currentTime - startTime))
                    / ((double) (finishTime - startTime));
            node.x = (startX + (t * (finishX - startX)));
            node.y = (startY + (t * (finishY - startY)));
            //org.graph.commons.logging.LogFactory.getLog(null).info("working... " + t + ", " + node.x + ", " + node.y);
            return false; //we're not done
        }
    }
}
