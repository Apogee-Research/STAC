// %1035290364839:org.tigris.gef.presentation%
package org.tigris.gef.presentation;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

import org.tigris.gef.base.Globals;
import org.tigris.gef.base.PathConv;
import org.tigris.gef.base.PathItemPlacementStrategy;
import org.tigris.gef.di.GraphEdge;
import org.tigris.gef.di.GraphNode;
import org.tigris.gef.graph.GraphEdgeHooks;
import org.tigris.gef.ui.Highlightable;
import org.tigris.gef.undo.Memento;
import org.tigris.gef.undo.UndoManager;

/**
 * Abstract Fig class for representing edges between ports.
 */
public abstract class FigEdge extends Fig implements Highlightable, GraphEdge {

    /**
     * Fig presenting the edge's from-port .
     */
    private Fig sourcePortFig;

    /**
     * Fig presenting the edge's to-port.
     */
    private Fig destPortFig;

    /**
     * FigNode presenting the edge's from-port's parent node.
     */
    private FigNode sourceFigNode;

    /**
     * FigNode presenting the edge's to-port's parent node.
     */
    private FigNode destFigNode;

    /**
     * Fig that presents the route of the edge.
     */
    private Fig routeFig;

    /**
     * True if the FigEdge should be drawn from the nearest point of each port
     * Fig. in 0.13 will be made private, use getter/setter
     */
    protected boolean _useNearest = false;

    /**
     * True when the FigEdgde should be drawn highlighted. in 0.13 will be made
     * private, use getter/setter
     */
    protected boolean _highlight = false;

    /**
     * The ArrowHead at the start of the line in 0.13 will be made private, use
     * getter/setter
     */
    protected ArrowHead _arrowHeadStart = ArrowHeadNone.TheInstance;

    /**
     * The ArrowHead at the end of the line in 0.13 will be made private, use
     * getter/setter
     */
    protected ArrowHead _arrowHeadEnd = ArrowHeadNone.TheInstance;

    /**
     * The items that are accumulated along the path, a vector.
     *
     * in 0.13 will become private use getPathItems()
     */
    protected Vector<PathItem> _pathItems = new Vector<PathItem>();

    public void delete() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private class PathItem implements java.io.Serializable {

        private static final long serialVersionUID = -5298572087861993804L;
        final Fig _fig;
        final private PathItemPlacementStrategy pathItemPlacementStrategy;

        PathItem(final Fig f, final PathConv pc) {
            _fig = f;
            pathItemPlacementStrategy = pc;
        }

        /**
         * in 0.13 use getPathItemPlacementStrategy
         *
         * @return
         */
        final public PathConv getPath() {
            return (PathConv) pathItemPlacementStrategy;
        }

        final public PathItemPlacementStrategy getPathItemPlacementStrategy() {
            return pathItemPlacementStrategy;
        }

        final public Fig getFig() {
            return _fig;
        }

        final public void paint() {

        }
    }

    /**
     * Construct a new FigEdge without any underlying edge.
     */
    public FigEdge() {
        setFig(makeEdgeFig());
    }

    // //////////////////////////////////////////////////////////////
    // constructors
    /**
     * Construct a new FigEdge with the given source and destination port figs
     * and FigNodes. The new FigEdge will represent the given edge (an object
     * from some underlying model).
     */
    public FigEdge(Fig s, Fig d, FigNode sfn, FigNode dfn, Object edge) {
        setSourcePortFig(s);
        setDestPortFig(d);
        setSourceFigNode(sfn);
        setDestFigNode(dfn);
        setOwner(edge);
        setFig(makeEdgeFig());
        routeFig.setGroup(this);
        routeFig.setLayer(getLayer());
    }

    /**
     * Add a new path item to this FigEdge. newPath indicates both the location
     * and the Fig (usually FigText) that should be drawn.
     */
    public void addPathItem(Fig newFig, PathConv newPath) {
        _pathItems.addElement(new PathItem(newFig, newPath));
        newFig.setGroup(this);
    }

    /**
     * Update my bounding box
     */
    public void calcBounds() {
        routeFig.calcBounds();
        Rectangle res = routeFig.getBounds();
        Point loc = new Point();
        int size = _pathItems.size();
        for (int i = 0; i < size; i++) {
            PathItem element = (PathItem) _pathItems.elementAt(i);
            PathConv pc = element.getPath();
            Fig f = element.getFig();
            int oldX = f.getX();
            int oldY = f.getY();
            int halfWidth = f.getWidth() / 2;
            int halfHeight = f.getHeight() / 2;
            pc.stuffPoint(loc);
            if (oldX != loc.x || oldY != loc.y) {
                f.damage();
                f.setLocation(loc.x - halfWidth, loc.y - halfHeight);
            }

            res.add(f.getBounds());
        }

        _x = res.x;
        _y = res.y;
        _w = res.width;
        _h = res.height;
    }

    final public void cleanUp() {
        routeFig.cleanUp();
    }

    // //////////////////////////////////////////////////////////////
    // Routing related methods
    /**
     * Method to compute the route a FigEdge should follow. By defualt this does
     * nothing. Sublcasses, like FigEdgeRectiline override this method.
     */
    final public void computeRoute() {
        if (UndoManager.getInstance().isGenerateMementos()) {
            Memento memento = new Memento() {
                Point[] points = getPoints();

                public void undo() {
                    UndoManager.getInstance().addMementoLock(this);
                    Point[] newpoints = getPoints();
                    setPoints(points);
                    points = newpoints;
                    damage();
                    UndoManager.getInstance().removeMementoLock(this);
                }

                public void redo() {
                    undo();
                }

                public void dispose() {
                }

                public String toString() {
                    return (isStartChain() ? "*" : " ")
                            + "ComputeRouteMemento " + Arrays.toString(points);
                }
            };
            UndoManager.getInstance().addMemento(memento);
        }
        computeRouteImpl();
    }

    abstract public void computeRouteImpl();

    final public boolean contains(int x, int y) {
        if (routeFig.contains(x, y)) {
            return true;
        }

        int size = _pathItems.size();
        for (int i = 0; i < size; i++) {
            Fig f = ((PathItem) _pathItems.elementAt(i)).getFig();
            if (f.contains(x, y)) {
                return true;
            }
        }

        return false;
    }

    public void removeFromDiagram() {
        if (sourceFigNode != null) {
            sourceFigNode.removeFigEdge(this);
        }

        if (destFigNode != null) {
            destFigNode.removeFigEdge(this);
        }

        super.removeFromDiagram();
    }

    /**
     * Get and set the flag about using Fig connection points rather than
     * centers.
     */
    final public boolean getBetweenNearestPoints() {
        return _useNearest;
    }

    // //////////////////////////////////////////////////////////////
    // Fig API
    /**
     * Reply the bounding box for this FigEdge.
     */
    final public Rectangle getBounds(Rectangle r) {
        if (r == null) {
            r = new Rectangle();
        }

        routeFig.getBounds(r);
        int size = _pathItems.size();
        for (int pathItemIndex = 0; pathItemIndex < size; pathItemIndex++) {
            PathItem pathItem = (PathItem) _pathItems.get(pathItemIndex);
            Fig f = pathItem.getFig();
            r.add(f.getBounds());
        }

        return r;
    }

    public boolean getDashed() {
        return routeFig.getDashed();
    }

    /**
     * Get the ArrowHead at the end of this FigEdge.
     */
    final public ArrowHead getDestArrowHead() {
        return _arrowHeadEnd;
    }

    /**
     * USED BY PGML.tee
     */
    final public FigNode getDestFigNode() {
        return destFigNode;
    }

    /**
     * USED BY PGML.tee
     */
    public Fig getDestPortFig() {
        return destPortFig;
    }

    // //////////////////////////////////////////////////////////////
    // accessors
    /**
     * Return the Fig that will be drawn. USED BY PGML.tee
     */
    final public Fig getFig() {
        return routeFig;
    }

    /**
     * The first point ion an edge USED BY PGML.tee
     */
    final public Point getFirstPoint() {
        return routeFig.getFirstPoint();
    }

    final public boolean getHighlight() {
        return _highlight;
    }

    final public Point getLastPoint() {
        return routeFig.getLastPoint();
    }

    /**
     * USED BY PGML.tee
     */
    final public Color getLineColor() {
        return routeFig.getLineColor();
    }

    /**
     * An edge cannot be filled with color
     *
     * @return false
     */
    final public boolean hasFillColor() {
        return false;
    }

    /**
     * USED BY PGML.tee
     */
    final public int getLineWidth() {
        return routeFig.getLineWidth();
    }

    final public int getNumPoints() {
        return routeFig.getNumPoints();
    }

    /**
     * Return the path item on this FigEdge closest to the given location.
     * needs-more-work: not implemented yet.
     */
    final public Fig getPathItem(PathConv pointOnPath) {
        // needs-more-work: Find the closest Fig to this point
        return null;
    }

    /**
     * Return the fig of a given path item.
     */
    final public Fig getPathItemFig(PathItem pathItem) {
        Fig fig = pathItem.getFig();
        return fig;
    }

    /**
     * Return all figs of the path items
     */
    final public Vector getPathItemFigs() {
        Vector figs = new Vector();
        for (int i = 0; i < _pathItems.size(); i++) {
            figs.add(getPathItemFig((PathItem) _pathItems.elementAt(i)));
        }

        return figs;
    }

    /**
     * Gets the PathItemPlacementStrategy for the given fig. The given fig must
     * be one of the pathItem figs, otherwise null will be returned.
     *
     * @param fig The fig to look for.
     * @return The PathItemPlacementStrategy for fig.
     */
    public PathItemPlacementStrategy getPathItemPlacementStrategy(Fig fig) {
        for (PathItem pi : _pathItems) {
            Fig f = getPathItemFig(pi);
            if (fig.equals(f)) {
                return pi.getPathItemPlacementStrategy();
            }
        }
    // Something bad has happened if we get here, it means the fig that 
        // we were asked to look for was not on a PathItem.
        //org.graph.commons.logging.LogFactory.getLog(null).info("Could not find PathItemPlacementStrategy for fig '"
        //    + fig + "'.");
        return null;
    }

    /**
     * Return the vector of path items on this FigEdge.
     *
     * use getPathItems
     */
    final public Vector getPathItemsRaw() {
        return _pathItems;
    }

    /**
     * @return the path items on this edge.
     */
    private List<PathItem> getPathItems() {
        return new ArrayList<PathItem>(_pathItems);
    }

    /**
     * @return all the path item placement strategies used by this edge.
     */
    public List<PathItemPlacementStrategy> getPathItemStrategies() {
        List<PathItemPlacementStrategy> strategyList = new ArrayList<PathItemPlacementStrategy>();
        for (final PathItem item : getPathItems()) {
            strategyList.add(item.getPath());
        }
        return strategyList;
    }

    final public int getPerimeterLength() {
        return routeFig.getPerimeterLength();
    }

    final public Point[] getPoints() {
        return routeFig.getPoints();
    }

    final public Point getPoint(int i) {
        return routeFig.getPoint(i);
    }

    /**
     * TODO document Used in SVG.TEE
     */
    public String getPrivateData() {
        Fig spf = getSourcePortFig();
        Fig dpf = getDestPortFig();
        FigNode sfn = getSourceFigNode();
        FigNode dfn = getDestFigNode();
        String data = "";
        if (spf != null) {
            data += "sourcePortFig=\"" + spf.getId() + "\" ";
        }

        if (dpf != null) {
            data += "destPortFig=\"" + dpf.getId() + "\" ";
        }

        if (sfn != null) {
            data += "sourceFigNode=\"" + sfn.getId() + "\" ";
        }

        if (dfn != null) {
            data += "destFigNode=\"" + dfn.getId() + "\" ";
        }

        return data;
    }

    /**
     * Get the ArrowHead at the start of this FigEdge.
     */
    final public ArrowHead getSourceArrowHead() {
        return _arrowHeadStart;
    }

    /**
     * USED BY PGML.tee
     */
    final public FigNode getSourceFigNode() {
        return sourceFigNode;
    }

    final public GraphNode getSourceGraphNode() {
        return sourceFigNode;
    }

    final public GraphNode getDestGraphNode() {
        return destFigNode;
    }

    /**
     * USED BY PGML.tee
     */
    public Fig getSourcePortFig() {
        return sourcePortFig;
    }

    final public int[] getXs() {
        return routeFig.getXs();
    }

    final public int[] getYs() {
        return routeFig.getYs();
    }

    public boolean hit(Rectangle r) {
        if (routeFig.hit(r)) {
            return true;
        }

        int size = _pathItems.size();
        for (int i = 0; i < size; i++) {
            Fig f = ((PathItem) _pathItems.elementAt(i)).getFig();
            if (f.isAnnotation() && f.hit(r)) {
                return true;
            }
        }

        return false;
    }

    final public Fig hitFig(Rectangle r) {
        Enumeration iter = _pathItems.elements();
        Fig res = null;
        if (routeFig.hit(r)) {
            res = routeFig;
        }

        while (iter.hasMoreElements()) {
            PathItem pi = (PathItem) iter.nextElement();
            Fig f = pi.getFig();
            if (f.hit(r)) {
                res = f;
            }
        }

        return res;
    }

    final public boolean intersects(Rectangle r) {
        if (routeFig.intersectsPerimeter(r)) {
            // org.graph.commons.logging.LogFactory.getLog(null).info("Intersects perimeter");
            return true;
        }

        int size = _pathItems.size();
        for (int i = 0; i < size; i++) {
            Fig f = ((PathItem) _pathItems.elementAt(i)).getFig();
            if (f.intersects(r)) {
                // org.graph.commons.logging.LogFactory.getLog(null).info("Intersects");
                return true;
            }
        }

        // org.graph.commons.logging.LogFactory.getLog(null).info("Doesn't intersect");
        return false;
    }

    final public boolean isReshapable() {
        return routeFig.isReshapable();
    }

    final public boolean isResizable() {
        return routeFig.isResizable();
    }

    final public boolean isRotatable() {
        return routeFig.isRotatable();
    }

    /**
     * Abstract method to make the Fig that will be drawn for this FigEdge. In
     * FigEdgeLine this method constructs a FigLine. In FigEdgeRectiline, this
     * method constructs a FigPoly.
     */
    protected abstract Fig makeEdgeFig();

    /**
     * Paint this FigEdge. TODO: take Highlight into account
     */
    public void paint(Graphics graphicContext) {
        // computeRoute();
        Graphics g = (Graphics) graphicContext;
        routeFig.paint(g);
        paintArrowHeads(g);
        paintPathItems(g);
    }

    public void appendSvg(StringBuffer sb) {
        // computeRoute();
        routeFig.appendSvg(sb);
        // appendSvgArrowHeads(g);
        appendSvgPathItems(sb);
    }

    // //////////////////////////////////////////////////////////////
    // display methods
    /**
     * Paint ArrowHeads on this FigEdge. Called from paint().
     */
    final protected void paintArrowHeads(Object g) {
        _arrowHeadStart.paintAtHead(g, routeFig);
        _arrowHeadEnd.paintAtTail(g, routeFig);
    }

    final public void paintHighlightLine(Graphics g, int x1, int y1, int x2,
            int y2) {
        g.setColor(Globals.getPrefs().getHighlightColor()); /* needs-more-work */

        double dx = (x2 - x1);
        double dy = (y2 - y1);
        double denom = Math.sqrt(dx * dx + dy * dy);
        if (denom == 0) {
            return;
        }

        double orthoX = dy / denom;
        double orthoY = -dx / denom;
        // needs-more-work: should fill poly instead
        for (double i = 2.0; i < 5.0; i += 0.27) {
            int hx1 = (int) (x1 + i * orthoX);
            int hy1 = (int) (y1 + i * orthoY);
            int hx2 = (int) (x2 + i * orthoX);
            int hy2 = (int) (y2 + i * orthoY);
            g.drawLine(hx1, hy1, hx2, hy2);
        }
    }

    /**
     * Paint any labels that are located relative to this FigEdge.
     */
    final protected void paintPathItems(Graphics g) {
        Vector pathVec = getPathItemsRaw();
        for (int i = 0; i < pathVec.size(); i++) {
            PathItem element = (PathItem) pathVec.elementAt(i);
            // PathConv path = element.getPath();
            Fig f = element.getFig();
            // int halfWidth = f.getWidth() / 2;
            // int halfHeight = f.getHeight() / 2;
            // Point loc = path.getPoint();
            // f.setLocation(loc.x - halfWidth, loc.y - halfHeight);
            f.paint(g);
        }
    }

    /**
     * Paint any labels that are located relative to this FigEdge.
     */
    final protected void appendSvgPathItems(StringBuffer sb) {
        Vector pathVec = getPathItemsRaw();
        for (int i = 0; i < pathVec.size(); i++) {
            PathItem element = (PathItem) pathVec.elementAt(i);
            Fig f = element.getFig();
            f.appendSvg(sb);
        }
    }

    // //////////////////////////////////////////////////////////////
    // notifications and updates
    public void propertyChange(PropertyChangeEvent pce) {
        // org.graph.commons.logging.LogFactory.getLog(null).info("FigEdge got a PropertyChangeEvent");
        String pName = pce.getPropertyName();
        Object src = pce.getSource();
        if (pName.equals("disposed") && src == getOwner()) {
            removeFromDiagram();
        }

        if (pName.equals("highlight") && src == getOwner()) {
            _highlight = ((Boolean) pce.getNewValue()).booleanValue();
            damage();
        }
    }

    /**
     * Removes the given path item.
     */
    final public void removePathItem(PathItem goneItem) {
        _pathItems.removeElement(goneItem);
        goneItem.getFig().setGroup(null);
    }

    final public void removePathItem(Fig goneFig) {
        for (int i = 0; i < _pathItems.size(); i++) {
            PathItem curItem = (PathItem) _pathItems.elementAt(i);
            if (curItem.getFig() == goneFig) {
                removePathItem(curItem);
                return;
            }
        }
    }

    final public void setBetweenNearestPoints(boolean un) {
        _useNearest = un;
    }

    final public void setDashed(boolean d) {
        routeFig.setDashed(d);
    }

    /**
     * Set the ArrowHead at the end of this FigEdge.
     */
    public void setDestArrowHead(ArrowHead newArrow) {
        _arrowHeadEnd = newArrow;
    }

    /**
     * Set the FigNode reprenting this FigEdge's to-node.
     */
    public void setDestFigNode(FigNode fn) {
        // assert fn != null
        try {
            if (destFigNode != null) {
                destFigNode.removeFigEdge(this);
            }

            destFigNode = fn;
            fn.addFigEdge(this);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Set the Fig reprenting this FigEdge's to-port.
     */
    final public void setDestPortFig(Fig fig) {
        if (fig == null) {
            throw new IllegalArgumentException(
                    "A destination port must be supplied");
        }
        destPortFig = fig;
    }

    public void setFig(Fig f) {
        if (routeFig != null && routeFig.getGroup() == this) {
            routeFig.setGroup(null);
        }

        routeFig = f;
        routeFig.setGroup(this);
        routeFig.setLayer(getLayer());
    }

    // //////////////////////////////////////////////////////////////
    // Highlightable implementation
    final public void setHighlight(boolean b) {
        _highlight = b;
        damage();
    }

    /**
     * Sets the line color of the edge and of soure/destination arrows.
     *
     * @param c
     */
    public void setLineColor(Color c) {
        routeFig.setLineColor(c);
        setArrowColor(getSourceArrowHead(), c);
        setArrowColor(getDestArrowHead(), c);
    }

    private void setArrowColor(ArrowHead arrow, Color c) {
        if (arrow != null) {
            arrow.setLineColor(c);
        }
    }

    final public void setLineWidth(int w) {
        routeFig.setLineWidth(w);
    }

    final public void setNumPoints(int npoints) {
        routeFig.setNumPoints(npoints);
        calcBounds();
    }

    /**
     * Set the edge (some object in an underlying model) that this FigEdge
     * should represent.
     */
    public void setOwner(Object own) {
        // org.graph.commons.logging.LogFactory.getLog(null).info("Setting owner of " + this + " to " + own);
        Object oldOwner = getOwner();
        if (oldOwner instanceof GraphEdgeHooks) {
            ((GraphEdgeHooks) oldOwner).removePropertyChangeListener(this);
        } else if (oldOwner instanceof Highlightable) {
            ((Highlightable) oldOwner).removePropertyChangeListener(this);
        }

        if (own instanceof GraphEdgeHooks) {
            ((GraphEdgeHooks) own).addPropertyChangeListener(this);
        } else if (own instanceof Highlightable) {
            ((Highlightable) own).addPropertyChangeListener(this);
        }

        super.setOwner(own);
    }

    final public void setPoints(Point[] ps) {
        routeFig.setPoints(ps);
        calcBounds();
    }

    final public void setPoint(int i, int x, int y) {
        routeFig.setPoint(i, x, y);
        calcBounds();
    }

    public void setPoint(Handle h, int x, int y) {
        routeFig.setPoint(h, x, y);
        calcBounds();
    }

    /**
     * Set the ArrowHead at the start of this FigEdge.
     */
    public void setSourceArrowHead(ArrowHead newArrow) {
        _arrowHeadStart = newArrow;
    }

    /**
     * Set the FigNode representing this FigEdge's from-node.
     */
    public void setSourceFigNode(FigNode fn) {
        // assert fn != null
        try {
            if (sourceFigNode != null) {
                sourceFigNode.removeFigEdge(this);
            }

            sourceFigNode = fn;
            fn.addFigEdge(this);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Set the GraphNode representing this FigEdge's from-node.
     */
    public void setSourceGraphNode(GraphNode node) {
        setSourceFigNode((FigNode) node);
    }

    /**
     * Set the GraphNode reprenting this FigEdge's to-node.
     */
    public void setDestGraphNode(GraphNode node) {
        setDestFigNode((FigNode) node);
    }

    /**
     * Get the Fig representing this FigEdge's from-port.
     */
    final public void setSourcePortFig(Fig fig) {
        if (fig == null) {
            throw new IllegalArgumentException("A source port must be supplied");
        }
        sourcePortFig = fig;
    }

    final public void setXs(int[] xs) {
        routeFig.setXs(xs);
        calcBounds();
    }

    final public void setYs(int[] ys) {
        routeFig.setYs(ys);
        calcBounds();
    }

    final public void stuffPointAlongPerimeter(int dist, Point res) {
        routeFig.stuffPointAlongPerimeter(dist, res);
    }

    /**
     * This is used by the SelectionManager and ModeDrag to move an edge because
     * its attached node has been dragged or pushed. TODO: The difference in
     * usage between this and translateImpl is not clear. Can we move this to
     * some controller class?
     *
     * @param dx
     * @param dy
     */
    final public void translateEdge(final int dx, final int dy) {
        routeFig.translate(dx, dy);
        calcBounds();
    }

    public void translateImpl(final int dx, final int dy) {
        routeFig.translate(dx, dy);
        calcBounds();
    }

    final public void updatePathItemLocations() {
        calcBounds();
    }
} /* end class FigEdge */
