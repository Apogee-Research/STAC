package graph.filter;

import graph.*;
import java.awt.*;

/**
 * A class for storing color configuration information for a filter.
 *
 * @author Michael Shilman (michaels@eecs.berkeley.edu)
 * @version $Id$
 */
public class ColorProp extends FilterProp {

    public Color val = new Color(0);

    public ColorProp(String title) {
        build(title);
    }

    public ColorProp(String title, Color val) {
        this.val = val;
        build(title);
    }

    protected void build(String s) {
        setLayout(new FlowLayout());
        Label lbl = new Label(s);
        add(lbl);
    }

    public void update() {
    }

    public void reset() {
    }
}
