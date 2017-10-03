package graph;

/**
 * An action that is applied to a graph.
 *
 * @see Graph
 * @author Michael Shilman (michaels@eecs.berkeley.edu)
 * @version $Id$
 */
public interface Action {

    /**
     * Perform the action on the graph from start to finish. Calls the functions
     * init(), step() (possibly multiple times), and finish().
     *
     * @see Action
     */
    public void apply(Graph g);

    /**
     * A one-time initialization so that apply() can be called repeatedly
     * without re-executing redundant work.
     *
     * @see Action#apply
     */
    public void init(Graph g);

    /**
     * Perform a single pass of a multi-pass operation, or the entire operation
     * of a non-iterative action.
     *
     * @see Action#apply
     */
    public void step(Graph g);

    /**
     * A one-time cleanup after the action is finished executing
     *
     * @see Action#apply
     */
    public void finish(Graph g);

	//XXX the certainty with which the action
    //XXX was successful.
    //XXX public float certainty();
}
