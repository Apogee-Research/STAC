// $Id: ModeCreateEdgeAndNode.java 1306 2011-04-17 22:39:18Z bobtarling $
// Copyright (c) 1996-2009 The Regents of the University of California. All
// Rights Reserved. Permission to use, copy, modify, and distribute this
// software and its documentation without fee, and without a written
// agreement is hereby granted, provided that the above copyright notice
// and this paragraph appear in all copies.  This software program and
// documentation are copyrighted by The Regents of the University of
// California. The software program and documentation are supplied "AS
// IS", without any accompanying services from The Regents. The Regents
// does not warrant that the operation of the program will be
// uninterrupted or error-free. The end-user understands that the program
// was developed for research purposes and is advised not to rely
// exclusively on the program for any reason.  IN NO EVENT SHALL THE
// UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT,
// SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES, INCLUDING LOST PROFITS,
// ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
// THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
// SUCH DAMAGE. THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY
// WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
// MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
// PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
// CALIFORNIA HAS NO OBLIGATIONS TO PROVIDE MAINTENANCE, SUPPORT,
// UPDATES, ENHANCEMENTS, OR MODIFICATIONS.
package org.tigris.gef.base;

import java.awt.Cursor;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Iterator;
import java.util.List;

import org.graph.commons.logging.Log;
import org.graph.commons.logging.LogFactory;

import org.tigris.gef.graph.GraphModel;
import org.tigris.gef.graph.GraphNodeHooks;
import org.tigris.gef.graph.GraphNodeRenderer;
import org.tigris.gef.graph.MutableGraphModel;
import org.tigris.gef.presentation.Fig;
import org.tigris.gef.presentation.FigEdge;
import org.tigris.gef.presentation.FigNode;
import org.tigris.gef.presentation.FigPoly;
import org.tigris.gef.presentation.Handle;
import org.tigris.gef.util.Localizer;

/**
 * A Mode to interpret user input while creating an edge. Basically mouse down
 * starts creating an edge from a source port Fig, mouse motion paints a
 * rubberband line, mouse up finds the destination port and finishes creating
 * the edge and makes an FigEdge and sends it to the back of the Layer.
 *
 * The argument "edgeClass" determines the type if edge to suggest that the
 * Editor's GraphModel construct. The GraphModel is responsible for actually
 * making an edge in the underlying model and connecting it to other model
 * elements.
 *
 * @author jrobbins
 */
public class ModeCreateEdgeAndNode extends ModeCreate {

    /**
     * Logger.
     */
    private static final Log LOG = LogFactory
            .getLog(ModeCreateEdgeAndNode.class);

    /**
     * The NetPort where the arc is painted from.
     */
    private Object startPort;

    /**
     * The Fig that presents the starting NetPort.
     */
    private Fig startPortFig;

    /**
     * The FigNode on the NetNode that owns the start port.
     */
    private FigNode sourceFigNode;

    /**
     * The new NetEdge that is being created.
     */
    private Object newEdge;

    /**
     * False if drawing from source and destination. True if drawing from
     * destination to source.
     */
    private boolean destToSource = false;

    /**
     * The number of points added so far.
     */
    // protected int _npoints = 0;
    private Handle handle = new Handle(-1);

    private FigNode fn;

    private FigEdge fe;

    private boolean postProcessEdge = false;

    /**
     * The constructor.
     *
     * The empty constructor is not required
     */
    public ModeCreateEdgeAndNode() {
        super();
    }

    /**
     * The constructor.
     * <p>
     *
     * Preferably use the constructor
     * {@link #ModeCreateEdgeAndNode(Editor, Object, boolean, SelectionButtons)}
     * below, since it allows a better mechanism to create the node.
     *
     * @param ed the parent editor of this mode. Each Mode instance belongs to
     * exactly one Editor instance.
     * @param edgeType the class of the edge
     * @param nodeType the class of the node
     * @param post if true, then the edge is postprocessed. See
     * postProcessEdge().
     */
    public ModeCreateEdgeAndNode(Editor ed, Object edgeType, Object nodeType,
            boolean post) {
        super(ed);
        setArg("edgeClass", edgeType);
        setArg("nodeClass", nodeType);
        postProcessEdge = post;
        LOG.debug("postprocessing: " + postProcessEdge);
    }

    /**
     * The constructor.
     *
     * @param ed the parent editor of this mode. Each Mode instance belongs to
     * exactly one Editor instance.
     * @param edgeType the class of the edge
     * @param post if true, then the edge is postprocessed. See
     * postProcessEdge().
     * @param nodeCreator the SelectionButtons that knows how to create the node
     */
    public ModeCreateEdgeAndNode(Editor ed, Object edgeType, boolean post,
            SelectionButtons nodeCreator) {
        super(ed);
        setArg("edgeClass", edgeType);
        setArg("nodeCreator", nodeCreator);
        postProcessEdge = post;
        LOG.debug("postprocessing: " + postProcessEdge);
    }

    // //////////////////////////////////////////////////////////////
    // accessors
    /**
     * @param fignode the source fignode
     * @param port the port
     * @param x the x to start from
     * @param y the y to start from
     * @param reverse true if the direction is reversed, i.e. from destination
     * to source
     */
    public void setup(FigNode fignode, Object port, int x, int y,
            boolean reverse) {
        start();
        sourceFigNode = fignode;
        startPortFig = fignode.getPortFig(port);
        startPort = port;
        _newItem = createNewItem(null, x, y);
        destToSource = reverse;
        setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
    }

    // //////////////////////////////////////////////////////////////
    // Mode API
    /**
     * @see org.tigris.gef.base.FigModifyingMode#instructions()
     */
    public String instructions() {
        return Localizer.localize("GefBase", "ModeCreateEdgeAndNodeInstructions");
    }

    // //////////////////////////////////////////////////////////////
    // ModeCreate API
    /**
     * Create the new item that will be drawn. In this case I would rather
     * create the FigEdge when I am done. Here I just create a rubberband
     * FigLine to show during dragging.
     *
     * @see org.tigris.gef.base.ModeCreate#createNewItem(
     * java.awt.event.MouseEvent, int, int)
     */
    public Fig createNewItem(MouseEvent me, int snapX, int snapY) {
        FigPoly p = new FigPoly(snapX, snapY);
        p.setLineColor(Globals.getPrefs().getRubberbandColor());
        p.setFillColor(null);
        p.addPoint(snapX, snapY); // add the first point twice
        // _npoints = 2;
        return p;
    }

    /**
     * @see org.tigris.gef.base.Mode#done()
     */
    public void done() {
        super.done();
        if (_newItem != null) {
            editor.damaged(_newItem);
        }
        _newItem = null; // use this as the fig for the new FigEdge
        sourceFigNode = null;
        startPort = null;
        startPortFig = null;
    }

    // //////////////////////////////////////////////////////////////
    // mouse event handlers
    /**
     * On mousePressed determine what port the user is dragging from. The
     * mousePressed event is sent via ModeSelect.
     *
     * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
     */
    public void mousePressed(MouseEvent me) {
    }

    /**
     * On mouseReleased, find the destination port, ask the GraphModel to
     * connect the two ports. If that connection is allowed, then construct a
     * new FigEdge and add it to the Layer and send it to the back.
     *
     * @see
     * java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
     */
    public void mouseReleased(MouseEvent me) {
        if (me.isConsumed()) {
            return;
        }
        if (sourceFigNode == null) {
            done();
            return;
        }
        boolean nodeWasCreated = false;
        int x = me.getX(), y = me.getY();
        Editor ce = Globals.curEditor();
        Fig f = ce.hit(x, y);
        if (f == null) {
            f = ce.hit(x - 16, y - 16, 32, 32);
        }
        GraphModel gm = ce.getGraphModel();
        if (!(gm instanceof MutableGraphModel)) {
            f = null;
        }
        MutableGraphModel mgm = (MutableGraphModel) gm;
        // TODO: potential class cast exception
        if (f == null) {
            LOG.debug("make new node");
            Object newNode = null;
            SelectionButtons sb = (SelectionButtons) getArg("nodeCreator");
            if (sb != null) {
                newNode = sb.getNewNode(0);
            } else {
                Class nodeClass = (Class) getArg("nodeClass");
                try {
                    newNode = nodeClass.newInstance();
                } catch (java.lang.IllegalAccessException ignore) {
                    LOG.error(ignore);
                    return;
                } catch (java.lang.InstantiationException ignore) {
                    LOG.error(ignore);
                    return;
                }
            }
            if (newNode instanceof GraphNodeHooks) {
                ((GraphNodeHooks) newNode).initialize(_args);
            }
            if (mgm.canAddNode(newNode)) {
                GraphNodeRenderer renderer = editor.getGraphNodeRenderer();
                Layer lay = editor.getLayerManager().getActiveLayer();
                fn = renderer.getFigNodeFor(gm, lay, newNode, null);
                editor.add(fn);
                mgm.addNode(newNode);
                Fig encloser = null;
                Rectangle bbox = fn.getBounds();
                List<Fig> otherFigs = lay.getContents();
                Iterator<Fig> others = otherFigs.iterator();
                while (others.hasNext()) {
                    Fig otherFig = others.next();
                    if (!(otherFig instanceof FigNode)) {
                        continue;
                    }
                    if (otherFig.equals(fn)) {
                        continue;
                    }
                    Rectangle trap = otherFig.getTrapRect();
                    if (trap != null
                            && (trap.contains(bbox.x, bbox.y) && trap.contains(
                                    bbox.x + bbox.width, bbox.y + bbox.height))) {
                        encloser = otherFig;
                    }
                }
                fn.setEnclosingFig(encloser);
                if (newNode instanceof GraphNodeHooks) {
                    ((GraphNodeHooks) newNode).postPlacement(editor);
                }
                editor.getSelectionManager().select(fn);
                nodeWasCreated = true;
                f = fn;
                f.setLocation(x - f.getWidth() / 2, y - f.getHeight() / 2);
            }
        }
        if (f instanceof FigNode) {
            FigNode destFigNode = (FigNode) f;
            // If its a FigNode, then check within the
            // FigNode to see if a port exists
            Object foundPort = destFigNode.deepHitPort(x, y);
            if (foundPort == null) {
                Iterator it = destFigNode.getPortFigs().iterator();
                if (it.hasNext()) {
                    foundPort = ((Fig) it.next()).getOwner();
                }
            }
            FigPoly p = (FigPoly) _newItem;
            editor.damaged(p);
            p.setComplete(true);
            if (foundPort != null && foundPort != startPort) {
                Fig destPortFig = destFigNode.getPortFig(foundPort);
                Object edgeType = getArg("edgeClass");
                if (destToSource) {
                    Object temp = startPort;
                    startPort = foundPort;
                    foundPort = temp;
                    FigNode tempFN = destFigNode;
                    destFigNode = sourceFigNode;
                    sourceFigNode = tempFN;
                    Fig tempFigPort = startPortFig;
                    startPortFig = destPortFig;
                    destPortFig = tempFigPort;
                }
                if (edgeType != null) {
                    newEdge = mgm.connect(startPort, foundPort,
                            (Class) edgeType);
                } else {
                    newEdge = mgm.connect(startPort, foundPort);
                }
                // Calling connect() will add the edge to the GraphModel and
                // any LayerPersectives on that GraphModel will get a
                // edgeAdded event and will add an appropriate FigEdge
                // (determined by the GraphEdgeRenderer).
                if (newEdge != null) {
                    if (postProcessEdge) {
                        LOG.debug("postprocess edge.");
                        postProcessEdge();
                    }
                    ce.damaged(_newItem);
                    sourceFigNode.damage();
                    destFigNode.damage();

                    LayerManager lm = ce.getLayerManager();
                    fe = (FigEdge) lm.getActiveLayer().presentationFor(newEdge);
                    fe.setFig(_newItem);
                    fe.setSourcePortFig(startPortFig);
                    fe.setSourceFigNode(sourceFigNode);
                    fe.setDestPortFig(destPortFig);
                    fe.setDestFigNode(destFigNode);
                    fe.setSourcePortFig(startPortFig);
                    fe.setSourceFigNode(sourceFigNode);
                    fe.setDestPortFig(destPortFig);
                    fe.setDestFigNode(destFigNode);
                    if (fe != null && !nodeWasCreated) {
                        ce.getSelectionManager().select(fe);
                    }
                    done();
                    _newItem = null;
                    if (fe instanceof MouseListener) {
                        ((MouseListener) fe).mouseReleased(me);
                    }
                    // set the new edge in place
                    if (sourceFigNode != null) {
                        sourceFigNode.updateEdges();
                    }
                    if (destFigNode != null) {
                        destFigNode.updateEdges();
                    }
                    return;
                }
                LOG.warn("connection return null");
            } else {
                LOG.warn("in dest node but no port");
            }
        }
        sourceFigNode.damage();
        ce.damaged(_newItem);
        _newItem = null;
        done();
    }

    /**
     * @see
     * java.awt.event.MouseMotionListener#mouseMoved(java.awt.event.MouseEvent)
     */
    public void mouseMoved(MouseEvent me) {
        mouseDragged(me);
    }

    /**
     * @see
     * java.awt.event.MouseMotionListener#mouseDragged(java.awt.event.MouseEvent)
     */
    public void mouseDragged(MouseEvent me) {
        if (me.isConsumed()) {
            return;
        }
        int x = me.getX(), y = me.getY();
        // if (_npoints == 0) { me.consume(); return; }
        if (_newItem == null) {
            me.consume();
            return;
        }
        FigPoly p = (FigPoly) _newItem;
        editor.damaged(_newItem); // startTrans?
        Point snapPt = new Point(x, y);
        editor.snap(snapPt);
        handle.index = p.getNumPoints() - 1;
        p.moveVertex(handle, snapPt.x, snapPt.y, true);
        editor.damaged(_newItem); // endTrans?
        me.consume();
    }

    // //////////////////////////////////////////////////////////////
    // key events
    /**
     * @see java.awt.event.KeyListener#keyTyped(java.awt.event.KeyEvent)
     */
    public void keyTyped(KeyEvent ke) {
        if (ke.getKeyChar() == KeyEvent.VK_ESCAPE) {
            done();
            ke.consume();
        }
    }

    // //////////////////////////////////////////////////////////////
    // internal methods
    protected void postProcessEdge() {
        postProcessEdge(newEdge);
    }

    /**
     * @param edge the newly created edge
     */
    protected void postProcessEdge(Object edge) {
    }

    static final long serialVersionUID = -427957543380196265L;
} /* end class ModeCreateEdgeAndNode */
