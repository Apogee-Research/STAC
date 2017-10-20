package graph.filter;

import graph.*;
import java.awt.*;

/**
 * A class for storing boolean configuration information for a filter.
 *
 * @author Michael Shilman (michaels@eecs.berkeley.edu)
 * @version $Id$
 */
public class BooleanProp extends FilterProp {

    public boolean val = false;
    protected Checkbox m_box = null;

    public BooleanProp(String s) {
        build(s);
    }

    public BooleanProp(String s, boolean val) {
        this.val = val;
        build(s);
    }

    protected void build(String s) {
        setLayout(new FlowLayout());
        m_box = new Checkbox(s);
        add(m_box);
        reset();
    }

    public void reset() {
        m_box.setState(val);
    }

    public void update() {
        val = m_box.getState();
    }
}
