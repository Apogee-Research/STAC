package graph.animation;

import java.util.*;
import java.awt.*;

/**
 * A class which supports efficient animation primitives for nodes. This class
 * is a "singleton" class of which there is only one instance. It maintains a
 * queue of pending AnimationActions, which are removed when they finish
 * executing. It runs in its own threads and executes all actions in the queue
 * while it runs.
 *
 * @see Node
 * @see AnimationAction
 * @author Michael Shilman (michaels@eecs.berkeley.edu)
 * @version $Id$
 */
public class AnimationManager implements Runnable {

    /**
     * The timeout if there are no actions pending.<p>
     * XXX	Should make the thread suspend and be woken up by any new action.
     */
    public static int TIMEOUT = 100;

    /**
     * A queue of the currently executing actions.
     */
    Vector actions = new Vector(10);

    /**
     * The component to repaint after each pass of the run() method.
     */
    protected static Component m_view = null;

    /**
     * The singleton instance of this class. A singleton is an single instance
     * of a class which has a global point of access.
     */
    private static AnimationManager s_this = null;

    /**
     * Makes sure this class is not instantiated elsewhere.
     */
    private AnimationManager() {
    }

    /**
     * Initialize this to refresh a specific view of the data. Create the
     * singleton object s_this.
     *
     * @param view	The view object to be repainted.
     */
    public static synchronized void init(Component view) {
        if (s_this == null) {
            m_view = view;
            s_this = new AnimationManager();
            Thread t = new Thread(s_this);
            t.setPriority(Thread.MIN_PRIORITY);
            t.start();
        } else {
            org.graph.commons.logging.LogFactory.getLog(null).info("AnimationManager already intialized!");
        }
    }

    /**
     * Add an action to the queue. This action will be executed asynchronously
     * in a separate thread.
     *
     * @param na	The action to be executed.
     */
    public static synchronized void addAction(AnimationAction na) {
        s_this.actions.addElement(na);
    }

    /**
     * Perform the animation asynchronously by applying all the actions in the
     * queue and then yielding.
     *
     * @see AnimationAction
     */
    public void run() {
        while (true) {
            if (actions.size() == 0) {
                try {
                    Thread.currentThread().sleep(TIMEOUT);
                } catch (Exception e) {
                }
            } else {
                applyAll();
                m_view.repaint();
                try {
                    Thread.currentThread().yield();
                } catch (Exception e) {
                }
            }
        }
    }

    /**
     * Apply all the actions in the queue, removing the ones that are done
     * executing.
     *
     * @see AnimationAction
     */
    synchronized private void applyAll() {
        for (Enumeration e = actions.elements(); e.hasMoreElements();) {
            AnimationAction a = (AnimationAction) e.nextElement();
            if (a.apply()) {
                actions.removeElement(a);
            }
        }
    }
}
