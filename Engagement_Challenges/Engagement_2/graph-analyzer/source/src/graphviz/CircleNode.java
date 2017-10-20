/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package graphviz;

import java.awt.*;
import java.awt.geom.*;

/**
 * A circular node that is filled with a color.
 */
public class CircleNode implements Node {

    /**
     * Construct a circle node with a given size and color.
     *
     * @param size the size
     * @param aColor the fill color
     */
    public CircleNode(Color aColor) {
        size = DEFAULT_SIZE;
        x = 0;
        y = 0;
        color = aColor;
    }

    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException exception) {
            return null;
        }
    }

    public void draw(Graphics2D g2) {
        Ellipse2D circle = new Ellipse2D.Double(
                x, y, size, size);
        Color oldColor = g2.getColor();
        g2.setColor(color);
        g2.fill(circle);
        g2.setColor(oldColor);
        g2.draw(circle);
    }

    public void translate(double dx, double dy) {
        x += dx;
        y += dy;
    }

    public boolean contains(Point2D p) {
        Ellipse2D circle = new Ellipse2D.Double(
                x, y, size, size);
        return circle.contains(p);
    }

    public Rectangle2D getBounds() {
        return new Rectangle2D.Double(
                x, y, size, size);
    }

    public Rectangle2D getInnerBounds(int newscale) {
        this.size = 100;
        double root = Math.sqrt((this.size * this.size) / 2);
        org.graph.commons.logging.LogFactory.getLog(null).info("root" + root);

        int ix = (int) (this.size - root) / 2;
        int iy = (int) (this.size - root) / 2;

        int boundingx = (int) ((ix / newscale) * scale);
        int boundingy = (int) ((iy / newscale) * scale);

        return new Rectangle2D.Double(
                boundingx, boundingy, size, size);
    }

    public Point2D getConnectionPoint(Point2D other) {
        double centerX = x + size / 2;
        double centerY = y + size / 2;
        double dx = other.getX() - centerX;
        double dy = other.getY() - centerY;
        double distance = Math.sqrt(dx * dx + dy * dy);
        if (distance == 0) {
            return other;
        } else {
            return new Point2D.Double(
                    centerX + dx * (size / 2) / distance,
                    centerY + dy * (size / 2) / distance);
        }
    }

    public int x;
    public int y;
    public int size;
    private Color color;
    private static final int DEFAULT_SIZE = 20;

    public double scale = 1;
}
