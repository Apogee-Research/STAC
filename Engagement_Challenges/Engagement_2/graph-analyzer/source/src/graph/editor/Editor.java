package graph.editor;

import graph.*;
import java.awt.*;
import java.util.*;

/**
 * A simple graph editor. The editor supports the following operations:
 * <ul>
 * <li><b>Nodes</b> - selecting, moving, creating, and deleting.</li>
 * <li><b>Edges</b> - creating.</li>
 * </ul>
 *
 * When it is about to perform an operation, it first checks with its
 * GraphMonitor. The GraphMonitor is a means for an application to dictate how
 * the editor should behave. If it returns <i>true</i>
 * for an operation, the operation is carried out *AND* the GraphMonitor has
 * some chance to annotate the node in some way or change its appearance or
 * update its internal data structures. Otherwise the operation is cancelled.
 *
 * @see graph.editor.GraphMonitor;
 * @see graph.Node;
 * @see graph.Edge;
 * @see graph.Graph;
 * @author Michael Shilman (michaels@eecs.berkeley.edu)
 * @version $Id$
 */
public class Editor extends Viewer {

    /**
     * In SELECT mode, the user can click on individual nodes, or drag a
     * rectangle. Holding down the SHIFT key adds to the existing selection.
     * Holding down the ALT/META key removes from the existing selection.
     */
    public static final int SELECT_MODE = 0;

    /**
     * In MOVE mode, the user can click on individual nodes or groups and drag
     * them around. Since hierarchy is not yet fully supported, the user can
     * only drag nodes that are on the top level of the hierarchy.
     */
    public static final int MOVE_MODE = 1;

    /**
     * In NODE mode, the user creates new nodes when he clicks the mouse. The
     * nodes are placed in the topmost reference frame.
     */
    public static final int NODE_MODE = 2;

    /**
     * In EDGE mode, the user clicks on a node and drags an edge to another
     * node.
     */
    public static final int EDGE_MODE = 3;

    /**
     * In DELETE mode, the user clicks on a node to destroy it and remove it
     * from the graph. Currently it only deletes nodes in the topmost reference
     * frame.
     */
    public static final int DELETE_MODE = 4;

    /**
     * In UNGROUP mode, the user clicks on a graph to destroy it and promote all
     * its children to the parent frame. Currently it only deletes groups in the
     * topmost reference frame.
     */
    public static final int UNGROUP_MODE = 5;

    /**
     * A reference number for sanity checks.
     */
    public static final int NUM_MODES = 6;

    /**
     * Keeps track of the current node that is being moved in MOVE_MODE.
     */
    Node m_move = null;

    /**
     * Keeps track of the current node that is the tail of a new edge in
     * EDGE_MODE.
     */
    Node m_tail = null;

    /**
     * Keeps track of the current node that is the head of a new edge in
     * EDGE_MODE. While the user is dragging the head around it is an invisible
     * dummy node. When the user finally drops the edge, it is either reassigned
     * to the target node, or deleted if the user drops it above no node.
     */
    Node m_head = null;

    /**
     * The X coordinate of the start position in a move.
     */
    double m_moveStartX = 0;

    /**
     * The Y coordinate of the start position in a move.
     */
    double m_moveStartY = 0;

    /**
     * A possible head for a new edge when the user is dragging it around. This
     * is used to highlight and unhighlight possible nodes as they come and go.
     */
    Node m_possibleHead = null;

    /**
     * The new edge that is being dragged. If the user places a valid edge, this
     * is "commited" to the graph. Otherwise it is detached and
     * garbage-collected.
     */
    Edge m_edge = null;

    /**
     * A counter to generate default labels for new nodes.
     */
    int m_nodeCount = 0;

    /**
     * A counter to generate default labels for new edges.
     */
    int m_edgeCount = 0;

    /**
     * The color of the bounding rectangle in selection.
     */
    Color m_selColor = Color.black;

    /**
     * The primary node selected. (XXX what's the policy here?)
     */
    Node m_primeSel = null;

    /**
     * All the other nodes selected.
     */
    Vector m_sel = new Vector();

    /**
     * Whether or not to automatically label new nodes as they are created.
     */
    boolean m_lblNodes = true;

    /**
     * Whether or not to automatically label new edges as they are created.
     */
    boolean m_lblEdges = false;

    /**
     * The X coordinate of the point where the user starts dragging out a
     * selection.
     */
    int m_startSelX;

    /**
     * The Y coordinate of the point where the user starts dragging out a
     * selection.
     */
    int m_startSelY;

    /**
     * The selection rectangle used for giving visual feedback to the user and
     * for testing containment.
     */
    Rectangle m_selRect = new Rectangle();

    /**
     * Whether or not we are in the middle of a selection drag. This is used to
     * decide whether or not to draw the selection rectangle.
     */
    boolean m_inSel = false;

    /**
     * The GraphMonitor for this editor.
     */
    protected GraphMonitor m_monitor;

    /**
     * The current mode the editor is in.
     */
    public int m_mode = MOVE_MODE;

    /**
     * XXX A really stupid synchronization mechanism which needs to be
     * rethought.
     */
    private boolean m_busy = false;

    /**
     * Construct a new editor with the given monitor and dimensions.
     */
    public Editor(GraphMonitor monitor, int w, int h) {
        super(w, h);
        m_monitor = monitor;
    }

    /**
     * Construct a new editor with the given monitor.
     */
    public Editor(GraphMonitor monitor) {
        super();
        m_monitor = monitor;
    }

    /**
     * Used to set the editing mode. Mode changes are not allowed while the user
     * is in the middle of performing an operation in a particular mode. For
     * example, if the user is in the middle of dragging an edge from node
     * <b>A</b> to <b>B</b>, it would cause funny behavior to put the viewer
     * into the <i>MOVE_MODE</i> state.
     *
     * @return	Whether or not the mode change was possible.
     */
    public boolean setMode(int mode) {
        if (!m_busy && (mode >= SELECT_MODE) && (mode <= NUM_MODES)) {
            m_mode = mode;
            return true;
        }
        return false;
    }

    /**
     * Clear the existing selection. Remove all entries from the selection list,
     * and unhighlight them.
     */
    public void clearSelection() {
        if (!m_busy) {
            for (Enumeration e = m_sel.elements(); e.hasMoreElements();) {
                Node n = (Node) e.nextElement();
                n.rep.select(false);
            }
            m_sel.removeAllElements();
        }
    }

    /**
     * Pick the node at coordinate <i>(x, y)</i>.
     *
     * @param x	The X pick coordinate.
     * @param y	The Y pick coordinate.
     * @return	The picked node, or <i>null</i> if no node found.
     */
    synchronized protected Node pick(int x, int y) {
        for (int i = graph.nodes.size(); i > 0; i--) {
            Node n = (Node) graph.nodes.elementAt(i - 1);
            if ((n.rep != null) && (n.rep.show)
                    && (x >= n.x) && (y >= n.y)
                    && (x < n.x + n.w) && (y < n.y + n.h)) {
                return n;
            }
        }
        return null;
    }

    /**
     * Pick the node whose center is nearest to coordinate <i>(x, y)</i>
     * and which falls within the specified tolerance.
     *
     * @param x	The X pick coordinate.
     * @param y	The Y pick coordinate.
     * @param tol	The square of the maximum distance.
     * @return	The picked node, or <i>null</i> if no node found.
     */
    synchronized protected Node pick(int x, int y, double tol) {
        Node bestNode = null;
        double bestDistance = Double.MAX_VALUE; //start out badly
        for (int i = graph.nodes.size(); i > 0; i--) {
            Node n = (Node) graph.nodes.elementAt(i - 1);
            double centerX = n.x + n.w / 2.0;
            double centerY = n.y + n.h / 2.0;
            double sqdist = (centerX - x) * (centerX - x) + (centerY - y) * (centerY - y);
            if ((sqdist < bestDistance) && (sqdist <= tol)) {
                bestDistance = sqdist;
                bestNode = n;
            }
        }
        return bestNode;
    }

    /**
     * Start a new node, and then make the viewer think it is in MOVE_MODE by
     * setting the <i>m_move</i> variable, without actually changing the mode.
     * This way when the user finally drops the node it will be back in
     * NODE_MODE.
     *
     * @param x	The X coordinate to start at.
     * @param y	The Y coordinate to start at.
     */
    synchronized protected void startNode(int x, int y) {
        clearSelection();
        m_busy = true;
        Node n = new Node();
        n.x = x;
        n.y = y;
        if ((m_monitor == null) || m_monitor.addNode(n)) {
            n.name = "Node_" + (m_nodeCount++);
            if (m_lblNodes) {
                n.lbl.label = n.name;
            }
            graph.nodes.addElement(n);
            m_move = n;
            m_moveStartX = x; //so that the node can be undone if necessary
            m_moveStartY = y;

            n.rep.select(true);
            repaint();
        } else {
            m_busy = false;
        }
    }

    /**
     * If there is a group at the specified coordinate, remove the group and
     * promote its contents to the parent level.
     */
    synchronized boolean ungroup(int x, int y) {
        Node n = pick(x, y);
        if (n != null && (n instanceof Graph)) {
            Graph g = (Graph) n;
            if ((m_monitor == null) || (m_monitor.ungroupGraph(g))) {
                for (Enumeration e = g.nodes.elements(); e.hasMoreElements();) {
                    Node child = (Node) e.nextElement();
                    g.parent.add(child);
                    org.graph.commons.logging.LogFactory.getLog(null).info("Promoting " + child.name);
                }
                graph.delete(g);
                repaint();
                return true;
            }
        }
        return false;
    }

    /**
     * Delete the node or group at (x, y).
     */
    synchronized boolean delete(int x, int y) {
        Node n = pick(x, y);
        if (n != null && ((m_monitor == null) || (m_monitor.deleteNode(n)))) {
            graph.delete(n);
            repaint();
            return true;
        }
        return false;
    }

    /**
     * Start a move operation. Set up some accounting variables such as
     * <i>m_move, m_moveStartX/Y</i> if the user has successfully selected a
     * proper node to move.
     */
    synchronized boolean startMove(int x, int y) {
        clearSelection();
        Node n = pick(x, y);
        if (n != null) {
            m_move = n;
            m_moveStartX = m_move.x; //so that the node can be undone if necessary
            m_moveStartY = m_move.y;
            n.rep.select(true);
            repaint();
            return true;
        }
        return false;
    }

    /**
     * Drag the node being moved to the specified coordinate.
     *
     * @param x	The X coordinate.
     * @param y	The Y coordinate.
     */
    synchronized boolean dragMove(int x, int y) {
        if (m_move != null) {
            m_move.x = x;
            m_move.y = y;
            repaint();
            return true;
        }
        return false;
    }

    /**
     * Drop the node being moved at the specified coordinate. If the monitor
     * disallows the drop, return the node to its original starting place
     * (before the move).
     *
     * @param x	The X coordinate.
     * @param y	The Y coordinate.
     */
    synchronized boolean finishMove(int x, int y) {
        if (m_move != null) {
            if ((m_monitor == null) || m_monitor.moveNode(m_move, x, y)) {
                m_move.x = x;
                m_move.y = y;
            } else {
                m_move.x = m_moveStartX;
                m_move.y = m_moveStartY;
            }

            m_move.rep.select(false);
            m_move = null;
            m_busy = false;

            repaint();
            return true;
        }
        return false;
    }

    /**
     * Start a new edge at the specified coordinate. This function sets up an
     * invisible dummy node that the user drags around.
     */
    synchronized boolean startEdge(int x, int y) {
        clearSelection();
        Node n = pick(x, y);
        if (n != null && ((m_monitor == null)
                || m_monitor.startEdge(m_edge, n))) {
            m_tail = n;
            m_tail.rep.select(true);
            m_head = new Node();
            m_head.name = "Dummy";
            m_head.rep = null;
            m_head.lbl = null;
            m_head.x = x;
            m_head.y = y;
            try {
                m_edge = m_tail.attach(m_head);
            } catch (Exception e) {
                //this should never happen
                org.graph.commons.logging.LogFactory.getLog(null).info(e.toString());
                System.exit(0);
            }
            return true;
        }
        return false;
    }

    /**
     * Drag the invisible dummy node <i>m_head</i> to the specified location,
     * causing the edge to follow.
     */
    synchronized boolean dragEdge(int x, int y) {
        if (m_tail != null && m_head != null) {
            Node n = pick(x, y);
            if (n != null) {
                if (!n.equals(m_possibleHead)) {
                    if (m_possibleHead != null) {
                        m_possibleHead.rep.select(false);
                    }
                    m_possibleHead = n;
                    m_possibleHead.rep.select(true);
                }
            } else {
                if ((m_possibleHead != null) && (m_possibleHead != m_tail)) {
                    m_possibleHead.rep.select(false);
                }
                m_possibleHead = null;
            }
            m_head.x = x;
            m_head.y = y;
            repaint();
            return true;
        }
        return false;
    }

    /**
     * Drag the invisible dummy node <i>m_head</i> to the specified location,
     * causing the edge to follow, then drop the node. If the drop is
     * successful, reassign the head. Otherwise, delete the edge completely.
     */
    synchronized boolean finishEdge(int x, int y) {
        if (m_tail != null && m_head != null) {
            Node n = pick(x, y);
            if (n != null && n != m_tail && ((m_monitor == null)
                    || m_monitor.addEdge(m_edge, m_tail, n))) {
                m_edge.name = "Edge_" + (m_edgeCount++);
                if (m_lblEdges) {
                    m_edge.lbl.label = m_edge.name;
                }
                m_edge.head = n;
                n.in.addElement(m_edge);
            } else {
                m_edge.detach();
            }

            if (m_possibleHead != null) {
                m_possibleHead.rep.select(false);
            }
            m_tail.rep.select(false);
            m_head = m_tail = m_possibleHead = null;
            m_edge = null;
            m_busy = false;
            repaint();
            return true;
        }
        return false;
    }

    /**
     * Update the selection rectangle when the user is dragging it around.
     */
    void updateSel(int x, int y) {
        m_selRect.x = Math.min(m_startSelX, x);
        m_selRect.y = Math.min(m_startSelY, y);
        m_selRect.width = Math.abs(m_startSelX - x);
        m_selRect.height = Math.abs(m_startSelY - y);
    }

    /**
     * Pick all the nodes which lie within the selection rectangle and add them
     * to the current selection. If SHIFT was not pressed, clear the selection
     * first.
     */
    void pickSel() {
        clearSelection();

        Rectangle bounds = new Rectangle();
        for (Enumeration e = graph.nodes.elements(); e.hasMoreElements();) {
            Node n = (Node) e.nextElement();
            bounds.x = (int) n.x;
            bounds.y = (int) n.y;
            bounds.width = (int) n.w;
            bounds.height = (int) n.h;

            if (m_selRect.intersects(bounds)) {
                if (n.rep != null && n.rep.show) {
                    n.rep.select(true);
                    m_sel.addElement(n);
                }
            }
        }
    }

    /**
     * XXX This is a hack. Paint the selection rectangle in the viewer.
     */
    protected void viewerPaint(Graphics g) {
        if (m_inSel) {
            g.setColor(m_selColor);
            g.drawRect(m_selRect.x, m_selRect.y, m_selRect.width, m_selRect.height);
        }
    }

    /**
     * Handle mouse down events. This does different things depending on the
     * mode that the editor is in.
     */
    public boolean mouseDown(Event evt, int x, int y) {
        switch (m_mode) {
            case DELETE_MODE:
                if (delete(x, y)) {
                    return true;
                }
                break;
            case UNGROUP_MODE:
                if (ungroup(x, y)) {
                    return true;
                }
                break;
            case MOVE_MODE:
                if (startMove(x, y)) {
                    return true;
                }
                break;
            case NODE_MODE:
                startNode(x, y);
                return true;
            case EDGE_MODE:
                if (startEdge(x, y)) {
                    return true;
                }
                break;
        }
        //if it makes it here, there was no pick
        if (m_mode == MOVE_MODE) {
            m_inSel = true;
            m_startSelX = x;
            m_startSelY = y;
            updateSel(x, y);
            repaint();
            return true;
        }

        return super.mouseDown(evt, x, y);
    }

    /**
     * Handle mouse drag events. This does different things depending on the
     * mode that the editor is in and also what state it's in.
     */
    public boolean mouseDrag(Event evt, int x, int y) {
        switch (m_mode) {
            case MOVE_MODE:
                if (dragMove(x, y)) {
                    return true;
                }
                break;
            case NODE_MODE:
                if (dragMove(x, y)) {
                    return true;
                }
                return true;
            case EDGE_MODE:
                if (dragEdge(x, y)) {
                    return true;
                }
                break;
        }
        //if it makes it here, there was no pick
        if (m_inSel) {
            updateSel(x, y);
            repaint();
            return true;
        }

        return super.mouseDrag(evt, x, y);
    }

    /**
     * Handle mouse up events. This does different things depending on the mode
     * that the editor is in and also what state it's in.
     */
    public boolean mouseUp(Event evt, int x, int y) {
        switch (m_mode) {
            case MOVE_MODE:
                if (finishMove(x, y)) {
                    return true;
                }
                break;
            case NODE_MODE:
                if (finishMove(x, y)) {
                    return true;
                }
                return true;
            case EDGE_MODE:
                if (finishEdge(x, y)) {
                    return true;
                }
                break;
        }

        if (m_inSel) {
            updateSel(x, y);
            pickSel();
            repaint();
            m_inSel = false;
            return true;
        }

        return super.mouseUp(evt, x, y);
    }

    /**
     * Handle keypress events. This switches between modes if the user isn't in
     * the middle of an operation.
     * <ul>
     * <li><b>n</b> - Node mode.</li>
     * <li><b>e</b> - Edge mode.</li>
     * <li><b>m</b> - Move mode.</li>
     * <li><b>s</b> - Select mode.</li>
     * <li><b>d</b> - Delete mode.</li>
     * </ul>
     *
     * It also handles some basic commands.
     * <ul>
     * <li><b>c</b> - Check the state of the graph for validity.</li>
     * <li><b>DELETE</b> - Delete the selected nodes.</li>
     * </ul>
     */
    public boolean keyDown(Event evt, int key) {
        if (!m_busy) {
            switch (key) {
                case 'n':
                    m_mode = NODE_MODE;
                    return true;
                case 'e':
                    m_mode = EDGE_MODE;
                    return true;
                case 'm':
                    m_mode = MOVE_MODE;
                    return true;
                case 's':
                    m_mode = SELECT_MODE;
                    return true;
                case 'd':
                    m_mode = DELETE_MODE;
                    return true;
                case 'c':
                    graph.check();
                    return true;
                case 127:
                    for (Enumeration e = m_sel.elements(); e.hasMoreElements();) {
                        Node n = (Node) e.nextElement();
                        graph.delete(n);
                    }
                    repaint();
                    return true;
                default:
//		    		org.graph.commons.logging.LogFactory.getLog(null).info("key = " + key);
            }
        }
        return super.keyDown(evt, key);
    }
}
