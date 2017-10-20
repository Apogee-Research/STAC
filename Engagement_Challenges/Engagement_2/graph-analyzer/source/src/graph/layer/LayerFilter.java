package graph.layer;

import graph.*;
import graph.filter.*;
import java.awt.*;

/**
 * A class for displaying layers
 *
 * XXX build gui
 *
 * @author Michael Shilman (michaels@eecs.berkeley.edu)
 * @version $Id$
 */
public class LayerFilter implements Filter {

    public static int s_layerIndex = AttributeManager.NO_INDEX;

    public LayerFilter() {
        if (s_layerIndex == AttributeManager.NO_INDEX) {
            s_layerIndex = AttributeManager.getIndex("Layer");
        }
    }

    /**
     * Apply this filter to the node, modifying its "show" display field.
     */
    public void apply(Node n) {
        LayerInfo info = (LayerInfo) n.getAttr(s_layerIndex);

        boolean show = true;//LayerMgr.getShow(info.layer);

        n.rep.show = show;
        for (int i = 0; i < n.in.size(); i++) {
            ((Edge) n.in.elementAt(i)).rep.show = show;
        }
        for (int i = 0; i < n.out.size(); i++) {
            ((Edge) n.out.elementAt(i)).rep.show = show;
        }
    }

    public String getName() {
        return "Layer";
    }

    public Component buildThumbnail() {
        return new Button("Layers");
    }

    /**
     * Build a dialog by which the user can easily configure parameters to the
     * Filter.
     */
    public Frame buildGUI() {
        return new Frame("Layer Filter");
    }
}
