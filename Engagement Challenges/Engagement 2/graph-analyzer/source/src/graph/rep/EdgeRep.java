package graph.rep;

import graph.*;
import java.awt.*;
import java.util.*;

/**
 * Graphical representation of an edge connecting two nodes.<p>
 *
 * If you want to hide an edge when either its head or its tail is hidden, you
 * can do the following from your filter:
 * <p>
 *
 * <PRE>
 *		e.rep.show = (n1.rep.show && n2.rep.show);
 * </PRE>
 *
 * @see graph.Edge
 * @author Michael Shilman (michaels@eecs.berkeley.edu)
 * @version $Id$
 */
public class EdgeRep extends BaseRep {

    //TODO:
    //XXX arcs
    //XXX dashed
    /**
     * A normal straight line.
     */
    public static final int STRAIGHT = 0;

    /**
     * A routed edge.
     */
    public static final int ROUTED = 1;

    /**
     * A cubic bezier curve.
     */
    public static final int BEZIER = 2;

    /**
     * The type of edge rep this is.
     */
    public int type = STRAIGHT;

    /**
     * The X coordinates of the routed edge.
     */
    public double[] xroute = null;

    /**
     * The Y coordinates of the routed edge.
     */
    public double[] yroute = null;

    /**
     * The color to draw the edge in.
     */
    public Color color = Color.gray;//XXX

    /**
     * Whether or not to display the arrow on the edge.
     */
    public boolean showArrow = true;

    /**
     * The width of the edge.
     */
    public int width = 1;

    /**
     * Set the edge to be routed by the specified array of coordinates.
     *
     * @param xroute	The X coordinates of the route points.
     * @param yroute	The Y coordinates of the route points.
     */
    public void route(double xroute[], double yroute[]) {
        type = ROUTED;
        this.xroute = xroute;
        this.yroute = yroute;
    }

    /**
     * XXX this should never get called
     */
    public void paint(Graphics g, double x, double y) {
        //XXX this should never get called	
    }

    /**
     * Paint an edge between <i>(x1, y1)</i> and <i>(x2, y2)</i>.
     */
    public void paint(Graphics g, double x1, double y1, double x2, double y2) {
        if (show) {
            if (color != null) {
                g.setColor(color);
            }
            // XXX else get the color from the graph
            //		or the default color
            switch (type) {
                case ROUTED:
                    double xstart = x1;
                    double ystart = y1;
                    for (int i = 0; i < xroute.length; i++) {
                        drawThickLine(g, xstart, ystart, xroute[i], yroute[i], width);
                        xstart = xroute[i];
                        ystart = yroute[i];
                    }
                    drawThickLine(g, xstart, ystart, x2, y2, width);
                    break;
                default:
                    drawThickLine(g, x1, y1, x2, y2, width);
                    if (showArrow) {
                        g.fillPolygon(getArrow(x1, y1, x2, y2));
                    }
            }
        }
    }

    /**
     * Draw a thick line.
     */
    void drawThickLine(Graphics g, double x1, double y1, double x2, double y2, int width) {
        for (int i = 0; i < width; i++) {
            int startx = (int) (x1 - width / 2 + i);
            int starty = (int) (y1 - width / 2 + i);
            int finx = (int) (x2 - width / 2 + i);
            int finy = (int) (y2 - width / 2 + i);
            g.drawLine(startx, starty, finx, finy);
        }
    }

    /**
     * A scale factor for the arrows that are displayed.
     */
    protected static final double ARROW_SIZE = 8.0;

    /**
     * Generate a triangle polygon which is the arrow in the middle of the edge.
     *
     * @return	A triangular polygon in screen coordinates.
     */
    private Polygon getArrow(double x1, double y1, double x2, double y2) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        double cx = (x1 + x2) / 2;
        double cy = (y1 + y2) / 2;
        double len = Math.sqrt(dx * dx + dy * dy);
        double ndx = (ARROW_SIZE * dx) / len;
        double ndy = (ARROW_SIZE * dy) / len;
        Polygon poly = new Polygon();
        poly.addPoint((int) (cx + ndx), (int) (cy + ndy));//tip
        poly.addPoint((int) (cx - ndy / 2), (int) (cy + ndx / 2));
        poly.addPoint((int) (cx + ndy / 2), (int) (cy - ndx / 2));
        return poly;
    }
}
