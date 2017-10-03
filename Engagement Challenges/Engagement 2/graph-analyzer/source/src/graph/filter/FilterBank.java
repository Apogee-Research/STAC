package graph.filter;

import graph.*;
import java.awt.*;
import java.util.*;

/**
 * A class for managing active filters. This manifests itself as a horizontal
 * row of buttons to be placed at the bottom of the editing window. The user can
 * add and delete filters, which adds and deletes their corresponding buttons.
 * When the user presses one of the buttons, a configuration dialog for that
 * filter pops up.<p>
 *
 * This provides a clean and simple way for the user to arbitrarily configure
 * his view on the data.
 *
 * @see Filter
 * @see FilterConfig
 * @see FilterPalette
 * @author Michael Shilman (michaels@eecs.berkeley.edu)
 * @version $Id$
 */
public class FilterBank extends Panel {

    public static final int MAX_FILTERS = 10;

    Button m_add = new Button("Add");
    Button m_delete = new Button("Delete");
    Panel m_filterButtons = new Panel();
    int m_numFilters = 0;
    Filter m_filters[] = new Filter[MAX_FILTERS];
    Button m_buttons[] = new Button[MAX_FILTERS];
    FilterPalette m_palette;
    GridBagLayout m_buttonLayout;
    GridBagConstraints m_buttonConstraints;
    static FilterBank s_this = null;
    Graph m_graph = null;

    public static FilterBank createFilterBank(Graph g) {
        //for now, make sure only one of these is
        //created, ever.
        if (s_this == null) {
            s_this = new FilterBank(g);
            return s_this;
        } else {
            return null;
        }
    }

    public static FilterBank getFilterBank() {
        return s_this;
    }

    public void setGraph(Graph g) {
        m_graph = g;
        update();
    }

    public Graph getGraph() {
        return m_graph;
    }

    public static void update() {
        if (s_this.m_graph != null) {
            s_this.apply(s_this.m_graph);
        }
    }

    public void apply(Graph g) {
        for (Enumeration e = g.nodes.elements(); e.hasMoreElements();) {
            Node n = (Node) e.nextElement();
            applySingle(n);
            if (n instanceof Graph) {
                Graph sub = (Graph) n;
                apply(sub);
            }
        }
    }

    public void applySingle(Node n) {
        for (int i = 0; i < m_numFilters; i++) {
            Filter f = m_filters[i];
            f.apply(n);
        }
    }

    private FilterBank(Graph g) {
        m_graph = g;

        GridBagLayout gb = new GridBagLayout();
        GridBagConstraints gc = new GridBagConstraints();
        setLayout(gb);

        gc.anchor = GridBagConstraints.WEST;
        gb.setConstraints(m_add, gc);
        add(m_add);
        gb.setConstraints(m_add, gc);
        add(m_delete);
        gc.gridwidth = GridBagConstraints.REMAINDER;
        gc.fill = GridBagConstraints.BOTH;
        gc.weightx = 1;
        gb.setConstraints(m_filterButtons, gc);
        m_buttonLayout = new GridBagLayout();
        m_buttonConstraints = new GridBagConstraints();
        m_buttonConstraints.insets = new Insets(3, 3, 3, 0);
        m_buttonConstraints.weightx = 1;
        m_buttonConstraints.anchor = GridBagConstraints.WEST;
        m_filterButtons.setLayout(m_buttonLayout);
        m_filterButtons.setBackground(Color.yellow);
        add(m_filterButtons);

        setBackground(Color.cyan);
    }

    void add(Filter f) {
        if (m_numFilters < (MAX_FILTERS - 1)) {
            m_filters[m_numFilters] = f;
            Button b = new Button(f.getName());
            m_buttons[m_numFilters] = b;
            m_buttonLayout.setConstraints(b, m_buttonConstraints);

            m_filterButtons.add(b);
            repaint();
            resize(size());
            m_numFilters++;
        }
    }

    public void setPalette(FilterPalette palette) {
        m_palette = palette;
    }

    void delete() {

    }

    public Dimension minimumSize() {
        return new Dimension(20, 300);
    }

    public boolean action(Event evt, Object what) {
        if (evt.target == m_add) {
            m_palette.show();
            return true;
        } else if (evt.target == m_delete) {
            //XXX delete the selected filter
            return true;
        } else {
            return super.action(evt, what);
        }
    }
}
