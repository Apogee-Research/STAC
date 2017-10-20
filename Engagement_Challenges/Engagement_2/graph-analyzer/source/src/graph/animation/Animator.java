package graph.animation;

import graph.*;
import java.util.*;
import java.awt.*;

/**
 * A utility class for animations. This class makes it really simple to use the
 * animation facilities. The tradeoff is that it masks much of the flexibility
 * of the underlying animation system.
 *
 * @see Node
 * @see AnimationAction
 * @author Michael Shilman (michaels@eecs.berkeley.edu)
 * @version $Id$
 */
public class Animator {

    /**
     * This function must be called before any animation call will work
     * properly. It performs the AnimationManager initialization so that a user
     * who only uses the Animator won't ever have to touch the AnimationManager.
     *
     * @see AnimationManager#init
     * @param c	The component that needs to be repainted after each step in the
     * animation.
     */
    public static void init(Component c) {
        //XXX
    }

    /**
     * Flash this node to highlight it.
     *
     * @param n	The number of times to flash the node.
     * @see FlashAction
     * @see AnimationManager
     */
    public static void flash(Node n, int num) {
        AnimationManager.addAction(new FlashAction(n, num, 100));
    }

    /**
     * Animate the motion of this node.
     *
     * @param x	The destination X coordinate.
     * @param y	The destination Y coordinate.
     * @see MoveAction
     * @see AnimationManager
     */
    public static void move(Node n, double x, double y) {
        AnimationManager.addAction(new MoveAction(n, x, y, 1000));
    }

    /**
     * Animate a color fade on the node.
     *
     * @param x	The destination X coordinate.
     * @param y	The destination Y coordinate.
     * @see FadeAction
     * @see AnimationManager
     */
    public static void fade(Node n, Color fill, Color border) {
        AnimationManager.addAction(new FadeAction(n, fill, border, 1000));
    }

    /**
     * Animate a scaling on the node.
     *
     * @param x	The destination X coordinate.
     * @param y	The destination Y coordinate.
     * @see FadeAction
     * @see AnimationManager
     */
    public static void scale(Node n, double w, double h) {
        AnimationManager.addAction(new ScaleAction(n, w, h, 1000));
    }
}
