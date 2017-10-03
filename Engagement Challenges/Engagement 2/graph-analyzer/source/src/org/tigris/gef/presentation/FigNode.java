// Copyright (c) 1996-99 The Regents of the University of California. All
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
// File: FigNode.java
// Classes: FigNode
// Original Author: ics125 spring 1996
// $Id: FigNode.java 1334 2011-05-23 10:30:22Z bobtarling $
package org.tigris.gef.presentation;

import org.graph.commons.logging.Log;
import org.graph.commons.logging.LogFactory;

import org.tigris.gef.base.Globals;
import org.tigris.gef.di.GraphEdge;
import org.tigris.gef.di.GraphNode;
import org.tigris.gef.graph.GraphNodeHooks;
import org.tigris.gef.graph.GraphPortHooks;
import org.tigris.gef.graph.MutableGraphSupport;
import org.tigris.gef.ui.Highlightable;
import org.tigris.gef.undo.UndoManager;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.awt.image.ByteLookupTable;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.awt.image.LookupOp;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Class to present a node (such as a NetNode) in a diagram.
 */
public class FigNode extends FigGroup implements Highlightable, GraphNode, MouseListener {

    private static final long serialVersionUID = 5312194520189613781L;

    private static final Log LOG = LogFactory.getLog(FigNode.class);

    private static final LookupOp SHADOW_LOOKUP_OP;
    private static final ConvolveOp SHADOW_CONVOLVE_OP;

    // Fields used in paint() for painting shadows
    private BufferedImage shadowImage;
    private int cachedWidth = -1;
    private int cachedHeight = -1;

    private List<Connector> connectors = new ArrayList<Connector>();

    /**
     * Set this to force a repaint of the shadow. Normally repainting only
     * happens when the outside boundaries change (for performance reasons (?)).
     * In some cases this does not suffice, and you can set this attribute to
     * force the update.
     */
    private boolean forceRepaint;

    /**
     * The intensity value of the shadow color (0-255).
     */
    protected static final int SHADOW_COLOR_VALUE = 32;

    /**
     * The transparency value of the shadow color (0-255).
     */
    protected static final int SHADOW_COLOR_ALPHA = 128;

    /**
     * Constants useful for determining what side (north, south, east, or west)
     * a port is located on.
     */
    public static final double ang45 = Math.PI / 4;
    public static final double ang135 = 3 * Math.PI / 4;
    public static final double ang225 = 5 * Math.PI / 4;
    public static final double ang315 = 7 * Math.PI / 4;

    // //////////////////////////////////////////////////////////////
    // instance variables
    /**
     * True if you want ports to show when the mouse moves in and be invisible
     * otherwise.
     */
    protected boolean _blinkPorts = false;

    /**
     * True when we want to draw the user's attention to this FigNode.
     */
    protected boolean _highlight = false;

    /**
     * A list of FigEdges that need to be rerouted when this FigNode moves.
     */
    private ArrayList<FigEdge> figEdges = new ArrayList<FigEdge>();

    private int shadowSize = 0;

    static {

        // Setup image ops used in rendering shadows
        byte[][] data = new byte[4][256];
        for (int i = 1; i < 256; ++i) {
            data[0][i] = (byte) SHADOW_COLOR_VALUE;
            data[1][i] = (byte) SHADOW_COLOR_VALUE;
            data[2][i] = (byte) SHADOW_COLOR_VALUE;
            data[3][i] = (byte) SHADOW_COLOR_ALPHA;
        }
        float[] blur = new float[9];
        for (int i = 0; i < blur.length; ++i) {
            blur[i] = 1 / 12f;
        }
        SHADOW_LOOKUP_OP = new LookupOp(new ByteLookupTable(0, data), null);
        SHADOW_CONVOLVE_OP = new ConvolveOp(new Kernel(3, 3, blur));
    }

    // //////////////////////////////////////////////////////////////
    // constructors
    public FigNode() {
    }

    /**
     * Constructs a new FigNode on the given node with the given owner.
     *
     * @param node The model item that this node represents
     */
    public FigNode(Object node) {
        setOwner(node);
        // if (node instanceof GraphNodeHooks)
        // ((GraphNodeHooks)node).addPropertyChangeListener(this);
    }

    /**
     * Constructs a new FigNode on the given node with the given owner and Figs.
     *
     * @param node the model item that this node represents
     * @param figs the figs to be contained as a group by this FigNode
     */
    public FigNode(Object node, Collection figs) {
        this(node);
        setFigs(figs);
    }

    /**
     * Returns true if dragging from a port on this fig should automatically go
     * to mode ModeCreateEdge
     *
     * @return the drag connectable property
     */
    public boolean isDragConnectable() {
        return true;
    }

    public Object clone() {
        FigNode figClone = (FigNode) super.clone();
        figClone.figEdges = (ArrayList) figEdges.clone();
        return figClone;
    }

    // //////////////////////////////////////////////////////////////
    // accessors
    /**
     * Set the property of highlighting ports when the user moves the mouse over
     * this FigNode.
     */
    public void setBlinkPorts(boolean b) {
        _blinkPorts = b;
        hidePorts();
    }

    /**
     * Determine if ports are set to appear only on mouseover.
     *
     * @return true if ports are set to appear only on mouseover.
     */
    public boolean isBlinkPorts() {
        return _blinkPorts;
    }

    /**
     * Adds a FigEdge to the list of them that need to be rerouted when this
     * FigNode moves.
     */
    public void addFigEdge(FigEdge fe) {
        figEdges.add(fe);
    }

    /**
     * Add a new connector to this node
     *
     * @param connector the connector to add.
     */
    public void addConnector(Connector connector) {
        connectors.add(connector);
        Fig connectorFig = (Fig) connector;
        Point centre = new Point(
                connectorFig.getX() + connectorFig.getHalfWidth(),
                connectorFig.getY() + connectorFig.getHalfHeight());
        Point attachPoint = getClosestPoint(centre);
        connectorFig.setLocation(
                attachPoint.x - connectorFig.getHalfWidth(),
                attachPoint.y - connectorFig.getHalfHeight());
        connector.setGraphNode(this);
    }

    /**
     * Remove a connector from this node
     *
     * @param connector the connector to remove.
     */
    public void removeConnector(Connector connector) {
        connectors.remove(connector);
    }

    /**
     * removes a FigEdge from the list of them that need to be rerouted when
     * this FigNode moves.
     */
    public void removeFigEdge(FigEdge fe) {
        figEdges.remove(fe);
    }

    public Collection<FigEdge> getFigEdges(Collection<FigEdge> c) {
        if (c == null) {
            return figEdges;
        }
        c.addAll(figEdges);
        return c;
    }

    public List<FigEdge> getFigEdges() {
        return (List<FigEdge>) (figEdges.clone());
    }

    public List<GraphEdge> getGraphEdges() {
        return (List<GraphEdge>) (figEdges.clone());
    }

    /**
     * Sets the owner (a node in some underlying model). If the given node
     * implements GraphNodeHooks, then the FigNode will register itself as a
     * listener on the node.
     */
    public void setOwner(Object node) {
        Object oldOwner = getOwner();
        if (oldOwner instanceof GraphNodeHooks) {
            ((GraphNodeHooks) oldOwner).removePropertyChangeListener(this);
        } else if (oldOwner instanceof Highlightable) {
            ((Highlightable) oldOwner).removePropertyChangeListener(this);
        }
        if (node instanceof GraphNodeHooks) {
            ((GraphNodeHooks) node).addPropertyChangeListener(this);
        } else if (node instanceof Highlightable) {
            ((Highlightable) node).addPropertyChangeListener(this);
        }
        super.setOwner(node);
    }

    /**
     * Returns true if any Fig in the group hits the given rect.
     */
    public boolean hit(Rectangle r) {
        int cornersHit = countCornersContained(r.x, r.y, r.width, r.height);
        if (_filled) {
            return cornersHit > 0;
        } else {
            return cornersHit > 0 && cornersHit < 4;
        }
    }

    public boolean contains(int x, int y) {
        return (_x <= x) && (x <= _x + _w) && (_y <= y) && (y <= _y + _h);
    }

    public void setEnclosingFig(Fig f) {
        if (f != null && f != getEnclosingFig() && getLayer() != null) {
            int edgeCount = figEdges.size();
            for (int i = 0; i < edgeCount; ++i) {
                FigEdge fe = (FigEdge) figEdges.get(i);
                getLayer().bringInFrontOf(fe, f);
            }
        }
        super.setEnclosingFig(f);
        // org.graph.commons.logging.LogFactory.getLog(null).info("enclosing fig has been set");
    }

    // //////////////////////////////////////////////////////////////
    // Editor API
    /**
     * When a FigNode is damaged, all of its edges may need repainting.
     */
    public void endTrans() {
        int edgeCount = figEdges.size();
        for (int i = 0; i < edgeCount; ++i) {
            FigEdge f = (FigEdge) figEdges.get(i);
            f.endTrans();
        }
        super.endTrans();
    }

    /**
     * When a FigNode is removed, all of its edges are removed first.
     */
    public void removeFromDiagram() {
        // remove the edges in reverse order because to make sure
        // that other edges in figEdge don't have their position
        // altered as a side effect.
        while (figEdges.size() > 0) {
            FigEdge f = (FigEdge) figEdges.get(figEdges.size() - 1);
            f.removeFromDiagram();
        }
        super.removeFromDiagram();
    }

    /**
     * When a FigNode is disposed, all of its edges are disposed.
     */
    public void deleteFromModel() {
        LOG.debug("Deleting FigNode from model");
        // delete the edges in reverse order because to make sure
        // that other edges in figEdge don't have their position
        // altered as a side effect.
        for (int i = figEdges.size() - 1; i >= 0; --i) {
            FigEdge f = (FigEdge) figEdges.get(i);
            f.deleteFromModel();
        }
        super.deleteFromModel();
    }

    /**
     * When a FigNode is disposed, all of its edges are disposed.
     *
     * 0.11 use deleteFromModel()
     */
    public void dispose() {
        deleteFromModel();
    }

    // //////////////////////////////////////////////////////////////
    // ports
    /**
     * Sets the port (some object in an underlying model) for Fig f. f must
     * already be contained in the FigNode. f will now represent the given port.
     */
    public void bindPort(Object port, Fig f) {
        Fig oldPortFig = getPortFig(port);
        if (oldPortFig != null) {
            oldPortFig.setOwner(null); // ?
        }
        f.setOwner(port);
    }

    /**
     * Removes a port from the current FigNode.
     */
    public void removePort(Fig rep) {
        if (rep.getOwner() != null) {
            rep.setOwner(null);
        }
    }

    /**
     * Reply the NetPort associated with the topmost Fig under the mouse, or
     * null if there is none.
     */
    public final Object hitPort(Point p) {
        return hitPort(p.x, p.y);
    }

    /**
     * Reply the port that "owns" the topmost Fig under the given point, or null
     * if none.
     */
    public Object hitPort(int x, int y) {
        Fig f = hitFig(new Rectangle(x, y, 1, 1));
        if (f != null) {
            Object owner = f.getOwner();
            return owner;
        } else {
            return null;
        }
    }

    /**
     * Reply a port for the topmost Fig that actually has a port. This allows
     * users to drag edges to or from ports that are hidden by other Figs.
     */
    public Object deepHitPort(int x, int y) {
        int figCount = getFigCount();
        for (int figIndex = 0; figIndex < figCount; ++figIndex) {
            Fig f = getFigAt(figIndex);
            Object own = f.getOwner();
            // assumes ports are always filled
            if (f.contains(x, y) && own != null) {
                return own;
            }
        }

        Rectangle r = new Rectangle(x - 16, y - 16, 32, 32);
        for (int figIndex = 0; figIndex < figCount; ++figIndex) {
            Fig f = getFigAt(figIndex);
            Object own = f.getOwner();
            // assumes ports are always filled
            if (f.hit(r) && own != null) {
                return own;
            }
        }

        return null;
    }

    /**
     * Reply the Fig that displays the given NetPort.
     */
    public Fig getPortFig(Object np) {
        int figCount = getFigCount();
        for (int figIndex = 0; figIndex < figCount; ++figIndex) {
            Fig f = getFigAt(figIndex);
            if (f.getOwner() == np) {
                return f;
            }
        }
        return null;
    }

    /**
     * Get all the figs that have some port as their owner
     *
     * @return the List of figs
     */
    public List getPortFigs() {
        ArrayList portFigs = new ArrayList();
        int figCount = getFigCount();
        for (int figIndex = 0; figIndex < figCount; ++figIndex) {
            Fig f = getFigAt(figIndex);
            if (isPortFig(f)) {
                portFigs.add(f);
            }
        }
        return portFigs;
    }

    private boolean isPortFig(Fig f) {
        boolean retVal = (f.getOwner() != null);
        if (retVal && getOwner() instanceof GraphNodeHooks) {
            retVal = f.getOwner() instanceof GraphPortHooks;
        }
        return retVal;
    }

    // //////////////////////////////////////////////////////////////
    // diagram-level operations
    /**
     * Reply the port's sector within the current view. This version works
     * precisely with square FigNodes the angxx constants should be removed and
     * calculated by the port if non-square FigNodes will be used.
     *
     * <pre>
     * Sectors
     * 	      \  1   /
     * 	       \    /
     * 	        \  /
     * 	     2   \/   -2
     * 		 /\
     * 	        /  \
     * 	       /    \
     * 	      /  -1  \
     * </pre>
     */
    public int getPortSector(Fig portFig) {
        Rectangle nodeBBox = getBounds();
        Rectangle portBBox = portFig.getBounds();
        int nbbCenterX = nodeBBox.x + nodeBBox.width / 2;
        int nbbCenterY = nodeBBox.y + nodeBBox.height / 2;
        int pbbCenterX = portBBox.x + portBBox.width / 2;
        int pbbCenterY = portBBox.y + portBBox.height / 2;
        int dX = pbbCenterX - nbbCenterX;
        int dY = pbbCenterY - nbbCenterY;

        //
        // the key is the tangent of this rectangle
        //
        // if you didn't care about divisions by zero,
        // you could do
        //
        // tangentBox = nodeBBox.height/nodeBBox.width;
        // tangentCenters = dY/dX;
        // if(Math.abs(tangentCenters) > tangentBox) sector 1 or -1
        //
        int sector = -1;
        if (Math.abs(dY * nodeBBox.width) > Math.abs(nodeBBox.height * (dX))) {
            if (dY > 0) {
                sector = 1;
            }
        } else {
            sector = 2;
            if (dX > 0) {
                sector = -2;
            }
        }
        return sector;
    }

    // //////////////////////////////////////////////////////////////
    // painting methods

    /*
     * Overridden to paint shadows. This method supports painting shadows for
     * any FigNodeModelElement. Any Figs that are nested within the
     * FigNodeModelElement will be shadowed.<p>
     * 
     * TODO: If g is not a Graphics2D shadows cannot be painted. This is a
     * problem when saving the diagram as SVG.
     * 
     * @see org.tigris.gef.presentation.Fig#paint(java.awt.Graphics)
     */
    public void paint(Graphics g) {
        if (shadowSize > 0 && g instanceof Graphics2D) {
            int width = getWidth();
            int height = getHeight();
            int x = getX();
            int y = getY();

            /*
             * Only create a new shadow image if figure size has changed. Which
             * does not catch all cases: consider show/hide toggle of a
             * stereotype on a package: in this case the total size remains, but
             * the notch at the corner increases/decreases. Hence also check the
             * "forceRepaint" attribute.
             */
            if (width != cachedWidth || height != cachedHeight || forceRepaint) {
                forceRepaint = false;

                cachedWidth = width;
                cachedHeight = height;

                BufferedImage img = new BufferedImage(width + 100,
                        height + 100, BufferedImage.TYPE_INT_ARGB);

                // Paint figure onto offscreen image
                Graphics ig = img.getGraphics();
                ig.translate(50 - x, 50 - y);
                paintOnce(ig);

                // Apply two filters to the image:
                // 1. Apply LookupOp which converts all pixel data in the
                // figure to the same shadow color.
                // 2. Apply ConvolveOp which creates blurred effect around
                // the edges of the shadow.
                shadowImage = SHADOW_CONVOLVE_OP.filter(SHADOW_LOOKUP_OP
                        .filter(img, null), null);
            }

            // Paint shadow image onto canvas
            Graphics2D g2d = (Graphics2D) g;
            g2d.drawImage(shadowImage, null, x + shadowSize - 50, y
                    + shadowSize - 50);
        }

        // Paint figure on top of shadow
        paintOnce(g);
    }

    /**
     * Paints the FigNode to the given Graphics. Calls super.paint to paint all
     * the Figs contained in the FigNode. Also can draw a highlighting rectangle
     * around the FigNode. Needs-more-work: maybe I should implement
     * LayerHighlight instead.
     */
    private void paintOnce(Graphics g) {
        super.paint(g);
        // org.graph.commons.logging.LogFactory.getLog(null).info("[FigNode] paint: owner = " + getOwner());
        if (_highlight) {
            Color lineColor = Globals.getPrefs().getHighlightColor();
            drawRect(g, false, null, 3, lineColor, _x - 5, _y - 5, _w + 9,
                    _h + 8, false, _dashes, _dashPeriod);
        }
    }

    // //////////////////////////////////////////////////////////////
    // Highlightable implementation
    public void setHighlight(boolean b) {
        _highlight = b;
        damage();
    }

    public boolean getHighlight() {
        return _highlight;
    }

    // //////////////////////////////////////////////////////////////
    // notifications and updates
    /**
     * The node object that this FigNode is presenting has changed state, or
     * been disposed or highlighted.
     */
    public void propertyChange(PropertyChangeEvent pce) {
        // org.graph.commons.logging.LogFactory.getLog(null).info("FigNode got a PropertyChangeEvent");
        String pName = pce.getPropertyName();
        Object src = pce.getSource();
        if (pName.equals("disposed") && src == getOwner()) {
            removeFromDiagram();
        }
        if (pName.equals("highlight") && src == getOwner()) {
            setHighlight(((Boolean) pce.getNewValue()).booleanValue());
        }
    }

    /**
     * Make the port Figs visible. Used when blinkingPorts is true.
     */
    public void showPorts() {
        int figCount = getFigCount();
        for (int figIndex = 0; figIndex < figCount; ++figIndex) {
            Fig f = getFigAt(figIndex);
            if (f.getOwner() != null) {
                f.setLineWidth(1);
                f.setFilled(true);
            }
        }
        endTrans();
    }

    /**
     * Make the port Figs invisible. Used when blinkingPorts is true.
     */
    public void hidePorts() {
        int figCount = getFigCount();
        for (int figIndex = 0; figIndex < figCount; ++figIndex) {
            Fig f = getFigAt(figIndex);
            if (f.getOwner() != null) {
                f.setLineWidth(0);
                f.setFilled(false);
            }
        }
        endTrans();
    }

    // //////////////////////////////////////////////////////////////
    // event handlers
    /**
     * If the mouse enters this FigNode's bbox and the _blinkPorts flag is set,
     * then show ports.
     */
    public void mouseEntered(MouseEvent me) {
        if (_blinkPorts) {
            showPorts();
        }
    }

    /**
     * If the mouse exits this FigNode's bbox and the _blinkPorts flag is set,
     * then hide ports.
     */
    public void mouseExited(MouseEvent me) {
        if (_blinkPorts) {
            hidePorts();
        }
    }

    /**
     * Do nothing when mouse is pressed in FigNode.
     */
    public void mousePressed(MouseEvent me) {
    }

    /**
     * Do nothing when mouse is released in FigNode.
     */
    public void mouseReleased(MouseEvent me) {
    }

    /**
     * Do nothing when mouse is clicked in FigNode.
     */
    public void mouseClicked(MouseEvent me) {
    }

    protected void translateImpl(int dx, int dy) {
        super.translateImpl(dx, dy);
        updateEdges();
    }

    public void superTranslate(int dx, int dy) {
        super.translate(dx, dy);
    }

    protected void setBoundsImpl(int x, int y, int w, int h) {
        super.setBoundsImpl(x, y, w, h);
        updateEdges();
    }

    /**
     * Update the position of edges according to the position of the node. Does
     * nothing if undo in progress.
     */
    public void updateEdges() {
        if (!UndoManager.getInstance().isUndoInProgress()) {
            int edgeCount = figEdges.size();
            for (int edgeIndex = 0; edgeIndex < edgeCount; ++edgeIndex) {
                FigEdge fe = (FigEdge) figEdges.get(edgeIndex);
                fe.computeRoute();
            }
        }
    }

    public void cleanUp() {
        int edgeCount = figEdges.size();
        for (int i = 0; i < edgeCount; ++i) {
            FigEdge fe = (FigEdge) figEdges.get(i);
            fe.cleanUp();
        }
    }

    /**
     * Return a list of other Figs that must be forced to be dragged at the same
     * time as this Fig or null if no dependent. By default this returns null,
     * override in concrete class as required.
     *
     * @return List of figs
     */
    public List<? extends Fig> getDragDependencies() {
        return null;
    }

    /**
     * Return a list of other Figs that must be forced to be dragged at the same
     * time as this Fig or null if no dependent. By default this returns null,
     * override in concrete class as required.
     *
     * @return List of figs
     */
    public List<Connector> getConnectors() {
        return connectors;
    }

    /**
     * @param size the new shadow size TODO: Move the shadow stuff into GEF
     */
    public void setShadowSize(int size) {
        if (size == shadowSize) {
            return;
        }
        MutableGraphSupport.enableSaveAction();
        shadowSize = size;
    }

    /**
     * do not use. This was deprecated by bobtarling at the same time that it
     * was introduced (Apr 2007), so may be deleted without warning as soon as
     * its single reference is gone.
     *
     * @param size
     */
    protected void setShadowSizeFriend(int size) {
        if (size == shadowSize) {
            return;
        }
        shadowSize = size;
    }

    /**
     * @return the current shadow size
     */
    public int getShadowSize() {
        return shadowSize;
    }

    /**
     * Force painting the shadow.
     */
    public void forceRepaintShadow() {
        forceRepaint = true;
    }

    public Rectangle getNodeBounds() {
        return getBounds();
    }

    public void delete() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
