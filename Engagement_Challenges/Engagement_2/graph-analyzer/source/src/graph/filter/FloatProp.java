package graph.filter;

import graph.*;
import java.awt.*;

/**
 * A class for storing floating point number configuration information for a
 * filter.
 *
 * @author Michael Shilman (michaels@eecs.berkeley.edu)
 * @version $Id$
 */
public class FloatProp extends FilterProp {

    public float val = 0.0f;
    protected TextField m_field = null;

    public FloatProp(String s) {
        build(s);
    }

    public FloatProp(String s, float val) {
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
        Float f = new Float(m_field.getText());
        val = f.floatValue();
    }
}
