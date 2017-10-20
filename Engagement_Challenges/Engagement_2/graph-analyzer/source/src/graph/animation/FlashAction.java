package graph.animation;

import graph.*;
import java.awt.Color;

/**
 * An animation class which flashes a node a number of times.
 *
 * @see Node
 * @see AnimationManager
 * @author Michael Shilman (michaels@eecs.berkeley.edu)
 * @version $Id$
 */
public class FlashAction extends AnimationAction {

    /**
     * The node that is being animated.
     */
    Node node;

    /**
     * The number of times to flash the node.
     */
    int num;

    /**
     * The minimum time between flashes.
     */
    int speed;

    /**
     * Is the current state flashing or not. This gets toggled each time the
     * animation is applied and the minimum flash time has passed.
     */
    boolean flashOn;

    /**
     * The last time that the node was updated.
     */
    long prevFlash = 0;

    /**
     * A new action on the specified node to flash it a certain number of times.
     *
     * @param n	The node to animate.
     * @param num	The number of times to flash the node.
     */
    public FlashAction(Node n, int num) {
        init(n, num, -1);
    }

    /**
     * A new action on the specified node to flash it a certain number of times
     * and at a certain rate.
     *
     * @param n	The node to animate.
     * @param num	The number of times to flash the node.
     * @param speed	The minimum pause between flashes in milliseconds.
     */
    public FlashAction(Node n, int num, int speed) {
        init(n, num, speed);
    }

    /**
     * Initialize a new FlashAction; called by the constructors.
     *
     * @param n	The node to animate.
     * @param num	The number of times to flash the node.
     * @param speed	The minimum pause between flashes in milliseconds.
     */
    void init(Node n, int num, int speed) {
        this.node = n;
        this.num = num;
        this.speed = speed;

        flashOn = false;
    }

    /**
     * Update the animation based on the current time and whether or not the
     * node was flashed last time the function was called.
     *
     * @return <i>true</i> when the action is finished, <i>false</i> otherwise.
     */
    boolean apply() {
        long cur = System.currentTimeMillis();
        if ((cur - prevFlash) > speed) {
            if (num == 0) {
                node.rep.select(false);
                return true;//done
            } else {
                prevFlash = cur;
                flashOn = !flashOn;
                node.rep.select(flashOn);
                num--;
                return false;//still working
            }
        }
        return false;
    }
}
