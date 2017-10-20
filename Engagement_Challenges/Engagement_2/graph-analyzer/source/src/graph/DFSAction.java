package graph;

import graph.*;
import graph.cluster.*;
import graph.rep.*;
import java.awt.*;
import java.util.*;

/**
 * A general DFS action.
 *
 * @author Michael Shilman (michaels@eecs.berkeley.edu)
 * @version $Id$
 */
//XXX
abstract class FooAction {

    abstract void apply(Graph g);
}

public class DFSAction implements Action {

    public FooAction preInit = null;
    public FooAction postInit = null;
    public FooAction preStep = null;
    public FooAction postStep = null;
    public FooAction preFinish = null;
    public FooAction postFinish = null;

    //    A
    //   /|\   If m_parent is A and m_prev is B, then 
    //  B C D  step() will operate on C.
    Node m_parent;
    Node m_prev;

    public void apply(Graph g) {
        init(g);
        while (true /* XXX */) {
            step(g);
        }
    }

    public void init(Graph g) {
        if (preInit != null) {
            preInit.apply(g);
        }
        //unmark all nodes
        if (postInit != null) {
            postInit.apply(g);
        }
    }

    public void step(Graph g) {
        if (preStep != null) {
            preStep.apply(g);
        }
        //mark and recurse
        if (postStep != null) {
            postStep.apply(g);
        }
    }

    public void finish(Graph g) {
        if (preFinish != null) {
            preFinish.apply(g);
        }
        //unmark all nodes
        if (postFinish != null) {
            postFinish.apply(g);
        }
    }
}
