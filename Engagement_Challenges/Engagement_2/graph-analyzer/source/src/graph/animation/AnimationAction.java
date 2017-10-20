package graph.animation;

import graph.*;
import java.awt.Color;

/**
 * A base class from which animation primitives inherit.
 *
 * @see AnimationManager
 * @see FadeAction
 * @see FlashAction
 * @see MoveAction
 * @see ScaleAction
 * @author Michael Shilman (michaels@eecs.berkeley.edu)
 * @version $Id$
 */
public abstract class AnimationAction {

    /**
     * Apply this action with whatever state the action was initialized with
     * and/or has accumulated.
     *
     * @return	Whether or not to continue applying this action. Return
     * <i>true</i> if finished,
     * <i>false</i> otherwise.
     */
    abstract boolean apply();
}
