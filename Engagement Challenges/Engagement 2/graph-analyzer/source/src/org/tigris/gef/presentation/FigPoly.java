// $Id: FigPoly.java 1233 2009-05-31 16:42:14Z tfmorris $
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

import org.tigris.gef.base.Geometry;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.GeneralPath;
import java.util.ArrayList;
import java.util.List;

/**
 * Primitive Fig to paint Polygons on a LayerDiagram. FigPolys contain a set of
 * points that define the polygon, a boolean to determine if the polygon should
 * be constrained to rectilinear (strict horizontal and vertical) segments, and
 * a number of handles that cannot be moved by user dragging. A FigPoly is not
 * closed unless the last point equals the first point. Thus, FigPolys can be
 * used to represent polylines such as FigEdgeRectilinear.
 *
 * @author jrobbins@ics.uci.edu
 */
public class FigPoly extends Fig {

    private static final float MITER_LIMIT = 10.0f;

    private static final long serialVersionUID = -4809619139509617929L;

    /**
     * The angle difference at which a point can be deleted.
     */
    private static final double FUDGEFACTOR = 7;

    // //////////////////////////////////////////////////////////////
    // instance variables
    /**
     * The total number of points.
     */
    protected int _npoints = 0;

    /**
     * The array of x coordinates.
     */
    protected int[] _xpoints = new int[4];

    /**
     * The array of y coordinates.
     */
    protected int[] _ypoints = new int[4];

    /**
     * Flag to control how the polygon is drawn
     */
    protected boolean _rectilinear = false;

    /**
     * Flag to indicate when the polygon is completed
     *
     * visibility will change - use getter/setter
     */
    public boolean _isComplete = false;

    /**
     * Flag to indicate when the polygon is used as a self-loop for a node
     */
    protected boolean _isSelfLoop = false;

    /**
     * The number of handles at each end of the polygon that cannot be dragged
     * by the user. -1 indicates that any point can be dragged. 0 indicates that
     * the endpoints cannot be dragged. 1 would indicate that the first 2 points
     * and the last 2 points cannot be dragged.
     */
    protected int _fixedHandles = -1;

    // //////////////////////////////////////////////////////////////
    // constructors
    /**
     * Construct a new FigPoly w/ the given line color.
     */
    public FigPoly(Color lineColor) {
        super();
        setLineColor(lineColor);
    }

    /**
     * Construct a new FigPoly w/ the given line color and fill color.
     */
    public FigPoly(Color lineColor, Color fillColor) {
        super();
        setLineColor(lineColor);
        setFillColor(fillColor);
    }

    /**
     * Construct a new FigPoly.
     */
    public FigPoly() {
        super();
    }

    /**
     * Construct a new FigPoly w/ the given point.
     */
    public FigPoly(int x, int y) {
        this();
        addPoint(x, y);
    }

    public Object clone() {
        FigPoly figClone = (FigPoly) super.clone();

        figClone._xpoints = (int[]) _xpoints.clone();
        figClone._ypoints = (int[]) _ypoints.clone();
        return figClone;
    }

    // //////////////////////////////////////////////////////////////
    // accessors
    /**
     * Get the current vector of points as a java.awt.Polygon.
     */
    public Polygon getPolygon() {
        return new Polygon(_xpoints, _ypoints, _npoints);
    }

    /**
     * Set the current vector of points.
     */
    public void setPolygon(Polygon p) {
        _npoints = p.npoints;
        _xpoints = new int[_npoints];
        _ypoints = new int[_npoints];
        System.arraycopy(p.xpoints, 0, _xpoints, 0, _npoints);
        System.arraycopy(p.ypoints, 0, _ypoints, 0, _npoints);
        calcBounds();
    }

    public void setSelfLoop(boolean self) {
        _isSelfLoop = self;
    }

    /**
     * Return the number of points in this polygon
     */
    public int getNumPoints() {
        return _npoints;
    }

    /**
     * Return true if the polygon should be constrained to rectilinear segments.
     */
    public boolean getRectilinear() {
        return _rectilinear;
    }

    /**
     * Set the rectilinear flag. Setting this flag to true will not change the
     * current shape of the polygon, instead future dragging by the user will
     * move near-by points to be rectilinear.
     */
    public void setRectilinear(boolean r) {
        _rectilinear = r;
    }

    /**
     * Reply the number of fixed handles. 0 indicates that the end points of the
     * polygon cannot be dragged by the user.
     */
    public int getFixedHandles() {
        return _fixedHandles;
    }

    /**
     * Set the number of points near each end of the polygon that cannot be
     * dragged by the user.
     */
    public void setFixedHandles(int n) {
        _fixedHandles = n;
    }

    // public boolean isSelfLoop() {
    // return _isSelfLoop;
    // }
    // //////////////////////////////////////////////////////////////
    // geomertric manipulations
    private static Handle _TempHandle = new Handle(0);

    /**
     * Set the end points of this polygon, regardless of the number of fixed
     * handles. This is used when nodes move.
     */
    public void setEndPoints(Point start, Point end) {

        while (_npoints < 2) {
            addPoint(start);
        }

        synchronized (_TempHandle) {
            _TempHandle.index = 0;
            moveVertex(_TempHandle, start.x, start.y, true);
            _TempHandle.index = _npoints - 1;
            moveVertex(_TempHandle, end.x, end.y, true);
        }
    }

    /**
     * Change the position of the object from were it is to were it is plus dx
     * or dy. Often called when an object is dragged. This could be very useful
     * if local-coordinate systems are used because deltas need less
     * transforming... maybe. Fires PropertyChange with "bounds".
     */
    protected void translateImpl(int dx, int dy) {
        Rectangle oldBounds = getBounds();

        for (int i = 0; i < _npoints; ++i) {
            _xpoints[i] += dx;
            _ypoints[i] += dy;
        }

        // dont call calcBounds because width and height are unchanged
        _x += dx;
        _y += dy;
        firePropChange("bounds", oldBounds, getBounds());
    }

    /**
     * Add a point to this polygon. Fires PropertyChange with "bounds".
     */
    public void addPoint(int x, int y) {
        growIfNeeded();
        _xpoints[_npoints] = x;
        _ypoints[_npoints] = y;
        _npoints++;
        Rectangle oldBounds = getBounds();

        calcBounds(); // a special case could be faster
        firePropChange("bounds", oldBounds, getBounds());
    }

    /**
     * Add a point to this polygon.
     */
    public final void addPoint(Point p) {
        addPoint(p.x, p.y);
    }

    /**
     * Reply true if the point at the given index can be moved. The index must
     * be valid, and the number of fixed handles must not * include this index,
     * unless ov is true to override the fixed * handles.
     */
    protected boolean canMoveVertex(int i, boolean ov) {
        return (i >= 0 && i < _npoints && (ov || i >= _fixedHandles
                && i < _npoints - _fixedHandles));
    }

    /**
     * Move the point indicated by the given Handle object to the given
     * location. Fixed handles cannot be moved, unless ov is set to true to
     * override the fixed handle constaint. Fires PropertyChange with "bounds".
     */
    public void moveVertex(Handle h, int x, int y, boolean ov) {
        int i = h.index;

        if (!_rectilinear) {
            _xpoints[i] = x;
            _ypoints[i] = y;
        } else {

            if (ov) {
                _xpoints[i] = x;
                _ypoints[i] = y;
            }

            if (i == _fixedHandles) {
                prependTwoPoints();
                h.index += 2;
                i += 2;
            }

            if (i == _npoints - _fixedHandles - 1) {
                appendTwoPoints();
            }

            if (i % 2 == 0) {

                if (canMoveVertex(i - 1, ov)) {
                    _xpoints[i - 1] = x;
                    _xpoints[i] = x;
                }

                if (canMoveVertex(i + 1, ov)) {
                    _ypoints[i + 1] = y;
                    _ypoints[i] = y;
                }
            } else {

                if (canMoveVertex(i - 1, ov)) {
                    _ypoints[i - 1] = y;
                    _ypoints[i] = y;
                }

                if (canMoveVertex(i + 1, ov)) {
                    _xpoints[i + 1] = x;
                    _xpoints[i] = x;
                }
            }
        }

        Rectangle oldBounds = getBounds();

        calcBounds();
        firePropChange("bounds", oldBounds, getBounds());
    }

    /**
     * Add two points to the front of the list of points. Needed to introduce
     * new handles at the front of the polygon when the user drags a point just
     * after a fixed handle.
     */
    protected void prependTwoPoints() {
        int[] tmp;

        tmp = new int[_npoints + 2];
        System.arraycopy(_xpoints, 0, tmp, 2, _npoints);
        _xpoints = tmp;
        tmp = new int[_npoints + 2];
        System.arraycopy(_ypoints, 0, tmp, 2, _npoints);
        _ypoints = tmp;
        _xpoints[0] = _xpoints[1] = _xpoints[2];
        _ypoints[0] = _ypoints[1] = _ypoints[2];
        _npoints += 2;
    }

    /**
     * Add two points at the end of the polygon. Needed if the user drags a
     * point just before a fixed handle.
     */
    protected void appendTwoPoints() {
        int[] tmp;

        tmp = new int[_npoints + 2];
        System.arraycopy(_xpoints, 0, tmp, 0, _npoints);
        _xpoints = tmp;
        tmp = new int[_npoints + 2];
        System.arraycopy(_ypoints, 0, tmp, 0, _npoints);
        _ypoints = tmp;
        _xpoints[_npoints + 1] = _xpoints[_npoints] = _xpoints[_npoints - 1];
        _ypoints[_npoints + 1] = _ypoints[_npoints] = _ypoints[_npoints - 1];
        _npoints += 2;
    }

    /**
     * Removes the point at index i. Needs-More-Work: this can mess up
     * rectilinear polygons so that they have one non-rectilinear segment. Fires
     * PropertyChange with "bounds".
     */
    public void removePoint(int i) {

        // needs-more-work: this assertion has been violated, track it down
        // TODO: It is violated by selecting a point of a triangle FigPoly
        // and pressing backspace.
        if (i < 0 || i >= _npoints) {
            throw new IllegalArgumentException(
                    "Point not found in LayerDiagram");
        }

        if (_npoints < 3) {
            return;
        }

        int[] tmp = new int[_npoints];

        if (_rectilinear && i != 0 && i != _npoints - 1) {

            if (i % 2 == 0) {
                _xpoints[i] = _xpoints[i + 1];
                _ypoints[i] = _ypoints[i - 1];
            } else {
                _xpoints[i] = _xpoints[i - 1];
                _ypoints[i] = _ypoints[i + 1];
            }
        } else {
            System.arraycopy(_xpoints, i + 1, tmp, 0, _npoints - i - 1);
            System.arraycopy(tmp, 0, _xpoints, i, _npoints - i - 1);
            System.arraycopy(_ypoints, i + 1, tmp, 0, _npoints - i - 1);
            System.arraycopy(tmp, 0, _ypoints, i, _npoints - i - 1);
            --_npoints;
        }

        Rectangle oldBounds = getBounds();

        calcBounds();
        firePropChange("bounds", oldBounds, getBounds());
    }

    /**
     * Inserts a point at index i. Needs-More-Work: this can mess up rectilinear
     * polygons so that they have one non-rectilinear segment. Fires
     * PropertyChange with "bounds".
     */
    public void insertPoint(int i, int x, int y) {
        growIfNeeded();
        int[] tmp = new int[_npoints];

        System.arraycopy(_xpoints, i + 1, tmp, 0, _npoints - i - 1);
        _xpoints[i + 1] = x;
        System.arraycopy(tmp, 0, _xpoints, i + 2, _npoints - i - 1);
        System.arraycopy(_ypoints, i + 1, tmp, 0, _npoints - i - 1);
        _ypoints[i + 1] = y;
        System.arraycopy(tmp, 0, _ypoints, i + 2, _npoints - i - 1);
        ++_npoints;
        calcBounds(); // needed? //?
    }

    /**
     * Increase the memory used to store polygon points, if needed.
     */
    protected void growIfNeeded() {

        if (_npoints >= _xpoints.length) {
            int[] tmp;

            tmp = new int[_npoints * 2];
            System.arraycopy(_xpoints, 0, tmp, 0, _npoints);
            _xpoints = tmp;
            tmp = new int[_npoints * 2];
            System.arraycopy(_ypoints, 0, tmp, 0, _npoints);
            _ypoints = tmp;
        }
    }

    /**
     * Find the required line break and return it's corordinates as a Point.
     * This results in a new object instantiation on every call, if multiple
     * calls are required consider getXs() and getYs() for performance.
     *
     * @param i the position of the point to return
     * @return the point at position i
     */
    public Point getPoint(int i) {
        return new Point(_xpoints[i], _ypoints[i]);
    }

    /**
     * USED BY PGML.tee
     */
    public List<Point> getPointsList() {
        List<Point> res = new ArrayList<Point>(_npoints);

        for (int i = 0; i < _npoints; i++) {
            res.add(new Point(_xpoints[i], _ypoints[i]));
        }

        return res;
    }

    /**
     * USED BY PGML.tee
     */
    public Point[] getPoints() {
        Point[] points = new Point[getPointsList().size()];

        for (int i = 0; i < _npoints; i++) {
            points[i] = getPoint(i);
        }

        return points;
    }

    /**
     * USED BY PGML.tee
     */
    public Point getFirstPoint() {
        return getPoint(0);
    }

    public Point getLastPoint() {
        return getPoint(_npoints - 1);
    }

    /**
     * When the user drags the handles, move individual points
     */
    public void setPoint(Handle h, int mX, int mY) {
        moveVertex(h, mX, mY, false);
    }

    public void setPoints(Point[] points) {
        _npoints = points.length;
        _xpoints = new int[_npoints];
        _ypoints = new int[_npoints];
        for (int i = 0; i < _npoints; ++i) {
            _xpoints[i] = points[i].x;
            _ypoints[i] = points[i].y;
        }
        calcBounds();
    }

    public void cleanUp() {
        for (int handleNumber = 1; handleNumber < _npoints - 1; handleNumber++) {
            Point start = new Point(_xpoints[handleNumber - 1],
                    _ypoints[handleNumber - 1]);
            Point middle = new Point(_xpoints[handleNumber],
                    _ypoints[handleNumber]);
            Point end = new Point(_xpoints[handleNumber + 1],
                    _ypoints[handleNumber + 1]);

            // remove points that are on top of each other
            if (start.equals(middle) || end.equals(middle)) {
                removePoint(handleNumber);
                break;
            }

            double startToMiddleAngle = Geometry.segmentAngle(start, middle);
            double middleToEndAngle = Geometry.segmentAngle(middle, end);
            double difference = Geometry.diffAngle(startToMiddleAngle,
                    middleToEndAngle);

            if (difference < FUDGEFACTOR) {
                removePoint(handleNumber);
            }
        }

        calcBounds();
    }

    /**
     * Returns the point that other connected Figs should attach to. By default,
     * returns the point closest to anotherPt.
     */
    public Point getClosestPoint(Point anotherPt) {
        return Geometry.ptClosestTo(_xpoints, _ypoints, _npoints, anotherPt);
    }

    public List getGravityPoints() {
        return getPointsList();
    }

    // //////////////////////////////////////////////////////////////
    // painting methods
    /**
     * Paint the FigPoly on the given Graphics
     */
    public void paint(Graphics g) {

        if (_filled && _fillColor != null) {
            if (g instanceof Graphics2D) {
                Graphics2D g2 = (Graphics2D) g;
                // TODO: Move this out of paint for performance
                Shape poly = new Polygon(getXs(), getYs(), getNumPoints());
                Rectangle bb = poly.getBounds();
                Paint oldPaint = g2.getPaint();
                g2.setPaint(getDefaultPaint(_fillColor, _lineColor, bb.x, bb.y,
                        bb.width, bb.height));
                g2.fill(poly);
                g2.setPaint(oldPaint);
            } else {
                g.setColor(_fillColor);
                g.fillPolygon(_xpoints, _ypoints, _npoints);
            }
        }

        if (getLineWidth() > 0 && _lineColor != null) {
            g.setColor(_lineColor);
            if (g instanceof Graphics2D) {
                float[] dashes = null;
                if (getDashed()) {
                    dashes = _dashes;
                }
                drawPolyLine((Graphics2D) g, (float) getLineWidth(), _npoints,
                        _xpoints, _ypoints, dashes, 0f);
            } else {
                if (getDashed()) {
                    drawDashedPerimeter(g, getLineWidth(), _npoints, _xpoints,
                            _ypoints, _dashes, _dashPeriod);
                } else {
                    g.drawPolyline(_xpoints, _ypoints, _npoints);
                }
            }
        }
    }

    private void drawDashedPerimeter(Graphics g, int lineWidth, int pointCount,
            int xPoints[], int yPoints[], float dashes[], int dashPeriod) {

        if (g instanceof Graphics2D) {
            drawPolyLine((Graphics2D) g, (float) lineWidth, pointCount,
                    xPoints, yPoints, dashes, 0f);
            return;
        }
        int phase = 0;
        for (int i = 1; i < pointCount; i++) {
            phase = drawDashedLine(g, lineWidth, xPoints[i - 1],
                    yPoints[i - 1], xPoints[i], yPoints[i], phase, dashes,
                    dashPeriod);
        }
    }

    /**
     * Draw a polyline
     *
     * @param g2 graphics context
     * @param width line width
     * @param pointCount number of points in line
     * @param xPoints array of X coordinates
     * @param yPoints array of Y coordinates
     * @param dashes array of dash lengths
     */
    private void drawPolyLine(Graphics2D g2, float width, int pointCount,
            int[] xPoints,
            int[] yPoints, float[] dashes, float dash_phase) {

        GeneralPath gp = new GeneralPath();
        gp.reset();
        gp.moveTo(xPoints[0], yPoints[0]);
        for (int i = 1; i < pointCount; i++) {
            gp.lineTo(xPoints[i], yPoints[i]);
        }

        Stroke originalStroke = g2.getStroke();
        g2.setStroke(getDefaultStroke(width, dashes, dash_phase));
        g2.draw(gp);
        g2.setStroke(originalStroke);
    }

    public void appendSvg(StringBuffer sb) {
        sb.append("<path id='").append(getId()).append("' class='")
                .append(getClass().getName())
                .append("' style='fill:none; stroke-width:")
                .append(getLineWidth())
                .append("; stroke:rgb(").append(getLineColor().getRed())
                .append(",").append(getLineColor().getGreen()).append(',')
                .append(getLineColor().getBlue()).append(" ;' d='");
        for (int i = 0; i < getPoints().length; ++i) {
            if (i == 0) {
                sb.append("M ");
            } else {
                sb.append("L ");
            }
            sb.append(getPoint(i).x).append(',').append(getPoint(i).y);
        }
        sb.append(" ' />");
    }

    // //////////////////////////////////////////////////////////////
    // selection methods
    /**
     * Reply the index of the vertex that the given mouse point is near.
     */
    protected int findHandle(int x, int y) {
        int handleSize = 6;
        int[] xs = _xpoints;
        int[] ys = _ypoints;

        for (int i = 0; i < _npoints; ++i) {
            if (x >= xs[i] - handleSize / 2 && y >= ys[i] - handleSize / 2
                    && x <= xs[i] + handleSize / 2
                    && y <= ys[i] + handleSize / 2) {
                return i;
            }
        }

        return -1;
    }

    /**
     * Reply true iff the given point is inside this FigPoly.
     */
    // ? does polygon.contains really work? need to call Geometry?
    public boolean contains(int x, int y) {
        Polygon p = getPolygon();

        return p.contains(x, y);
    }

    /**
     * Returns the array of X coordinates of points
     */
    public int[] getXs() {
        return _xpoints;
    }

    /**
     * Returns the array of Y coordinates of points
     */
    public int[] getYs() {
        return _ypoints;
    }

    /**
     * Sets the FigPoly's bounding box to the given coordinates. Scales all
     * points into the new bounding box. Fires PropertyChange with "bounds".
     */
    protected void setBoundsImpl(int x, int y, int w, int h) {
        Rectangle oldBounds = getBounds();

        if (w > 0 && h > 0) {

            for (int i = 0; i < _npoints; ++i) {
                _xpoints[i] = x + ((_xpoints[i] - _x) * w) / _w;
                _ypoints[i] = y + ((_ypoints[i] - _y) * h) / _h;
            }

            _x = x;
            _y = y;
            _w = w;
            _h = h;
            firePropChange("bounds", oldBounds, getBounds());
        }
    }

    /**
     * Returns the length of the perimeter of the polygon, which is the sum of
     * all the lengths of its segments.
     */
    public int getPerimeterLength() {

        // needs-more-work: should I cache this value?
        int len = 0;
        int dx;
        int dy;

        for (int i = 0; i < _npoints - 1; ++i) {
            dx = _xpoints[i + 1] - _xpoints[i];
            dy = _ypoints[i + 1] - _ypoints[i];
            len += (int) Math.sqrt(dx * dx + dy * dy);
        }

        return len;
    }

    /**
     * Returns a point along the perimeter at distance dist from the start of
     * the polygon.
     */
    public void stuffPointAlongPerimeter(int dist, Point res) {

        for (int i = 0; i < _npoints - 1; ++i) {
            int dx = _xpoints[i + 1] - _xpoints[i];
            int dy = _ypoints[i + 1] - _ypoints[i];
            int segLen = (int) Math.sqrt(dx * dx + dy * dy);

            if (dist < segLen) {

                if (segLen != 0) {
                    res.x = _xpoints[i] + (dx * dist) / segLen;
                    res.y = _ypoints[i] + (dy * dist) / segLen;
                    return;
                } else {
                    res.x = _xpoints[i];
                    res.y = _ypoints[i];
                    return;
                }
            } else {
                dist -= segLen;
            }
        }

        // what if there are 0 points?
        res.x = _xpoints[0];
        res.y = _ypoints[0];
    }

    /**
     * FigPolys are resizeable and reshapable, but not rotatable (yet).
     */
    public boolean isResizable() {
        return true;
    }

    public boolean isReshapable() {
        return true;
    }

    public boolean isRotatable() {
        return false;
    }

    /**
     * Tests, if the perimeter of the polygon is closed, i.e. if the last point
     * of the polygon is equal to the first point.
     *
     * @return True, if the perimeter is closed, otherwise false.
     */
    public boolean isPerimeterClosed() {

        if (_xpoints.length < 1 || _npoints < 1) {
            return false;
        }

        if (_npoints <= _xpoints.length) {
            return (_xpoints[0] == _xpoints[_npoints - 1])
                    && (_ypoints[0] == _ypoints[_npoints - 1]);
        } else {
            return (_xpoints[0] == _xpoints[_xpoints.length - 1])
                    && (_ypoints[0] == _ypoints[_ypoints.length - 1]);
        }
    }

    public boolean isComplete() {
        return _isComplete;
    }

    public void setComplete(boolean complete) {
        this._isComplete = complete;
    }

    // //////////////////////////////////////////////////////////////
    // internal utility functions
    /**
     * Return the number of corners of the given rectangle that are contained
     * within this polygon.
     *
     * @see Fig#hit
     */
    protected int countCornersContained(int x, int y, int w, int h) {

        if (!isPerimeterClosed()) {
            return 0;
        }

        Polygon p = getPolygon();
        int cornersHit = 0;

        if (p.contains(x, y)) {
            cornersHit++;
        }

        if (p.contains(x + w, y)) {
            cornersHit++;
        }

        if (p.contains(x, y + h)) {
            cornersHit++;
        }

        if (p.contains(x + w, y + h)) {
            cornersHit++;
        }

        return cornersHit;
    }

    /**
     * Tests, if the given rectangle hits this polygon.
     *
     * @param rect The rectangle to be tested.
     * @return True, if the rectangle hit this polygon, otherwise false.
     */
    public boolean hit(Rectangle rect) {

        if (super.hit(rect)) {
            return true;
        }

        return intersectsPerimeter(rect);
    }

    /**
     * Tests, if the given rectangle intersects with the perimeter of this
     * polygon.
     *
     * @param rect The rectangle to be tested.
     * @return True, if the rectangle intersects the perimeter, otherwise false.
     */
    public boolean intersectsPerimeter(Rectangle rect) {

        for (int i = 1; i < _npoints; i++) {

            if (rect.intersectsLine(_xpoints[i - 1], _ypoints[i - 1],
                    _xpoints[i], _ypoints[i])) {
                return true;
            }
        }

        return false;
    }

    /**
     * Update the bounding box.
     */
    public void calcBounds() {

        // needs-more-work: could be faster, dont alloc polygon
        Rectangle polyBounds = getPolygon().getBounds();

        _x = polyBounds.x;
        _y = polyBounds.y;
        _w = polyBounds.width;
        _h = polyBounds.height;
    }
} /* end class FigPoly */
