package graphed;

import java.awt.*;
import java.awt.geom.*;

/**
 * An edge that is shaped like a straight line.
 */
public class LineEdge extends AbstractEdge {

    public void draw(Graphics2D g2) {
        g2.draw(getConnectionPoints());
    }

    public boolean contains(Point2D aPoint) {
        final double MAX_DIST = 2;
        return getConnectionPoints().ptSegDist(aPoint)
                < MAX_DIST;
    }
}
