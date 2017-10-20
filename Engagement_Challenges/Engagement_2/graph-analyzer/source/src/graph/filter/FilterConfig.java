package graph.filter;

import graph.*;
import java.awt.*;

/**
 * A simple dialog box for manipulating configuration information of a filter.
 * It is constructed out of an array of FilterProps, each of which provide their
 * own GUI's. If the user hits the <i>done</i>
 * button, the properties are committed. If the user hits the
 * <i>cancel</i> button the changes are discarded.
 * <p>
 *
 * This class is provided as a convenience to make things easier for filter
 * writers. However, the Filter class itself simply returns a Frame to configure
 * itself, so special filters may have more custom configuration GUI's where
 * appropriate.
 *
 * @see Filter
 * @see FilterProp
 * @author Michael Shilman (michaels@eecs.berkeley.edu)
 * @version $Id$
 */
public class FilterConfig extends Frame {

    /**
     * The array of properties that are configured by this dialog.
     */
    protected FilterProp[] m_props = null;
    protected Button m_done = null;
    protected Button m_cancel = null;

    /**
     * Builds the dialog box for this dialog so that the user can configure the
     * filter parameters.
     */
    FilterConfig(FilterProp[] props) {
        m_props = props;
        setLayout(new GridLayout(m_props.length + 2, 1));
        for (int i = 0; i < m_props.length; i++) {
            add(m_props[i]);
        }
        m_done = new Button("Done");
        add(m_done);
        m_cancel = new Button("Cancel");
        add(m_cancel);

    }

    /**
     * Commit the changes to each of the properties.
     */
    public void update() {
        for (int i = 0; i < m_props.length; i++) {
            m_props[i].update();
        }
    }

    public boolean action(Event evt, Object what) {
        if (evt.target == m_done) {
            update();
            hide();
            return true;
        } else if (evt.target == m_cancel) {
            hide();
            return true;
        }
        return super.action(evt, what);
    }
}
