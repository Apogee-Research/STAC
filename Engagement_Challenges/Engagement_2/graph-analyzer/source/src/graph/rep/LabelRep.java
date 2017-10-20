package graph.rep;

import graph.*;
import java.awt.*;

/**
 * A data structure for storing the graphical representation of a node's label.
 *
 * @author Michael Shilman (michaels@eecs.berkeley.edu)
 * @version $Id$
 */
public class LabelRep extends BaseRep {

    /**
     * The color in which to draw the label.
     */
    public Color color = Color.black;//XXX

    /**
     * The font in which to draw the label.
     */
    public Font font = new Font("Helvetica", Font.BOLD, 10);

    /**
     * The string which is being displayed.
     */
    public String label = null;

    public void paint(Graphics g, double x, double y) {
        if (show) {
            //XXX get default values
            g.setFont(font);
            if (color != null) {
                g.setColor(color);
            }
            if (label != null) {
                g.drawString(label, (int) x, (int) y);
            }
        }
    }

    public void paint(Graphics g, double x, double y, double w, double h) {
        //XXX handle width and height
        paint(g, x, y);
    }
}
