package graph.editor;

import graph.*;

/**
 * An interface for a monitor object which cares about when graphs are edited,
 * and can monitor editing operations. The editor asks the monitor, <i>"Can I do
 * [operation]?"</i>
 * and the monitor returns a boolean value which can allow or disallow the
 * operation.
 * <p>
 *
 * Also, the functions are always called right before an operation is going to
 * be performed, so the monitor can invoke some changes in its internal state
 * which reflect the command if it answers in the affirmative.
 * <p>
 *
 * The interface is sort of clunky in that it contains so many functions, and
 * different entities within your application might want to answer different
 * questions and effect different state changes. The best way to do this is that
 * your implementation of GraphMonitor can just parsel out the work to one or
 * more other classes/objects.
 *
 * @see Editor
 * @see EditorMonitor
 * @see Graph
 * @see Edge
 * @see Node
 * @author Michael Shilman (michaels@eecs.berkeley.edu)
 * @version $Id$
 */
public interface GraphMonitor {

    /**
     * This is called when the user tries to add a node to the graph.
     *
     * @return	Whether or not to allow the operation.
     */
    public boolean addNode(Node n);

    /**
     * This is called when the user tries to delete a node from the graph.
     *
     * @return	Whether or not to allow the operation.
     */
    public boolean deleteNode(Node n);

    /**
     * This is called when the user tries to move a node in the graph. It
     * expects that the node has not been moved yet.
     *
     * @return	Whether or not to allow the operation.
     */
    public boolean moveNode(Node n, int x, int y);

    /**
     * This is called when the user tries to select a node in the graph.
     *
     * @return	Whether or not to allow the operation.
     */
    public boolean selectNode(Node n);

    /**
     * This is called when the user tries to deselect a node in the graph.
     *
     * @return	Whether or not to allow the operation.
     */
    public boolean deselectNode(Node n);

    //Edge monitors=================================================
    /**
     * This is called when the user tries to start adding an edge to the graph.
     *
     * @return	Whether or not to allow the operation.
     */
    public boolean startEdge(Edge e, Node tail);

    /**
     * This is called when the user tries to add a edge to the graph. The
     * monitor assumes that the current state of the edge (its <i>head</i> and
     * <i>tail</i>
     * members) is irrelevant, and only considers the
     * <i>head</i> and <i>tail</i> arguments to the function.
     *
     * @return	Whether or not to allow the operation.
     */
    public boolean addEdge(Edge e, Node tail, Node head);

    /**
     * This is called when the user tries to delete an edge from the graph.
     *
     * @return	Whether or not to allow the operation.
     */
    public boolean deleteEdge(Edge e);

    /**
     * This is called when the user tries to select an edge in the graph.
     *
     * @return	Whether or not to allow the operation.
     */
    public boolean selectEdge(Edge e);

    /**
     * This is called when the user tries to move an edge from one node to
     * another. In other words, if an edge was connecting node <b>A</b> and node
     * <b>B</b>, and the user tries to move the edge to connect node <b>A</b> to
     * node <b>C</b>, then this callback would be called.
     * <p>
     *
     * It expects that the <i>head</i> and <i>tail</i>
     * members be set to what they were <b>before</b>
     * the move operation is executed.
     *
     * @param e	The edge to move
     * @param from	The original tail (or head) node for the edge
     * @param to	The proposed new tail (or head) node for the edge
     * @return	Whether or not to allow the operation.
     */
    public boolean moveEdge(Edge e, Node from, Node to);

    //Graph monitors
    /**
     * This is called when the user tries to add a new graph.
     *
     * @return	Whether or not to allow the operation.
     */
    public boolean addGraph(Graph g);

    /**
     * This is called when the user tries to delete a graph.
     *
     * @return	Whether or not to allow the operation.
     */
    public boolean deleteGraph(Graph g);

    /**
     * This is called when the user tries to ungroup a graph.
     *
     * @return	Whether or not to allow the operation.
     */
    public boolean ungroupGraph(Graph g);

    /**
     * This is called when the user tries to select a graph.
     *
     * @return	Whether or not to allow the operation.
     */
    public boolean selectGraph(Graph g);
}
