package graph.editor;

import graph.*;

/**
 * A simple graph monitor that okays all operations and has no side-effects.
 *
 * @see graph.editor.Editor;
 * @author Michael Shilman (michaels@eecs.berkeley.edu)
 * @version $Id$
 */
public class EditorMonitor implements GraphMonitor {

    //Node monitors
    public boolean addNode(Node n) {
        return true;
    }

    public boolean deleteNode(Node n) {
        return true;
    }

    public boolean moveNode(Node n, int x, int y) {
        return true;
    }

    public boolean selectNode(Node n) {
        return true;
    }

    public boolean deselectNode(Node n) {
        return true;
    }

    //Edge monitors
    public boolean startEdge(Edge e, Node tail) {
        return true;
    }

    public boolean addEdge(Edge e, Node tail, Node head) {
        return true;
    }

    public boolean deleteEdge(Edge e) {
        return true;
    }

    public boolean selectEdge(Edge e) {
        return true;
    }

    public boolean moveEdge(Edge e, Node from, Node to) {
        return true;
    }

    //Graph monitors
    public boolean addGraph(Graph g) {
        return true;
    }

    public boolean deleteGraph(Graph g) {
        return true;
    }

    public boolean ungroupGraph(Graph g) {
        return true;
    }

    public boolean selectGraph(Graph g) {
        return true;
    }
}
