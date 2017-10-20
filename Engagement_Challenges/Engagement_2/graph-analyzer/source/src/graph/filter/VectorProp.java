package graph.filter;

import graph.*;
import java.awt.*;

/**
 * A class for storing vector configuration information for a filter.
 *
 * //XXX - clarify documentation
 *
 * @author Michael Shilman (michaels@eecs.berkeley.edu)
 * @version $Id$
 */
public class VectorProp extends FilterProp {

    public double[] val = new double[3];

    public VectorProp() {
    }

    public Component getGUI() {
        Button b = new Button("VectorProp");
        b.setBackground(Color.blue);
        return b;
    }

    @Override
    public void update() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void reset() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
