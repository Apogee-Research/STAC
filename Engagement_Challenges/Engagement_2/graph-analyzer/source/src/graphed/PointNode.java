package graphed;

import java.awt.*;
import java.awt.geom.*;

/**
 * An inivisible node that is used in the toolbar to draw an edge.
 */
public class PointNode implements Node {

    Object refObj;

    /**
     * Constructs a point node with coordinates (0, 0)
     */
    public PointNode() {
        point = new Point2D.Double();
    }

    public void draw(Graphics2D g2) {
    }

    public void translate(double dx, double dy) {
        point.setLocation(point.getX() + dx,
                point.getY() + dy);
    }

    public boolean contains(Point2D p) {
        return false;
    }

    public Rectangle2D getBounds() {
        return new Rectangle2D.Double(point.getX(),
                point.getY(), 0, 0);
    }

    public Point2D getConnectionPoint(Point2D other) {
        return point;
    }

    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException exception) {
            return null;
        }
    }

    private Point2D point;

    @Override
    public void setReferenceObject(Object ref) {

        refObj = ref;

    }

    @Override
    public Object getReferenceObject() {
        return refObj;
    }
}
