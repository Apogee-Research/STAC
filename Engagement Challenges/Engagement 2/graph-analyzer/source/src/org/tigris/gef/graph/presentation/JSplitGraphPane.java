package org.tigris.gef.graph.presentation;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.JPanel;
import javax.swing.JSplitPane;

import org.tigris.gef.base.Editor;
import org.tigris.gef.base.Globals;
import org.tigris.gef.graph.presentation.JGraph;

/**
 * A split pane container for showing two instances of the same graph.
 *
 * @author Bob Tarling
 */
public class JSplitGraphPane extends JPanel {

    private static final long serialVersionUID = 3796638763703844578L;

    /**
     * The clone graph will snap shut if it is smaller then this
     */
    private static final int MINIMUM_CLONE_HEIGHT = 30;

    /**
     * A zero by zero dimension
     */
    private static final Dimension ZERO_DIMENSION = new Dimension(0, 0);

    /**
     * The maximum possible dimension
     */
    private static final Dimension MAX_DIMENSION = new Dimension(
            Integer.MAX_VALUE, Integer.MAX_VALUE);

    /**
     * The cloned graph sits inside a clone container. This is initially an
     * empty panel and of limitless size, allowing JSplitPane to resize the
     * panel to any position. As soon as this panel is expanded a clone of the
     * graph is generated and placed inside. If this panel is collapsed then the
     * graph is removed and disposed of.
     */
    private final JPanel CLONE_CONTAINER = new JPanel();

    private JSplitPane splitPane = null;

    /**
     * A clone graph which is created or destroyed by reposition of the
     * splitter.
     */
    private JGraph clonedGraph;

    /**
     * Construct a new split graph pane containing a graph
     *
     * @param graph
     */
    public JSplitGraphPane(JGraph graph) {
        splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        this.setLayout(new BorderLayout());
        this.add(splitPane);

        graph.setPreferredSize(MAX_DIMENSION);

        CLONE_CONTAINER.setSize(ZERO_DIMENSION);
        CLONE_CONTAINER.setMinimumSize(ZERO_DIMENSION);
        CLONE_CONTAINER.setPreferredSize(ZERO_DIMENSION);
        CLONE_CONTAINER.setLayout(new BorderLayout());

        splitPane.add(graph, JSplitPane.TOP);
        splitPane.add(CLONE_CONTAINER, JSplitPane.BOTTOM);
        splitPane.resetToPreferredSizes();

        CLONE_CONTAINER.addComponentListener(new CloneSizeWatcher());
    }

    private class CloneSizeWatcher extends ComponentAdapter {

        public void componentResized(ComponentEvent arg0) {
            int newHeight = CLONE_CONTAINER.getHeight();
            if (newHeight > MINIMUM_CLONE_HEIGHT) {
                if (clonedGraph == null) {
                    Editor ce = Globals.curEditor();
                    Editor ed = (Editor) ce.clone();
                    clonedGraph = new JGraph(ed);
                    CLONE_CONTAINER.add(clonedGraph);
                    validate();
                }
            } else {
                if (clonedGraph != null) {
                    CLONE_CONTAINER.remove(clonedGraph);
                    clonedGraph = null;
                }
                splitPane.resetToPreferredSizes();
            }
        }
    }
}
