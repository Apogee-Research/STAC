package graph.editor;

import graph.*;
import java.awt.*;
import java.util.*;

/**
 * A simple graph viewer. It displays a graph and can also draw some "Painter"
 * objects which know how to draw into the window.<p>
 *
 * For now it also has a hacky key press handler which invokes a test action of
 * some sort on the graph.
 *
 * @see graph.Editor;
 * @see graph.Graph;
 * @author Michael Shilman (michaels@eecs.berkeley.edu)
 * @version $Id$
 */
public class Viewer extends Canvas {

    /**
     * The default viewer height.
     */
    public static int HEIGHT = 600;

    /**
     * The default viewer width.
     */
    public static int WIDTH = HEIGHT;

    /**
     * The default border around the viewer's canvas.
     */
    public static final int BORDER = 50;

    /**
     * XXX A hacky action which can be used to test various actions.
     */
    public Action action = null;

    /**
     * The graph that is displayed in the window.
     */
    public Graph graph = null;

    /**
     * The graph that is displayed in the window.
     */
    public Map<String, Graph> graphs = null;

    /**
     * A number of other painters which can paint into the window.
     */
    public Vector painters = new Vector();

    /**
     * The offscreen image for double buffering.
     */
    Image m_offImg = null;

    /**
     * Construct a new empty viewer.
     */
    public Viewer() {
        super();
    }

    /**
     * Construct a viewer of size <i>w x h</i>.
     */
    public Viewer(int w, int h) {
        WIDTH = w;
        HEIGHT = h;
    }

    public void update(Graphics g) {
        paint(g);
    }

    public Dimension preferredSize() {
        return new Dimension(WIDTH, HEIGHT);
    }

    /**
     * Handle key presses. The <i>return</i> key invokes the action member
     * variable.
     *
     * @see #action
     */
    public boolean keyDown(Event evt, int key) {
        switch (key) {
            case '\n':
                if (action != null) {
                    action.apply(graph);
                    repaint();
                    return true;
                } else {
                    return super.keyDown(evt, key);
                }
            default:
                return super.keyDown(evt, key);
        }
    }

    /**
     * XXX A hack to more easily allow subclasses of viewer to paint something
     * extra into the window.
     */
    protected void viewerPaint(Graphics g) {

    }

    /**
     * Paint the contents of the graph and each of the Painter objects into the
     * canvas.
     */
    public void paint(Graphics g) {
        if (m_offImg == null) {
            m_offImg = createImage(WIDTH, HEIGHT);
        }

        Graphics offg = m_offImg.getGraphics();
        super.paint(offg);
        graph.paint(offg);
        viewerPaint(offg);
        for (Enumeration e = painters.elements(); e.hasMoreElements();) {
            Painter p = (Painter) e.nextElement();
            p.paint(offg);
        }
        g.drawImage(m_offImg, 0, 0, this);
    }
}
