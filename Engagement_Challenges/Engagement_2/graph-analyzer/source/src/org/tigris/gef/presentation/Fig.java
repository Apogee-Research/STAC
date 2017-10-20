// $Id: Fig.java 1328 2011-05-21 14:52:00Z bobtarling $
// Copyright (c) 1996,2009 The Regents of the University of California. All
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
package org.tigris.gef.presentation;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.event.MouseEvent;
import java.awt.geom.Line2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;
import javax.swing.JMenu;
import org.tigris.gef.base.AlignAction;
import org.tigris.gef.base.CmdReorder;
import org.tigris.gef.base.Editor;
import org.tigris.gef.base.Geometry;
import org.tigris.gef.base.Globals;
import org.tigris.gef.base.Layer;
import org.tigris.gef.base.LayerDiagram;
import org.tigris.gef.base.Selection;
import org.tigris.gef.base.SelectionManager;
import org.tigris.gef.di.DiagramElement;
import org.tigris.gef.graph.GraphEdgeHooks;
import org.tigris.gef.graph.GraphNodeHooks;
import org.tigris.gef.graph.GraphPortHooks;
import org.tigris.gef.graph.MutableGraphSupport;
import org.tigris.gef.properties.PropCategoryManager;
import org.tigris.gef.ui.PopupGenerator;
import org.tigris.gef.undo.Memento;
import org.tigris.gef.undo.UndoManager;
import org.tigris.gef.util.Localizer;

/**
 * This class is the base class for basic drawing objects such as rectangles,
 * lines, text, circles, etc. Also, class FigGroup implements a composite
 * figure. Fig's are Diagram elements that can be placed in any LayerDiagram.
 * Fig's are also used to define the look of FigNodes on NetNodes.
 */
public abstract class Fig implements DiagramElement, Cloneable,
        java.io.Serializable, PropertyChangeListener, PopupGenerator {

    public Fig parent;
    public Shape shape;

    public double scale = 1.0;

    /**
     * The smallest size that the user can drag this Fig.
     */
    public static final int MIN_SIZE = 4;

    /**
     * The size of the dashes drawn when the Fig is dashed.
     */
    private static final String[] DASHED_CHOICES = {"Solid", "Dashed",
        "LongDashed", "Dotted", "DotDash"};

    private static final float[][] DASH_ARRAYS = {null, {5.0f, 5.0f},
        {15.0f, 5.0f}, {3.0f, 10.0f}, {3.0f, 6.0f, 10.0f, 6.0f}}; // opaque,

    // transparent,
    // [opaque,
    // transparent]
    private static final int[] DASH_PERIOD = {0, 10, 20, 13, 25,}; // the

    // sum
    // of
    // each
    // subarray
    /**
     * Indicates whether this fig can be moved
     */
    boolean movable = true;

    /**
     * Indicates whether this fig can be resized
     */
    boolean resizable = true;

    /**
     * The Layer that this Fig is in. Each Fig can be in exactly one Layer, but
     * there can be multiple Editors on a given Layer.
     */
    private transient Layer layer = null;

    /**
     * True if this object is locked and cannot be moved by the user.
     */
    private boolean locked = false;

    /**
     * Owners are underlying objects that "own" the graphical Fig's that
     * represent them. For example, a FigNode and FigEdge keep a pointer to the
     * net-level object that they represent. Also, any Fig can have NetPort as
     * an owner.
     *
     * @see FigNode#setOwner
     * @see FigNode#bindPort
     */
    private transient Object owner;

    /**
     * X coordinate of the Fig's bounding box. It is the responsibility of
     * subclasses to make sure this value is ALWAYS up-to-date.
     *
     * in 0.13.4 use getX, getBounds or getLocation.
     */
    protected int _x;

    /**
     * Y coordinate of the Fig's bounding box. It is the responsibility of
     * subclasses to make sure this value is ALWAYS up-to-date.
     *
     * in 0.13.4 use getY, getBounds or getLocation.
     */
    protected int _y;

    /**
     * Width of the Fig's bounding box. It is the responsibility of subclasses
     * to make sure this value is ALWAYS up-to-date.
     *
     * in 0.13.4 use getWidth, getBounds or getSize.
     */
    protected int _w;

    /**
     * Height of the Fig's bounding box. It is the responsibility of subclasses
     * to make sure this value is ALWAYS up-to-date.
     *
     * in 0.13.4 use getWidth, getBounds or getSize.
     */
    protected int _h;

    /**
     * Name of the resource being basis to this figs localization.
     */
    private String resource = "";

    /**
     * Outline color of fig object.
     *
     * in 0.13.4 use getLineColor/setLineColor.
     */
    Color _lineColor = Color.black;

    /**
     * Fill color of fig object.
     *
     * in 0.13.4 use getFillColor/setFillColor.
     */
    Color _fillColor = Color.white;

    /**
     * Thickness of object's border. This is included in the overall size of the
     * object, so the size of the interior is smaller by 2 * lineWidth in each
     * dimension.
     *
     * will become private use set/getLineWidth()
     */
    int _lineWidth = 1;

    /**
     * in 0.13.4 use getDashes
     */
    protected float[] _dashes = null;

    /**
     * this is not used
     */
    protected int _dashStyle = 0;

    /**
     * in 0.13.4 use getDashePeriod
     */
    protected int _dashPeriod = 0;

    /**
     * True if the object should fill in its area.
     *
     * will become private use getLineWidth()
     */
    protected boolean _filled = true;

    /**
     * The parent Fig of which this Fig is a child
     */
    private Fig group = null;

    protected String _context = "";

    /**
     * True if the Fig is visible
     */
    private boolean visible = true;

    /**
     * this is not used
     */
    protected boolean _allowsSaving = true;

    /**
     * by mvw in GEF0.13.1M2. Use SelectionManager instead. See issue 146. This
     * value is never set.
     *
     */
    private transient boolean _selected = false;

    /**
     * This flag is set at the start of the removal process. It is later used
     * for testing to confirm that all removed figs have actually gone from all
     * layers.
     */
    private boolean removeStarted;

    // //////////////////////////////////////////////////////////////
    // static initializer
    static {
        PropCategoryManager.categorizeProperty("Geometry", "x");
        PropCategoryManager.categorizeProperty("Geometry", "y");
        PropCategoryManager.categorizeProperty("Geometry", "width");
        PropCategoryManager.categorizeProperty("Geometry", "height");
        PropCategoryManager.categorizeProperty("Geometry", "filled");
        PropCategoryManager.categorizeProperty("Geometry", "locked");
        PropCategoryManager.categorizeProperty("Style", "lineWidth");
        PropCategoryManager.categorizeProperty("Style", "fillColor");
        PropCategoryManager.categorizeProperty("Style", "lineColor");
        PropCategoryManager.categorizeProperty("Style", "filled");
    }

    // //////////////////////////////////////////////////////////////
    // geometric manipulations
    /**
     * Margin between this Fig and automatically routed arcs.
     */
    public static final int BORDER = 8;
    public String containername;

    /**
     * Most subclasses will not use this constructor, it is only useful for
     * subclasses that redefine most of the infrastructure provided by class
     * Fig.
     */
    public Fig() {
        an = NoAnnotationStrategy.getInstance();
    }

    /**
     * Construct a new Fig with the given bounds.
     */
    public Fig(int x, int y, int w, int h) {
        /* Do not set the owner to null when none is given: */
        this(x, y, w, h, Color.black, Color.white);
    }

    /**
     * Construct a new Fig with the given bounds and colors.
     */
    public Fig(int x, int y, int w, int h, Color lineColor, Color fillColor) {
        /* Do not set the owner to null when none is given: */
        this();
        _x = x;
        _y = y;
        _w = w;
        _h = h;
        if (lineColor != null) {
            _lineColor = lineColor;
        } else {
            _lineWidth = 0;
        }

        if (fillColor != null) {
            _fillColor = fillColor;
        } else {
            _filled = false;
        }

    }

    /**
     * Construct a new Fig with the given bounds, colors, and owner.
     */
    public Fig(int x, int y, int w, int h, Color lineColor, Color fillColor,
            Object own) {
        this(x, y, w, h, lineColor, fillColor);

        setOwner(own);
        // annotation related
    }

    /**
     * Feature removed. It is unrealistic that different Figs will have
     * different locales.
     */
    public void setResource(String resource) {
        this.resource = resource;
    }

    /**
     * Feature removed. It is unrealistic that different Figs will have
     * different locales.
     */
    public String getResource() {
        return resource;
    }

    // --------------------------------
    // annotation related
    protected AnnotationStrategy an = NoAnnotationStrategy.getInstance();

    protected boolean annotationStatus = false;

    protected Fig annotationOwner;

    // specifies the AnnotationOwner
    public void setAnnotationOwner(Fig f) {
        annotationOwner = f;
        setAnnotationStatus(annotationOwner != null);
    }

    // fig is not an annotation any longer
    public void unsetAnnotationOwner() {
        annotationOwner = null;
        setAnnotationStatus(false);
    }

    public Fig getAnnotationOwner() {
        return annotationOwner;
    }

    /**
     * USED BY PGML.tee
     */
    public AnnotationStrategy getAnnotationStrategy() {
        return an;
    }

    /**
     * Set the AnnotationStrategy for this Fig. using this method will discard
     * the previous AnnotationStrategy
     */
    public void setAnnotationStrategy(AnnotationStrategy a) {
        an = a;
    }

    /**
     * returns true if this fig is an annotation of any other fig
     */
    public boolean isAnnotation() {
        return annotationStatus;
    }

    // TODO Do we ever want to call this, or just override isAnnotation?
    public void setAnnotationStatus(boolean newValue) {
        annotationStatus = newValue;
    }

    /**
     * Adds a new Annotation of type "text" to fig.
     */
    // Only used by PGMLParser
    final public void addAnnotation(Fig annotation, String type, String context) {
    }

    // Unused method
    //
    // final public void removeAnnotation(String context) {
    // }
    final public void removeAnnotation(Fig annotationFig) {
        if (annotationFig.isAnnotation()
                && (this == annotationFig.getAnnotationOwner())) {
            Globals.curEditor().remove(annotationFig);
            getAnnotationStrategy().removeAnnotation(annotationFig);
        }
    }

    /**
     * Fig has been moved: Adjust the annotation positions Extracted from
     * endTrans() so that annotation positions can be updated without redrawing
     * everything.
     */
    // TODO This is only required by SelectionManager. Should this be package
    // private
    // an move SelectionManager into this package?
    final public void translateAnnotations() {
        // If this Fig is an annotation itself, simply store the position at the
        // owner.
        if (this.isAnnotation()) {
            SelectionManager selectionManager = Globals.curEditor()
                    .getSelectionManager();
            if (!(selectionManager.containsFig(this.getAnnotationOwner()))
                    && selectionManager.containsFig(this)) {
                (getAnnotationOwner().an).storeAnnotationPosition(this);
            }
        }

        // If this Fig is owner of annotations then move the annotations
        // according to the Fig's own position.
        if (!(getAnnotationStrategy() instanceof NoAnnotationStrategy)) {
            getAnnotationStrategy().translateAnnotations(this);
        }
    }

    /**
     * Updates the positions of the connected annotations.
     */
    final public void updateAnnotationPositions() {
        Enumeration annotations = getAnnotationStrategy().getAllAnnotations();
        while (annotations.hasMoreElements()) {
            Fig annotation = (Fig) annotations.nextElement();
            getAnnotationStrategy().storeAnnotationPosition(annotation);
            annotation.endTrans();
        }

        endTrans();
    }

    // Only used by PGMLParser
    final public void initAnnotations() {
    }

    // end annotation related
    // -----------------------------------
    /**
     * Add a point to this fig. sub classes should implement. TODO: Why isn't
     * this extended by FigEdgePoly?
     */
    public void addPoint(int x, int y) {
    }

    /**
     * The specified PropertyChangeListeners <b>propertyChange</b> method will
     * be called each time the value of any bound property is changed. Note: the
     * JavaBeans specification does not require PropertyChangeListeners to run
     * in any particular order.
     * <p>
     *
     * Since most Fig's will never have any listeners, and I want Figs to be
     * fairly light-weight objects, listeners are kept in a global Hashtable,
     * keyed by Fig. NOTE: It is important that all listeners eventually remove
     * themselves, otherwise this will prevent garbage collection.
     */
    final public void addPropertyChangeListener(PropertyChangeListener l) {
        Globals.addPropertyChangeListener(this, l);
    }

    /**
     * Remove this PropertyChangeListener from the JavaBeans internal list. If
     * the PropertyChangeListener isn't on the list, silently do nothing.
     */
    final public void removePropertyChangeListener(PropertyChangeListener l) {
        Globals.removePropertyChangeListener(this, l);
    }

    /**
     * Align this Fig with the given rectangle. Some subclasses may need to know
     * the editor that initiated this action.
     *
     * @param r the rectangle to align to.
     * @param direction
     * @param ed the editor that initiated this action.
     */
    final public void align(Rectangle r, int direction, Editor ed) {
        Rectangle bbox = getBounds();
        int dx = 0;
        int dy = 0;
        switch (direction) {

            case AlignAction.ALIGN_TOPS:
                dy = r.y - bbox.y;
                break;

            case AlignAction.ALIGN_BOTTOMS:
                dy = r.y + r.height - (bbox.y + bbox.height);
                break;

            case AlignAction.ALIGN_LEFTS:
                dx = r.x - bbox.x;
                break;

            case AlignAction.ALIGN_RIGHTS:
                dx = r.x + r.width - (bbox.x + bbox.width);
                break;

            case AlignAction.ALIGN_CENTERS:
                dx = r.x + r.width / 2 - (bbox.x + bbox.width / 2);
                dy = r.y + r.height / 2 - (bbox.y + bbox.height / 2);
                break;

            case AlignAction.ALIGN_H_CENTERS:
                dx = r.x + r.width / 2 - (bbox.x + bbox.width / 2);
                break;

            case AlignAction.ALIGN_V_CENTERS:
                dy = r.y + r.height / 2 - (bbox.y + bbox.height / 2);
                break;

            case AlignAction.ALIGN_TO_GRID:
                Point loc = getLocation();
                Point snapPt = new Point(loc.x, loc.y);
                ed.snap(snapPt);
                dx = snapPt.x - loc.x;
                dy = snapPt.y - loc.y;
                break;
        }

        translate(dx, dy);
    }

    /**
     * Update the bounds of this Fig. By default it is assumed that the bounds
     * have already been updated, so this does nothing.
     *
     * @see FigText#calcBounds
     */
    public void calcBounds() {
    }

    // note: computing non-intersection is faster on average. Maybe I
    // should structure the API to allow clients to take advantage of that?
    /**
     * Return the center of the given Fig. By default the center is the center
     * of its bounding box. Subclasses may want to define something else.
     *
     * in 0.11.1 Use getCenter();
     */
    // USED BY PGML.tee
    final public Point center() {
        return getCenter();
    }

    /**
     * Return the center of the given Fig. By default the center is the center
     * of its bounding box. Subclasses may want to define something else.
     */
    // USED BY PGML.tee
    public Point getCenter() {
        Rectangle bbox = getBounds();
        return new Point(bbox.x + bbox.width / 2, bbox.y + bbox.height / 2);
    }

    /**
     * in 0.11.1 use
     * org.tigris.gef.persistence.pgml.PgmlUtility.getClassNameAndBounds (Fig)
     */
    // USED BY PGML.tee
    public String classNameAndBounds() {
        return getClass().getName() + "[" + getX() + ", " + getY() + ", "
                + getWidth() + ", " + getHeight() + "]";
    }

    public void cleanUp() {
    }

    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }

    /**
     * Can the fig can be copied and pasted
     */
    public boolean isCopyable() {
        return true;
    }

    /**
     * Can the fig can be cut and pasted
     */
    public boolean isCutable() {
        return true;
    }

    /**
     * Return a point that should be used for arcs that go toward the given
     * point. By default, this makes arcs end on the edge that is nearest the
     * given point.
     *
     * needs-more-work: define gravity points, berths
     */
    public Point connectionPoint(Point anotherPt) {
        List grav = getGravityPoints();
        if (grav != null && grav.size() > 0) {
            int ax = anotherPt.x;
            int ay = anotherPt.y;
            Point bestPoint = (Point) grav.get(0);
            int bestDist = Integer.MAX_VALUE;
            int size = grav.size();
            for (int i = 0; i < size; i++) {
                Point gp = (Point) grav.get(i);
                int dx = gp.x - ax;
                int dy = gp.y - ay;
                int dist = dx * dx + dy * dy;
                if (dist < bestDist) {
                    bestDist = dist;
                    bestPoint = gp;
                }
            }

            return new Point(bestPoint.x, bestPoint.y);
        }

        return getClosestPoint(anotherPt);
    }

    /**
     * Reply true if the given point is inside the given Fig. By default reply
     * true if the point is in my bounding box. Subclasses like FigCircle and
     * FigEdge do more specific checks.
     */
    public boolean contains(int x, int y) {
        return (_x <= x) && (x <= _x + _w) && (_y <= y) && (y <= _y + _h);
    }

    /**
     * Reply true if the given point is inside this Fig by calling contains(int
     * x, int y).
     */
    final public boolean contains(Point p) {
        return contains(p.x, p.y);
    }

    /**
     * Reply true if the all four corners of the given rectangle are inside this
     * Fig, as determined by contains(int x, int y).
     */
    final public boolean contains(Rectangle r) {
        return countCornersContained(r.x, r.y, r.width, r.height) == 4;
    }

    /**
     * Reply the number of corners of the given rectangle that are inside this
     * Fig, as determined by contains(int x, int y).
     */
    protected int countCornersContained(int x, int y, int w, int h) {
        int cornersHit = 0;
        if (contains(x, y)) {
            cornersHit++;
        }

        if (contains(x + w, y)) {
            cornersHit++;
        }

        if (contains(x, y + h)) {
            cornersHit++;
        }

        if (contains(x + w, y + h)) {
            cornersHit++;
        }

        return cornersHit;
    }

    /**
     * Resize the object for drag on creation. It bypasses the things done in
     * resize so that the position of the object can be kept as the anchor
     * point. Needs-More-Work: do I really need this function?
     *
     * @see FigLine#createDrag
     */
    public void createDrag(int anchorX, int anchorY, int x, int y, int snapX,
            int snapY) {
        int newX = Math.min(anchorX, snapX);
        int newY = Math.min(anchorY, snapY);
        int newW = Math.max(anchorX, snapX) - newX;
        int newH = Math.max(anchorY, snapY) - newY;
        setBounds(newX, newY, newW, newH);
    }

    /**
     * This is called after an Cmd modifies a Fig and the Fig needs to be
     * redrawn in its new position.
     */
    public void endTrans() {
        translateAnnotations();
        damage();
    }

    /**
     * This Fig has changed in some way, tell its Layer to record my bounding
     * box as a damageAll region so that I will eventualy be redrawn.
     */
    public void damage() {
        Layer lay = getLayer();
        Fig group = getGroup();
        while (lay == null && group != null) {
            lay = group.getLayer();
            group = group.getGroup();
        }
        if (lay != null) {
            lay.damageAll();
            // lay.damaged(this);
        }
    }

    /**
     * Get the rectangle on whose corners the dragging handles are to be drawn.
     * Should be overwritten by Figures with Bounds larger than the HandleBox.
     * Normally these should be identical.
     */
    public Rectangle getHandleBox() {
        return getBounds();
    }

    /**
     * Set the HandleBox. Normally this should not be used. It is intended for
     * figures where the Handlebox is different from the Bounds. Overide this
     * method if HandleBox and bounds differ
     */
    public void setHandleBox(int x, int y, int w, int h) {
        setBounds(x, y, w, h);
    }

    // //////////////////////////////////////////////////////////////
    // Editor API
    /**
     * Remove this Fig from the Layer it belongs to.
     */
    public void removeFromDiagram() {

        if (UndoManager.getInstance().isGenerateMementos()) {
            class FigRemoveMemento extends Memento {

                Layer lay;

                Fig fig;

                Fig encFig;

                boolean vis;

                public FigRemoveMemento(Fig f) {
                    fig = f;
                    lay = fig.getLayer();
                    encFig = f.getEnclosingFig();
                    vis = fig.isVisible();
                }

                public void undo() {
                    UndoManager.getInstance().addMementoLock(this);
                    fig.setEnclosingFig(encFig);
                    if (lay != null) {
                        lay.add(fig);
                    }
                    fig.visible = vis;
                    UndoManager.getInstance().removeMementoLock(this);
                }

                public void redo() {
                    UndoManager.getInstance().addMementoLock(this);
                    fig.removeFromDiagram();
                    UndoManager.getInstance().removeMementoLock(this);
                }
            }
            ;
            UndoManager.getInstance().addMemento(new FigRemoveMemento(this));
        }

        removeStarted = true;
        visible = false;

        // delete all annotations first
        java.util.Enumeration iter = getAnnotationStrategy()
                .getAllAnnotations();
        while (iter.hasMoreElements()) {
            Fig annotation = (Fig) iter.nextElement();
            getAnnotationStrategy().getAnnotationProperties(annotation)
                    .removeLine();
            removeAnnotation(annotation);
            annotation.removeFromDiagram();
        }

        if (layer != null) {
            Layer oldLayer = layer;
            layer.remove(this);
            oldLayer.deleted(this);
        }

        // ak: remove this figure from the enclosed figures of the encloser
        setEnclosingFig(null);
    }

    /**
     * Delete whatever application object this Fig is representing, the Fig
     * itself should automatically be deleted as a side-effect. Simple Figs have
     * no underlying model, so they are just deleted. Figs that graphically
     * present some part of an underlying model should NOT delete themselves,
     * instead they should ask the model to dispose, and IF it does then the
     * figs will be notified.
     */
    public void deleteFromModel() {
        removeStarted = true;
        Object own = getOwner();
        if (own instanceof GraphNodeHooks) {
            ((GraphNodeHooks) own).deleteFromModel();
        } else if (own instanceof GraphEdgeHooks) {
            ((GraphEdgeHooks) own).deleteFromModel();
        } else if (own instanceof GraphPortHooks) {
            ((GraphPortHooks) own).deleteFromModel();
        } else {
            removeFromDiagram();
        }
    }

    // protected void drawDashedPerimeter(Graphics g) {
    // Point segStart = new Point();
    // Point segEnd = new Point();
    // int numDashes = _dashes.length;
    // int length = getPerimeterLength();
    // int i = 0;
    // int d = 0;
    // while(i < length) {
    // stuffPointAlongPerimeter(i, segStart);
    // i += _dashes[d];
    // d = (d + 1) % numDashes;
    // stuffPointAlongPerimeter(i, segEnd);
    // g.drawLine(segStart.x, segStart.y, segEnd.x, segEnd.y);
    // i += _dashes[d];
    // d = (d + 1) % numDashes;
    // }
    // }
    //
    public void drawRect(final Graphics g, final boolean filled,
            final Color fillColor, final int lineWidth, final Color lineColor,
            final int x, final int y, final int w, final int h,
            final boolean dashed, final float dashes[], final int dashPeriod) {

        if (filled && fillColor != null) {
            // TODO This causes loss of border edges in various FigNodes
            // in ArgoUML. See sequence diagram and deployment diagrams.
            // Disabling until cause is known.
            // if (g instanceof Graphics2D) {
            // Graphics2D g2 = (Graphics2D) g;
            // Paint oldPaint = g2.getPaint();
            // g2.setPaint(getDefaultPaint(fillColor, lineColor, x, y, w, h));
            // g2.fill(new Rectangle2D.Float(x + lineWidth, y + lineWidth,
            // w - 2 * lineWidth, h - 2 * lineWidth));
            // g2.setPaint(oldPaint);
            // } else {
            int xx = x;
            int yy = y;
            int ww = w;
            int hh = h;
            if (lineColor != null) {
                if (lineWidth > 1 && !dashed && lineColor != fillColor) {
                    int lineWidth2 = lineWidth * 2;
                    g.setColor(lineColor);
                    g.fillRect(xx, yy, ww, hh);
                    xx += lineWidth;
                    yy += lineWidth;
                    ww -= lineWidth2;
                    hh -= lineWidth2;
                }
            }
            g.setColor(fillColor);
            g.fillRect(xx, yy, ww, hh);
            if (lineColor != null && lineColor != fillColor) {
                if (lineWidth == 1 || dashed) {
                    paintRectLine(g, xx, yy, ww, hh, lineWidth, lineColor,
                            dashed, dashes, dashPeriod);
                }
            }
            // }
        } else {
            paintRectLine(g, x, y, w, h, lineWidth, lineColor, dashed, dashes,
                    dashPeriod);
        }
    }

    /**
     * Paint the line of a rectangle without any fill. Manages line width and
     * dashed lines.
     *
     * @param g The Graphics object
     * @param x The x co-ordinate of the rectangle
     * @param y The y co-ordinate of the rectangle
     * @param w The width of the rectangle
     * @param h The height of the rectangle
     * @param lwidth The linewidth of the rectangle
     */
    private void paintRectLine(Graphics g, int x, int y, int w, int h,
            int lineWidth, Color lineColor, boolean dashed, float dashes[],
            int dashPeriod) {
        // TODO This causes an underline in the association label of ArgoUML
        // Disabling until cause is known.
        // if (g instanceof Graphics2D) {
        // Graphics2D g2 = (Graphics2D) g;
        // Paint oldPaint = g2.getPaint();
        // g2.setPaint(lineColor);
        // Stroke oldStroke = g2.getStroke();
        // g2.setStroke(getDefaultStroke(lineWidth, dashes, 0));
        // g2.draw(new Rectangle2D.Float(x, y, w, h));
        // g2.setStroke(oldStroke);
        // g2.setPaint(oldPaint);
        // } else {
        if (lineWidth > 0 && lineColor != null) {
            g.setColor(lineColor);
            if (lineWidth == 1) {
                paintRectLine(g, x, y, w, h, dashed, lineWidth, dashes,
                        dashPeriod);
            } else {
                int xx = x;
                int yy = y;
                int hh = h;
                int ww = w;

                for (int i = 0; i < lineWidth; ++i) {
                    paintRectLine(g, xx++, yy++, ww, hh, dashed, lineWidth,
                            dashes, dashPeriod);
                    ww -= 2;
                    hh -= 2;
                }
            }
        }
        // }
    }

    private void paintRectLine(Graphics g, int x, int y, int w, int h,
            boolean dashed, int lineWidth, float dashes[], int dashPeriod) {
        if (!dashed) {
            g.drawRect(x, y, w, h);
        } else {
            drawDashedRectangle(g, 0, x, y, w, h, lineWidth, dashes, dashPeriod);
        }
    }

    private void drawDashedRectangle(Graphics g, int phase, int x, int y,
            int w, int h, int lineWidth, float dashes[], int dashPeriod) {

        phase = drawDashedLine(g, lineWidth, x, y, x + w, y, phase, dashes,
                dashPeriod);
        phase = drawDashedLine(g, lineWidth, x + w, y, x + w, y + h, phase,
                dashes, dashPeriod);
        phase = drawDashedLine(g, lineWidth, x + w, y + h, x, y + h, phase,
                dashes, dashPeriod);
        phase = drawDashedLine(g, lineWidth, x, y + h, x, y, phase, dashes,
                dashPeriod);
    }

    public int drawDashedLine(Graphics g, int lineWidth, int x1, int y1,
            int x2, int y2, int phase, float[] dashes, int dashPeriod) {
        if (g instanceof Graphics2D) {
            return drawDashedLineG2D((Graphics2D) g, lineWidth, phase, x1, y1,
                    x2, y2, dashes, dashPeriod);
        }

        // Fall back on the old inefficient method of drawing dashed
        // lines. This is required until SVGWriter is converted to
        // extend Graphics2D. This ignores the line width.
        int segStartX;
        int segStartY;
        int segEndX;
        int segEndY;
        int dxdx = (x2 - x1) * (x2 - x1);
        int dydy = (y2 - y1) * (y2 - y1);
        int length = (int) Math.sqrt(dxdx + dydy);
        int numDashes = dashes.length;
        int d;
        int dashesDist = 0;
        for (d = 0; d < numDashes; d++) {
            dashesDist += dashes[d];
            // find first partial dash?
        }

        d = 0;
        int i = 0;
        while (i < length) {
            segStartX = x1 + ((x2 - x1) * i) / length;
            segStartY = y1 + ((y2 - y1) * i) / length;
            i += dashes[d];
            d = (d + 1) % numDashes;
            if (i >= length) {
                segEndX = x2;
                segEndY = y2;
            } else {
                segEndX = x1 + ((x2 - x1) * i) / length;
                segEndY = y1 + ((y2 - y1) * i) / length;
            }

            g.drawLine(segStartX, segStartY, segEndX, segEndY);
            i += dashes[d];
            d = (d + 1) % numDashes;
        }

        // needs-more-work: phase not taken into account
        return (length + phase) % dashesDist;
    }

    private int drawDashedLineG2D(Graphics2D g, int lineWidth, int phase,
            int x1, int y1, int x2, int y2, float[] dashes, int dashPeriod) {
        int dxdx = (x2 - x1) * (x2 - x1);
        int dydy = (y2 - y1) * (y2 - y1);
        int length = (int) (Math.sqrt(dxdx + dydy) + 0.5); // This causes a
        // smaller rounding
        // error of
        // 0.5pixels max. .
        // Seems acceptable.

        Stroke originalStroke = g.getStroke(); // we need this to restore
        // the original stroke
        // afterwards

        Stroke dashedStroke = getDefaultStroke(lineWidth, dashes, phase);
        g.setStroke(dashedStroke);
        g.draw(new Line2D.Float(x1, y1, x2, y2));
        g.setStroke(originalStroke); // undo the manipulation of g

        return (length + phase) % dashPeriod;
    }

    final public void firePropChange(String propName, int oldV, int newV) {
        firePropChange(propName, new Integer(oldV), new Integer(newV));
    }

    /**
     * Creates a PropertyChangeEvent and calls all registered listeners
     * propertyChanged() method.
     */
    final public void firePropChange(String propName, Object oldV, Object newV) {
        Globals.firePropChange(this, propName, oldV, newV);
        if (group != null) {
            PropertyChangeEvent pce = new PropertyChangeEvent(this, propName,
                    oldV, newV);
            group.propertyChange(pce);
        }
    }

    final public void firePropChange(String propName, boolean oldV, boolean newV) {
        firePropChange(propName, new Boolean(oldV), new Boolean(newV));
    }

    /**
     * Return a Rectangle that completely encloses this Fig. Subclasses may
     * override getBounds(Rectangle).
     */
    // USED BY PGML.tee
    public final Rectangle getBounds() {
        return getBounds(null);
    }

    /**
     * Stores the Rectangle that completely encloses this Fig into "return
     * value" <b>r</b> and return <b>r</b>. If r is <code>null</code> a new
     * <code>Rectangle</code> is allocated. This version of
     * <code>getBounds</code> is useful if the caller wants to avoid allocating
     * a new <code>Rectangle</code> object on the heap.
     *
     * @param r the return value, modified to the components bounds
     * @return r
     */
    public Rectangle getBounds(Rectangle r) {
        if (r == null) {
            return new Rectangle(_x, _y, _w, _h);
        }
        r.setBounds(_x, _y, _w, _h);
        return r;
    }

    /**
     * Overrule this if you have a non-rectangular shape, and want the edge to
     * be able to attach to locations on line-segments.
     *
     * @param anotherPt the point (usually outside the fig) to connet to
     * @return a point on the border of this
     */
    public Point getClosestPoint(Point anotherPt) {
        return Geometry.ptClosestTo(getBounds(), anotherPt);
    }

    /**
     * Get the dashed attribute
     */
    public boolean getDashed() {
        return (_dashes != null);
    }

    public float[] getDashes() {
        return _dashes;
    }

    public int getDashPeriod() {
        return _dashPeriod;
    }

    /**
     * in 0.11.1 us org.tigris.gef.persistence.pgml.PgmlUtility.getDashed(Fig)
     */
    // USED by PGML.tee
    final public int getDashed01() {
        return getDashed() ? 1 : 0;
    }

    final public String getDashedString() {
        return (_dashes == null) ? DASHED_CHOICES[0] : DASHED_CHOICES[1];
    }

    public Vector getEnclosedFigs() {
        return null;
    }

    /**
     * USED BY PGML.tee
     */
    public Fig getEnclosingFig() {
        return null;
    }

    /**
     * Does this Fig support the concept of "fill color" in principle
     *
     * @return true if the Fig can be filled
     */
    public boolean hasFillColor() {
        return true;
    }

    public Color getFillColor() {
        return _fillColor;
    }

    /**
     * use isFilled()
     *
     * @return
     */
    public boolean getFilled() {
        return _filled;
    }

    public boolean isFilled() {
        return _filled;
    }

    /**
     * in 0.11.1 us org.tigris.gef.persistence.pgml.PgmlUtility.getDashed(Fig)
     */
    final public int getFilled01() {
        return _filled ? 1 : 0;
    }

    /**
     * Does this Fig support the concept of "line color" in principle
     *
     * @return true if the Fig can have a line color
     */
    public boolean hasLineColor() {
        return true;
    }

    /**
     * USED BY SVG.tee
     */
    public Color getLineColor() {
        return _lineColor;
    }

    /**
     * USED BY SVG.tee
     */
    public int getLineWidth() {
        return _lineWidth;
    }

    /**
     * TODO: Should be abstract. Is this needed on Fig or FigEdge
     */
    public Point getFirstPoint() {
        return new Point();
    }

    /**
     * Overrule this if you want to connect to a limited number of points, and
     * to points only.
     * <p>
     * Instead, if you want to connect to any point on one or more
     * line-segments, then you should overrule getClosestPoint().
     *
     * @return the list of gravity points.
     */
    public List getGravityPoints() {
        return null;
    }

    final public Fig getGroup() {
        return group;
    }

    /**
     * TODO must determine the purpose of this.
     *
     * @return the context of the Fig.
     */
    // USED BY PGML.tee
    final public String getContext() {
        return _context;
    }

    /**
     * in 0.11.1 Call getHeight() and half it.
     *
     * @return
     */
    /*
     * USED BY PGML.tee
     */
    final public int getHalfHeight() {
        return _h / 2;
    }

    /**
     * in 0.11.1 Call getHeight() and half it.
     *
     * @return
     */
    /*
     * USED BY PGML.tee
     */
    final public int getHalfWidth() {
        return _w / 2;
    }

    /*
     * USED BY PGML.tee
     */
    public String getId() {
        if (getGroup() != null) {
            String gID = getGroup().getId();
            if (getGroup() instanceof FigGroup) {
                return gID
                        + "."
                        + ((List) ((FigGroup) getGroup()).getFigs())
                        .indexOf(this);
            } else if (getGroup() instanceof FigEdge) {
                return gID
                        + "."
                        + (((List) ((FigEdge) getGroup()).getPathItemFigs())
                        .indexOf(this) + 1);
            } else {
                return gID + ".0";
            }
        }

        Layer layer = getLayer();
        if (layer == null) {
            return "LAYER_NULL";
        }

        List c = (List) layer.getContents();
        int index = c.indexOf(this);
        return "Fig" + index;
    }

    /**
     * TODO: Should be abstract. Is this needed on Fig or FigEdge
     */
    public Point getLastPoint() {
        return new Point();
    }

    final public Layer getLayer() {
        return layer;
    }

    /**
     * Returns a point that is the upper left corner of the Fig's bounding box.
     * Implementation creates a new Point instance, consider getX() and getY()
     * for performance.
     */
    final public Point getLocation() {
        return new Point(_x, _y);
    }

    final public boolean getLocked() {
        return locked;
    }

    /**
     * Returns the minimum size of the Fig. This is the smallest size that the
     * user can make this Fig by dragging. You can ignore this and make Figs
     * smaller programmitically if you must. TODO: return a single instance of
     * an immutable Dimension
     */
    public Dimension getMinimumSize() {
        return new Dimension(MIN_SIZE, MIN_SIZE);
    }

    public int getNumPoints() {
        return 0;
    }

    /**
     * Return the model element that this Fig represents. USED BY PGML.tee
     */
    public Object getOwner() {
        return owner;
    }

    /**
     * Return the length of the path around this Fig. By default, returns the
     * perimeter of the Fig's bounding box. Subclasses like FigPoly have more
     * specific logic.
     */
    public int getPerimeterLength() {
        return _w + _w + _h + _h;
    }

    public Point[] getPoints() {
        return new Point[0];
    }

    public Point getPoint(int i) {
        return null;
    }

    public Vector getPopUpActions(MouseEvent me) {
        Vector popUpActions = new Vector();
        JMenu orderMenu = new JMenu(Localizer.localize("PresentationGef",
                "Ordering"));
        orderMenu.setMnemonic((Localizer.localize("PresentationGef",
                "OrderingMnemonic")).charAt(0));
        orderMenu.add(CmdReorder.BringForward);
        orderMenu.add(CmdReorder.SendBackward);
        orderMenu.add(CmdReorder.BringToFront);
        orderMenu.add(CmdReorder.SendToBack);
        popUpActions.addElement(orderMenu);
        return popUpActions;
    }

    /**
     * Returns the prefered size of the Fig. This will be useful for automated
     * layout. By default just uses the current size. Subclasses must override
     * to return something useful.
     */
    final public Dimension getPreferredSize() {
        return new Dimension(_w, _h);
    }

    /**
     * in 0.11.1 this should not form part of the API
     */
    // Used in SVG.TEE
    public String getPrivateData() {
        return "";
    }

    /**
     * Returns the size of the Fig.
     */
    public Dimension getSize() {
        return new Dimension(_w, _h);
    }

    public String getTipString(MouseEvent me) {
        if (owner == null) {
            return toString();
        }
        return owner.toString();
    }

    public Rectangle getTrapRect() {
        return getBounds();
    }

    /**
     * Get the rectangle bounds of each area in this Fig that can trap and
     * enclose a FigNode.
     *
     * @return the list of Rectangles
     */
    public List<Rectangle> getTrapRects(Fig de) {
        ArrayList<Rectangle> rects = new ArrayList<Rectangle>(1);
        rects.add(getTrapRect());
        return rects;
    }

    public boolean getUseTrapRect() {
        return false;
    }

    /**
     * use PgmlUtility.visibilityToString(Fig f)
     */
    // USED BY PGML.tee
    final public int getVisState() {
        if (isVisible()) {
            return 1;
        }
        return 0;
    }

    /**
     * Get the current width of the Fig.
     */
    // USED BY PGML.tee
    final public int getWidth() {
        return _w;
    }

    /**
     * Get the current height of the Fig.
     */
    // USED BY PGML.tee
    final public int getHeight() {
        return _h;
    }

    /**
     * Get the x position of the Fig.
     */
    // USED BY PGML.tee
    final public int getX() {
        return _x;
    }

    /**
     * Get the y position of the Fig.
     */
    // USED BY PGML.tee
    final public int getY() {
        return _y;
    }

    public int[] getXs() {
        return new int[0];
    }

    public int[] getYs() {
        return new int[0];
    }

    /**
     * Determine if the given rectangle contains some pixels of the Fig. This is
     * used to determine if the user is trying to select this Fig. Rather than
     * ask if the mouse point is in the Fig, I use a small rectangle around the
     * mouse point so that small objects and lines are easier to select. If the
     * fig is invisible this method always returns false.
     *
     * @param r the rectangular hit area
     * @return true if the hit rectangle strikes this fig
     */
    public boolean hit(Rectangle r) {
        if (!isVisible() || !isSelectable()) {
            return false;
        }
        int cornersHit = countCornersContained(r.x, r.y, r.width, r.height);
        if (_filled) {
            return cornersHit > 0;
        } else {
            return cornersHit > 0 && cornersHit < 4;
        }
    }

    public void insertPoint(int i, int x, int y) {
    }

    /**
     * Reply true if the object intersects the given rectangle. Used for
     * selective redrawing and by ModeSelect to select all Figs that are partly
     * within the selection rectangle.
     * <p>
     * Note: comparisons are strict (e.g. '<' instead of '<='), so that figs
     * with zero height or width are handled correctly.
     */
    public boolean intersects(Rectangle r) {
        return !((r.x + r.width < _x) || (r.y + r.height < _y)
                || (r.x > _x + _w) || (r.y > _y + _h));
    }

    /**
     * Reply true if the object's perimeter intersects the given rectangle. Used
     * for selective redrawing and by ModeSelect to select all Figs that are
     * partly within the selection rectangle.
     * <p>
     * Note: comparisons are strict (e.g. '<' instead of '<='), so that figs
     * with zero height or width are handled correctly.
     */
    public boolean intersectsPerimeter(Rectangle r) {
        return (r.intersectsLine(_x, _y, _x, _y + _h)
                && r.intersectsLine(_x, _y + _h, _x + _w, _y + _h)
                && r.intersectsLine(_x + _w, _y + _h, _x + _w, _y) && r
                .intersectsLine(_x + _w, _y, _x, _y));
    }

    /**
     * Returns true if this Fig can be resized by the user.
     */
    final public boolean isLowerRightResizable() {
        return false;
    }

    /**
     * Returns true if this Fig can be moved around by the user.
     */
    final public boolean isMovable() {
        return movable;
    }

    /**
     * Returns true if this Fig can be reshaped by the user.
     */
    public boolean isReshapable() {
        return false;
    }

    /**
     * Determine if this Fig can be resized
     *
     * @return true if this Fig can be resized by the user.
     */
    public boolean isResizable() {
        return resizable;
    }

    /**
     * Determine if this Fig can be selected
     *
     * @return true if this Fig can be selected by the user.
     */
    public boolean isSelectable() {
        return true;
    }

    /**
     * Returns true if this Fig can be rotated by the user.
     */
    public boolean isRotatable() {
        return false;
    }

    /**
     * Returns the current selection state for this item
     *
     * @return True, if the item is currently selected, otherwise false. by mvw
     * in GEF0.13.1M2. Use SelectionManager instead. See issue 146. This value
     * is never set.
     */
    final public boolean isSelected() {
        return _selected;
    }

    /**
     * SelectionManager calls this to attempt to create a custom Selection
     * object when selecting a Fig. Override this only if you have specialist
     * requirements For a selected Fig. SelectionManger uses its own rules if
     * this method returns null.
     *
     * @return a specialist Selection class or null to delegate creation to the
     * Selection Manager.
     */
    public Selection makeSelection() {
        return null;
    }

    /**
     * Method to paint this Fig. By default it paints an "empty" space,
     * subclasses should override this method.
     */
    abstract public void paint(Graphics g);

    /**
     * Method to paint this Fig. By default it paints an "empty" space,
     * subclasses should override this method. TODO: Deprecate? Appears unused
     */
    abstract public void appendSvg(StringBuffer sb);

    protected void appendSvgStyle(StringBuffer sb) {
        sb.append(" style='fill:rgb(").append(getFillColor().getRed()).append(
                ",").append(getFillColor().getGreen()).append(",").append(
                        getFillColor().getBlue()).append(");").append("stroke-width:")
                .append(getLineWidth()).append(";").append("stroke:rgb(")
                .append(getLineColor().getRed()).append(",").append(
                        getLineColor().getGreen()).append(",").append(
                        getLineColor().getBlue()).append(");'");
    }

    /**
     * Return a point at the given distance along the path around this Fig. By
     * default, uses perimeter of the Fig's bounding box. Subclasses like
     * FigPoly have more specific logic.
     */
    final public Point pointAlongPerimeter(int dist) {
        Point res = new Point();
        stuffPointAlongPerimeter(dist, res);
        return res;
    }

    public void postLoad() {
    }

    public void postSave() {
    }

    public void preSave() {
    }

    /**
     * Draw the Fig on a PrintGraphics. This just calls paint.
     */
    final public void print(Graphics g) {
        paint(g);
    }

    /**
     * By default just pass it up to enclosing groups. Subclasses of FigNode may
     * want to override this method.
     */
    public void propertyChange(PropertyChangeEvent pce) {
        if (group != null) {
            group.propertyChange(pce);
        }
    }

    /**
     * Force recalculating of bounds and redraw of fig.
     */
    final public void redraw() {
        Rectangle rect = getBounds();
        setBounds(rect.x, rect.y, rect.width, rect.height);
        damage();
    }

    // TODO: Make sure this is extended in FigEdgePoly and FigPoly
    public void removePoint(int i) {
    }

    /**
     * Change the back-to-front ordering of a Fig in LayerDiagram. Should the
     * Fig have any say in it?
     *
     * @see LayerDiagram#reorder
     * @see CmdReorder
     */
    final public void reorder(int func, Layer lay) {
        lay.reorder(this, func);
    }

    /**
     * Reply a rectangle that arcs should not route through. Basically this is
     * the bounding box plus some margin around all egdes.
     */
    final public Rectangle routingRect() {
        return new Rectangle(_x - BORDER, _y - BORDER, _w + BORDER * 2, _h
                + BORDER * 2);
    }

    /**
     * Set the bounds of this Fig. Fires PropertyChangeEvent "bounds". This
     * method can be undone by performing UndoAction.
     */
    final public void setBounds(final int newX, final int newY,
            final int newWidth, final int newHeight) {

        if (group == null
                && (newX != _x || newY != _y || newWidth != _w || newHeight != _h)) {
            MutableGraphSupport.enableSaveAction();
            if (UndoManager.getInstance().isGenerateMementos()) {
                Memento memento = new Memento() {
                    int oldX = _x;

                    int oldY = _y;

                    int oldWidth = _w;

                    int oldHeight = _h;

                    public void undo() {
                        setBoundsImpl(oldX, oldY, oldWidth, oldHeight);
                        damage();
                    }

                    public void redo() {
                        setBoundsImpl(newX, newY, newWidth, newHeight);
                        damage();
                    }

                    public void dispose() {
                    }

                    public String toString() {
                        return (isStartChain() ? "*" : " ") + "BoundsMemento "
                                + oldX + ", " + oldY + ", " + oldWidth + ", "
                                + oldHeight;
                    }
                };
                UndoManager.getInstance().addMemento(memento);
            }
        }
        setBoundsImpl(newX, newY, newWidth, newHeight);
    }

    /**
     * Set the bounds of this Fig. Fires PropertyChangeEvent "bounds".
     */
    protected void setBoundsImpl(int x, int y, int w, int h) {
        Rectangle oldBounds = getBounds();
        _x = x;
        _y = y;
        _w = w;
        _h = h;
        firePropChange("bounds", oldBounds, getBounds());
    }

    /**
     * Change my bounding box to the given Rectangle. Just calls setBounds(x, y,
     * w, h).
     */
    public final void setBounds(Rectangle r) {
        setBounds(r.x, r.y, r.width, r.height);
    }

    public final void setCenter(Point p) {
        int newX = p.x - (_w / 2);
        int newY = p.y - (_h / 2);
        setLocation(newX, newY);
    }

    /**
     * USED BY PGML.tee
     */
    public void setEnclosingFig(Fig f) {
        if (f != null && f != getEnclosingFig() && layer != null) {
            layer.bringInFrontOf(this, f);
            damage();
        }
    }

    /**
     * Sets the enclosing FigGroup of this Fig. The enclosing group is always
     * notified of property changes, without need to add a listener.
     */
    final public void setGroup(Fig f) {
        group = f;
    }

    final public void setContext(String context) {
        _context = context;
    }

    // //////////////////////////////////////////////////////////////
    // accessors
    /**
     * Sets the Layer that this Fig belongs to. Fires PropertyChangeEvent
     * "layer".
     */
    public void setLayer(Layer lay) {
        firePropChange("layer", layer, lay);
        layer = lay;
    }

    /**
     * Sets the color that will be used if the Fig is filled. If col is null,
     * turns off filling. Fires PropertyChangeEvent "fillColor", or "filled".
     */
    public void setFillColor(Color col) {
        if (col == null) {
            if (_fillColor == null) {
                return;
            }
        } else {
            if (col.equals(_fillColor)) {
                return;
            }
        }

        if (col != null) {
            firePropChange("fillColor", _fillColor, col);
            _fillColor = col;
        } else {
            firePropChange("filled", _filled, false);
            _filled = false;
        }

        MutableGraphSupport.enableSaveAction();
    }

    /**
     * Sets a flag to either fill the Fig with its fillColor or not. Fires
     * PropertyChangeEvent "filled".
     */
    public void setFilled(boolean f) {
        firePropChange("filled", _filled, f);
        _filled = f;
    }

    /**
     * Sets the color to be used if the lineWidth is > 0. If col is null, sets
     * the lineWidth to 0. Fires PropertyChangeEvent "lineColor", or
     * "lineWidth".
     */
    public void setLineColor(Color col) {
        if (col == null) {
            if (_lineColor == null) {
                return;
            }
        } else {
            if (col.equals(_lineColor)) {
                return;
            }
        }
        if (col != null) {
            firePropChange("lineColor", _lineColor, col);
            _lineColor = col;
        } else {
            firePropChange("lineWidth", _lineWidth, 0);
            _lineWidth = 0;
        }
        MutableGraphSupport.enableSaveAction();
    }

    /**
     * Set the line width. Zero means lines are not drawn. One draws them one
     * pixel wide. Larger widths are in experimental support stadium
     * (hendrik@freiheit.com, 2003-02-05). Fires PropertyChangeEvent
     * "lineWidth".
     *
     * @param w The new lineWidth value
     */
    public void setLineWidth(int w) {
        int newLW = Math.max(0, w);
        firePropChange("lineWidth", _lineWidth, newLW);
        _lineWidth = newLW;
    }

    public Shape getShape() {
        return shape;
    }

    /**
     * Set line to be dashed or not *
     */
    public void setDashed(boolean now_dashed) {
        if (now_dashed) {
            _dashes = DASH_ARRAYS[1];
            _dashPeriod = DASH_PERIOD[1];
        } else {
            _dashes = null;
        }
    }

    public void setDashedString(String dashString) {
        setDashed(dashString.equalsIgnoreCase("solid"));
    }

    /**
     * Move the Fig to the given position. By default translates the Fig so that
     * the upper left corner of its bounding box is at the location. Fires
     * property "bounds".
     */
    final public void setLocation(int x, int y) {
        translate(x - _x, y - _y);
    }

    /**
     * Move the Fig to the given position.
     */
    public final void setLocation(Point p) {
        setLocation(p.x, p.y);
    }

    /**
     * Sets whether this Fig is locked or not. Most Cmds check to see if Figs
     * are locked and will not request modifications to locked Figs. Fires
     * PropertyChangeEvent "locked".
     */
    final public void setLocked(boolean b) {
        firePropChange("locked", locked, b);
        locked = b;
    }

    public void setNumPoints(int npoints) {
    }

    /**
     * Sets the owner object of this Fig. Fires PropertyChangeEvent "owner"
     */
    public void setOwner(Object own) {
        firePropChange("owner", owner, own);
        owner = own;
    }

    /**
     * Get and set the points along a path for Figs that are path-like.
     */
    public void setPoints(Point[] ps) {
    }

    public void setPoint(int i, int x, int y) {
    }

    final public void setPoint(int i, Point p) {
        setPoint(i, p.x, p.y);
    }

    public void setPoint(Handle h, int x, int y) {
        setPoint(h.index, x, y);
    }

    final public void setPoint(Handle h, Point p) {
        setPoint(h, p.x, p.y);
    }

    /**
     * Derived classes should implement this method
     *
     * in 0.11.1 this should not form part of the API
     */
    public void setPrivateData(String data) {
    }

    /**
     * Sets the size of the Fig. Fires property "bounds".
     */
    final public void setSize(int w, int h) {
        setBounds(_x, _y, w, h);
    }

    /**
     * Sets the size of the Fig. Fires property "bounds".
     */
    final public void setSize(Dimension d) {
        setSize(d.width, d.height);
    }

    /**
     * Set the width of the Fig.
     * <p>
     * Use this method only if the width property is the only bounds property of
     * the Fig you wish to amend. If you intend to also change the height use
     * setSize(int width, int height). If you also intend to amend the location
     * use setBounds(int x, int y, int width, int height). Calling a single
     * method will be far more efficient in changing bounds.
     *
     * @param width The new width.
     */
    final public void setWidth(int w) {
        setBounds(_x, _y, w, _h);
    }

    /**
     * Set the height of the Fig.
     * <p>
     * Use this method only if the height property is the only bounds property
     * of the Fig you wish to amend. If you intend to also change the width use
     * setSize(int width, int height). If you also intend to amend the location
     * use setBounds(int x, int y, int width, int height). Calling a single
     * method will be far more efficient in changing bounds.
     *
     * @param height The new height.
     */
    final public void setHeight(int h) {
        setBounds(_x, _y, _w, h);
    }

    /**
     * Set the X co-ordinate of the Fig.
     * <p>
     * Use this method only if the X property is the only bounds property of the
     * Fig you wish to amend. If you intend to also change the Y co ordinate use
     * setLocation(int x, int y). If you also intend to amend the width and/or
     * height use setBounds(int x, int y, int width, int height). Calling a
     * single method will be far more efficient in changing bounds.
     *
     * @param x The new x co-ordinate
     */
    final public void setX(int x) {
        setBounds(x, _y, _w, _h);
    }

    public void setXs(int[] xs) {
    }

    /**
     * Set the Y co-ordinate of the Fig.
     * <p>
     * Use this method only if the Y property is the only bounds property of the
     * Fig you wish to amend. If you intend to also change the X co ordinate use
     * setLocation(int x, int y). If you also intend to amend the width and/or
     * height use setBounds(int x, int y, int width, int height). Calling a
     * single method will be far more efficient in changing bounds.
     *
     * @param y The new y co-ordinate
     */
    final public void setY(int y) {
        setBounds(_x, y, _w, _h);
    }

    public void setYs(int[] ys) {
    }

    /**
     * Reshape the given rectangle to be my bounding box.
     *
     * use getBounds(Rectangle r)
     */
    final public void stuffBounds(Rectangle r) {
        r.setBounds(_x, _y, _w, _h);
    }

    public void stuffPointAlongPerimeter(int dist, Point res) {
        if (dist < _w && dist >= 0) {
            res.x = _x + (dist);
            res.y = _y;
        } else if (dist < _w + _h) {
            res.x = _x + _w;
            res.y = _y + (dist - _w);
        } else if (dist < _w + _h + _w) {
            res.x = _x + _w - (dist - _w - _h);
            res.y = _y + _h;
        } else if (dist < _w + _h + _w + _h) {
            res.x = _x;
            res.y = _y + (_w + _h + _w + _h - dist);
        } else {
            res.x = _x;
            res.y = _y;
        }
    }

    /**
     * Change the position of the object from where it is to where it is plus dx
     * and dy. Often called when an object is dragged. This could be very useful
     * if local-coordinate systems are used because deltas need less
     * transforming... maybe. Fires property "bounds". TODO: make final and
     * subclasses should extend translateImpl The method is undoable by
     * performing the UndoAction.
     *
     * @param dx the x offset
     * @param dy the y offset
     */
    public void translate(final int dx, final int dy) {
        if (dx == 0 && dy == 0) {
            return;
        }
        if (group == null) {

            class TranslateMemento extends Memento {

                int oldX;

                int oldY;

                int oldWidth;

                int oldHeight;

                TranslateMemento(int currentX, int currentY, int currentWidth,
                        int currentHeight) {
                    oldX = currentX;
                    oldY = currentY;
                    oldWidth = currentWidth;
                    oldHeight = currentHeight;
                }

                public void undo() {
                    setBoundsImpl(oldX, oldY, oldWidth, oldHeight);
                    damage();
                }

                public void redo() {
                    translateImpl(dx, dy);
                    damage();
                }

                public String toString() {
                    return (isStartChain() ? "*" : " ") + "TranslateMemento "
                            + oldX + ", " + oldY;
                }
            }
            if (UndoManager.getInstance().isGenerateMementos()) {
                UndoManager.getInstance().addMemento(
                        new TranslateMemento(_x, _y, _w, _h));
            }
        }
        MutableGraphSupport.enableSaveAction();
        translateImpl(dx, dy);
    }

    /**
     * Change the position of the object from where it is to where it is plus dx
     * and dy. Often called when an object is dragged. This could be very useful
     * if local-coordinate systems are used because deltas need less
     * transforming... maybe. Fires property "bounds".
     */
    protected void translateImpl(int dx, int dy) {
        Rectangle oldBounds = getBounds();
        _x += dx;
        _y += dy;
        firePropChange("bounds", oldBounds, getBounds());
    }

    /**
     * Reply true if the entire Fig is contained within the given Rectangle.
     * This can be used by ModeSelect to select Figs that are totally within the
     * selection rectangle.
     */
    final public boolean within(Rectangle r) {
        return r.contains(_x, _y) && r.contains(_x + _w, _y + _h);
    }

    /**
     * Returns true if the fig is visible
     */
    final public boolean isVisible() {
        return visible;
    }

    /**
     * Set the visible status of the fig
     */
    public void setVisible(boolean visible) {
        if (this.visible == visible) {
            return;
        }
        MutableGraphSupport.enableSaveAction();
        this.visible = visible;
    }

    /**
     * Set whether this Fig can be resized
     *
     * @param resizable true to make this Fig resizable
     */
    public void setResizable(boolean resizable) {
        this.resizable = resizable;
    }

    /**
     * Set whether this Fig can be moved
     *
     * @param movable true to make this Fig movable
     */
    public void setMovable(boolean movable) {
        this.movable = movable;
    }

    final public boolean isRemoveStarted() {
        return removeStarted;
    }

    protected Stroke getDefaultStroke(int lineWidth) {
        float[] dashes = null;
        if (getDashed()) {
            dashes = _dashes;
        }
        return getDefaultStroke(lineWidth, dashes, 0);
    }

    protected Stroke getDefaultStroke(float lineWidth, float[] dashes,
            float phase) {
        return new BasicStroke(lineWidth, BasicStroke.CAP_SQUARE,
                BasicStroke.JOIN_MITER, 10.0f, dashes, (float) phase);
    }

    /**
     * Get the default paint. Currently it's just the fill color.
     *
     * @return the Paint to use
     */
    protected Paint getDefaultPaint(Color fillColor, Color lineColor, int x,
            int y, int w, int h) {
        Paint p = fillColor; // solid fill
        // simple vertical gradient
        // p = new GradientPaint(new Point2D.Float(x, y), fillColor,
        // new Point2D.Float(x, y + h), fillColor.darker() /*lineColor*/);
        // diagonal stripey cyclic gradient
        // p = new GradientPaint(new Point2D.Float(x, y), lineColor,
        // new Point2D.Float(x + 20, y + 20), fillColor, true);
        // Texture paint
        // BufferedImage img = ;
        // p = new TexturePaint(img,
        // new Rectangle(0, 0, img.getWidth(), img.getHeight()));
        return p;
    }

} /* end class Fig */
