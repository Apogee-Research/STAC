package graph.filter;

import graph.*;
import java.awt.*;
import java.util.Vector;

/**
 * A catalog for filter prototypes. When the user hits the
 * <i>add</i> button in the FilterBank, a FilterPalette is displayed. For now,
 * this is simply a list of available filters from which the user can select one
 * or more.<p>
 *
 * When the user finishes his selection and hits <i>done</i>, the filters are
 * cloned and added to the FilterBank.
 *
 * @see Filter
 * @see FilterBank
 * @author Michael Shilman (michaels@eecs.berkeley.edu)
 * @version $Id$
 */
public class FilterPalette extends Frame {

    private static final int WIDTH = 150;
    private static final int HEIGHT = 300;

    Button m_done = new Button("Done");
    Button m_cancel = new Button("Cancel");
    List m_list = new List();
    Vector m_filters = new Vector();
    FilterBank m_bank = null;

    public FilterPalette() {
        setLayout(new GridLayout(3, 1));
        add(m_list);
        add(m_done);
        add(m_cancel);
    }

    public void show() {
        resize(WIDTH, HEIGHT);
        super.show();
    }

    /**
     * Add another filter prototype to the palette.
     */
    public void add(Filter f) {
        m_filters.addElement(f);
        m_list.addItem(f.getName());
    }

    public void setBank(FilterBank bank) {
        m_bank = bank;
    }

    public boolean action(Event evt, Object what) {
        if (evt.target == m_done) {
            int indices[] = m_list.getSelectedIndexes();
            for (int i = 0; i < indices.length; i++) {
                Filter f = (Filter) m_filters.elementAt(indices[i]);
                m_bank.add(f);
            }
            hide();
            return true;
        } else if (evt.target == m_cancel) {
            hide();
            return true;
        } else {
            return super.action(evt, what);
        }
    }
}
