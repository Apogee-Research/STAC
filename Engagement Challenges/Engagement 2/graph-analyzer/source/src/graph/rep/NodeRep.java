package graph.rep;

import graph.*;
import java.awt.*;

/**
 * A data structure for storing the graphical representation of a node. Nodes
 * can have be presented as one of several shapes, or as an image of some sort.
 *
 * @see graph.Node
 * @see graph.Graph
 * @author Michael Shilman (michaels@eecs.berkeley.edu)
 * @version $Id$
 */
public class NodeRep extends BaseRep implements Cloneable {

    /**
     * Draw this node in the shape of a rectangle.
     */
    public static final int RECTANGLE = 0;

    /**
     * Draw this node in the shape of a 3d rectangle.
     */
    public static final int THREED_RECT = 1;

    /**
     * Draw this node in the shape of a rounded rectangle.
     */
    public static final int ROUND_RECT = 2;

    /**
     * Draw this node in the shape of an oval.
     */
    public static final int OVAL = 3;

    /**
     * Draw this node in as an image.
     */
    public static final int IMAGE = 4;

//XXX
//	public static final int TRIANGLE = 0;
//	public static final int IMAGE = 0;
//  public Image i = null;
    /**
     * The drawing type of the node. Defaults to rectangle.
     */
    public int type = RECTANGLE;

    /**
     * The drawing type of the node when it is selected.
     */
    public int selType = RECTANGLE;

    /**
     * The radius of the corner in a rounded rectangle representation.
     */
    public int corner = 5;

    /**
     * The fill color for the standard shape representation.
     */
    public Color fill = Color.orange;//XXX

    /**
     * The border color for the standard shape representation.
     */
    public Color border = Color.blue;//XXX

    /**
     * The image object in the image representation.
     */
    public Image image = null;

    /**
     * The fill color for the selected shape representation.
     */
    public Color selFill = Color.red;

    /**
     * The border color for the selected shape representation.
     */
    public Color selBorder = Color.blue;

    /**
     * The image object in the image representation when the node is selected.
     */
    public Image selImage = null;

    /**
     * Paint this node at the specified position.
     */
    public void paint(Graphics g, double x, double y) {
        switch (type) {
            case IMAGE:
                if (image != null) {
                    g.drawImage(image, (int) x, (int) y, null);
                }
                break;
            default:
                paint(g, x, y, Node.DEFAULT_WIDTH, Node.DEFAULT_HEIGHT);
        }
    }

    /**
     * Make a copy of this object.
     */
    public Object clone() {
        NodeRep rep = new NodeRep();
        rep.type = type;
        rep.selType = selType;
        rep.corner = corner;
        rep.fill = fill;
        rep.border = border;
        rep.image = image;
        rep.selImage = selImage;
        rep.selFill = selFill;
        rep.selBorder = selBorder;
        rep.selected = selected;
        return rep;
    }

    /**
     * Paint this object in the specified rectangle.
     */
    public void paint(Graphics g, double x1, double y1, double w1, double h1) {
        int x = (int) x1;
        int y = (int) y1;
        int w = (int) w1;
        int h = (int) h1;
        int t = ((selected) ? selType : type);
        Color fc = ((selected) ? selFill : fill);
        Color bc = ((selected) ? selBorder : border);
        Image img = ((selected) ? selImage : image);

        if (show) {
            if (fc != null) {
                g.setColor(fc);
                switch (t) {
                    case THREED_RECT:
                        g.fill3DRect(x, y, w, h, true);
                    case ROUND_RECT:
                        g.fillRoundRect(x, y, w, h, corner, corner);
                        break;
                    case OVAL:
                        g.fillOval(x, y, w, h);
                        break;
                    case IMAGE:
                        if (img != null) {
                            //						org.graph.commons.logging.LogFactory.getLog(null).info("Drawing image: " + img.getWidth(null) + " x " + img.getHeight(null));
                            g.drawImage(img, (int) x, (int) y, (int) w, (int) h, null);
                        }
                        break;
                    case RECTANGLE:
                    default:
                        g.fillRect(x, y, w, h);
                        break;
                }
            }

            if (bc != null) {
                g.setColor(bc);
                switch (t) {
                    case THREED_RECT:
                        g.draw3DRect(x, y, w, h, true);
                    case ROUND_RECT:
                        g.drawRoundRect(x, y, w, h, corner, corner);
                        break;
                    case OVAL:
                        g.drawOval(x, y, w, h);
                        break;
                    case IMAGE:
                        break;
                    case RECTANGLE:
                    default:
                        g.drawRect(x, y, w, h);
                        break;
                }
            }
        }
    }
}
