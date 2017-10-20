package graph.filter;

import graph.*;
import java.awt.*;

/**
 * A class for storing string configuration information for a filter.
 *
 * @author Michael Shilman (michaels@eecs.berkeley.edu)
 * @version $Id$
 */
public class StringProp extends FilterProp {

    public String val = new String("");
    protected TextField m_field = null;

    public StringProp(String title, String val) {
        this.val = val;
        build(title);
    }

    public StringProp(String title) {
        build(title);
    }

    protected void build(String s) {
        setLayout(new FlowLayout());
        Label lbl = new Label(s);
        add(lbl);
        m_field = new TextField();
        add(m_field);
        reset();
    }

    public void reset() {
        m_field.setText(String.valueOf(val));
    }

    public void update() {
        val = m_field.getText();
    }
}
