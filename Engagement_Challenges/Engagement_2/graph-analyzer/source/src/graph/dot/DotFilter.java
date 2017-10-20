package graph.dot;

import graph.*;
import graph.filter.*;
import java.util.Hashtable;
import java.util.Enumeration;
import java.awt.*;
import java.lang.reflect.Field;

/**
 * Take DOT attributes and map them onto the display.
 *
 * @see	DotParser
 * @author Michael Shilman (michaels@eecs.berkeley.edu)
 * @version $Id$
 */
public class DotFilter implements Filter {

    public static int s_dotIndex = AttributeManager.NO_INDEX;

    DotFilter() {
        if (s_dotIndex == AttributeManager.NO_INDEX) {
            s_dotIndex = AttributeManager.getIndex("Dot");
        }
    }

    public String getName() {
        return "Dot";
    }

    public void apply(Node n) {
        DotInfo info = (DotInfo) n.getAttr(s_dotIndex);
        if (info.firstTime && (info != null) && (info.props != null)) {
            Enumeration keys = info.props.keys();
            Enumeration vals = info.props.elements();
            while (keys.hasMoreElements() && vals.hasMoreElements()) {
                String key = (String) keys.nextElement();
                String val = (String) vals.nextElement();
                if (key.equals("pos")) {
                    filterPos(info, val);
                } else if (key.equals("width") || key.equals("w")) {
                    filterWidth(info, val);
                } else if (key.equals("height") || key.equals("h")) {
                    filterHeight(info, val);
                } else if (key.equals("x")) {
                    filterX(info, val);
                } else if (key.equals("y")) {
                    filterY(info, val);
                } else if (key.equals("color")) {
                    //XXX color info
                    try {
                        Field field = Class.forName("java.awt.Color").getField(val);
                        info.color = (Color) field.get(null);
                    } catch (Exception e) {
                        info.color = Color.BLACK; // Not defined
                    }
                }
            }
            info.firstTime = false;
        }

        n.x = info.x;
        n.y = info.y;
        n.w = info.w;
        n.h = info.h;
        n.color = info.color;

//		org.graph.commons.logging.LogFactory.getLog(null).info(">>>> FILTERING: " + n.name + ", " + n.x + ", " + n.y + ", " + n.w + ", " + n.h );
    }

    static void filterWidth(DotInfo info, String val) {
        Float w = new Float(val);
        info.w = (int) (w.floatValue());
    }

    static void filterHeight(DotInfo info, String val) {
        Float h = new Float(val);
        info.h = (int) (h.floatValue());
    }

    static void filterX(DotInfo info, String val) {
        Double x = new Double(val);
        info.x = x.intValue();
    }

    static void filterY(DotInfo info, String val) {
        org.graph.commons.logging.LogFactory.getLog(null).info("Filter y pos to: " + val);

        Double y = new Double(val);
        info.y = y.intValue();
    }

    static void filterPos(DotInfo info, String val) {
        int comma = val.indexOf(',');
        Integer x = new Integer(val.substring(0, comma));
        Integer y = new Integer(val.substring(comma + 1));
        org.graph.commons.logging.LogFactory.getLog(null).info("Filter pos to: " + x + ", " + y);
        info.x = x.intValue();
        info.y = y.intValue();
    }

    public Frame buildGUI() {
        return new Frame("Dot Filter");
    }

    public Component buildThumbnail() {
        return null;
    }
}
