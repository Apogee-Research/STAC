package graph.filter;

import graph.*;
import java.awt.*;

/**
 * A class for storing integer number configuration information for a filter.
 *
 * @author Michael Shilman (michaels@eecs.berkeley.edu)
 * @version $Id$
 */
public class IntProp extends FilterProp {

    public int val = 0;
    protected TextField m_field = null;

    public IntProp(String s) {
        build(s);
    }

    public IntProp(String s, int val) {
        this.val = val;
        build(s);
    }

    protected void build(String s) {
        setLayout(new FlowLayout());
        Label lbl = new Label(s);
        add(lbl);
        m_field = new TextField();
        reset();
    }

    public void reset() {
        m_field.setText(String.valueOf(val));
    }

    public void update() {
        Integer i = new Integer(m_field.getText());
        val = i.intValue();
    }
}
