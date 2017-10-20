package graph.filter;

import graph.*;
import java.awt.*;

/**
 * An interface for filter configuration properties. A concrete instance of
 * FilterProp is added to a dialog and allows the user to configure an instance
 * of some primitive data type which the filter uses in its processing of the
 * graph.<p>
 *
 * For example, a color wheel might be used to select a color to highlight
 * various nodes.
 *
 * @see Filter
 * @see FilterConfig
 * @see BooleanProp
 * @see ColorProp
 * @see FloatProp
 * @see IntProp
 * @see StringProp
 * @see VectorProp
 * @author Michael Shilman (michaels@eecs.berkeley.edu)
 * @version $Id$
 */
public abstract class FilterProp extends Panel {

    /**
     * Commit all the changes that the user has made to the property.
     */
    public abstract void update();

    /**
     * Reset the entries to the default values.
     */
    public abstract void reset();
}
