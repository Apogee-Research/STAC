// Copyright (c) 1996-99 The Regents of the University of California. All
// Rights Reserved. Permission to use, copy, modify, and distribute this
// software and its documentation without fee, and without a written
// agreement is hereby granted, provided that the above copyright notice
// and this paragraph appear in all copies.  This software program and
// documentation are copyrighted by The Regents of the University of
// California. The software program and documentation are supplied "AS
// IS", without any accompanying services from The Regents. The Regents
// does not warrant that the operation of the program will be
// uninterrupted or error-free. The end-user understands that the program
// was developed for research purposes and is advised not to rely
// exclusively on the program for any reason.  IN NO EVENT SHALL THE
// UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT,
// SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES, INCLUDING LOST PROFITS,
// ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
// THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
// SUCH DAMAGE. THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY
// WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
// MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
// PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
// CALIFORNIA HAS NO OBLIGATIONS TO PROVIDE MAINTENANCE, SUPPORT,
// UPDATES, ENHANCEMENTS, OR MODIFICATIONS.
package org.tigris.gef.graph.presentation;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.ToolTipManager;

import org.tigris.gef.base.NudgeAction;
import org.tigris.gef.base.SelectNextAction;
import org.tigris.gef.base.Diagram;
import org.tigris.gef.base.Editor;
import org.tigris.gef.base.Globals;
import org.tigris.gef.base.Layer;
import org.tigris.gef.base.LayerDiagram;
import org.tigris.gef.base.SelectNearAction;
import org.tigris.gef.base.ZoomAction;
import org.tigris.gef.di.DiagramElement;
import org.tigris.gef.event.GraphSelectionListener;
import org.tigris.gef.event.ModeChangeListener;
import org.tigris.gef.graph.ConnectionConstrainer;
import org.tigris.gef.graph.GraphEdgeRenderer;
import org.tigris.gef.graph.GraphModel;
import org.tigris.gef.graph.GraphNodeRenderer;
import org.tigris.gef.presentation.Fig;
import org.tigris.gef.presentation.FigText;
import org.tigris.gef.presentation.TextEditor;

/**
 * JGraph is a Swing component that displays a connected graph and allows
 * interactive editing. In many ways this class serves as a simple front-end to
 * class Editor, and other classes which do the real work.
 */
public class JGraph extends JPanel implements Cloneable, AdjustmentListener,
        MouseWheelListener {

    /**
     * The Editor object that is being shown in this panel
     */
    private Editor editor;

    private JGraphInternalPane drawingPane;

    private JScrollPane scrollPane;

    private Dimension defaultSize = new Dimension(6000, 6000);

    private Hashtable _viewPortPositions = new Hashtable();

    private String _currentDiagramId = null;

    private ZoomAction zoomOut = new ZoomAction(0.9);
    private ZoomAction zoomIn = new ZoomAction(1.1);

    // //////////////////////////////////////////////////////////////
    // constructor
    /**
     * Make a new JGraph with a new DefaultGraphModel.
     *
     * @see org.tigris.gef.graph.presentation.DefaultGraphModel
     */
    public JGraph() {
        this(new DefaultGraphModel());
    }

    /**
     * Make a new JGraph with a new DefaultGraphModel.
     *
     * @see org.tigris.gef.graph.presentation.DefaultGraphModel
     */
    public JGraph(ConnectionConstrainer cc) {
        this(new DefaultGraphModel(cc));
    }

    /**
     * Make a new JGraph with a the GraphModel and Layer from the given Diagram.
     */
    public JGraph(Diagram d) {
        this(new Editor(d));
    }

    /**
     * Make a new JGraph with the given GraphModel
     */
    public JGraph(GraphModel gm) {
        this(new Editor(gm, null));
    }

    /**
     * Make a new JGraph with the given Editor. All JGraph contructors
     * eventually call this contructor.
     */
    public JGraph(Editor ed) {
        super(false); // not double buffered. I do my own flicker-free redraw.
        editor = ed;
        drawingPane = new JGraphInternalPane(editor);
        setDrawingSize(getDefaultSize());

        scrollPane = new JScrollPane(drawingPane);

        scrollPane.setBorder(null);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(25);
        scrollPane.getVerticalScrollBar().setUnitIncrement(25);

        editor.setJComponent(drawingPane);
        setLayout(new BorderLayout());
        add(scrollPane, BorderLayout.CENTER);
        addMouseListener(editor);
        addMouseMotionListener(editor);
        addKeyListener(editor);
        scrollPane.getHorizontalScrollBar().addAdjustmentListener(this);
        scrollPane.getVerticalScrollBar().addAdjustmentListener(this);

        initKeys();

        validate();

        Collection layerManagerContent = ed.getLayerManager().getContents();
        if (layerManagerContent != null) {
            updateDrawingSizeToIncludeAllFigs(Collections
                    .enumeration(layerManagerContent));
        } // end if

        int mask = java.awt.event.KeyEvent.ALT_MASK
                | java.awt.event.KeyEvent.CTRL_MASK;
        establishAlternateMouseWheelListener(this, mask);
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object o) {
        if (o instanceof JGraph) {
            JGraph other = (JGraph) o;
            if (((this.getCurrentDiagramId() != null && this
                    .getCurrentDiagramId().equals(other.getCurrentDiagramId())) || (this
                    .getCurrentDiagramId() == null && other
                    .getCurrentDiagramId() == null))
                    && this.getEditor().equals(other.getEditor())) {
                return true;
            }
        }
        return false;
    }

    /**
     * @see Object#hashCode()
     *
     * TODO: Investigate further:
     * <p>
     *
     * According to a mail from GZ (6th November 2004) on the ArgoUML dev list,
     * {@link javax.swing.RepaintManager} puts these objects in some kind of
     * data structure that uses this function. Assuming that there is a reason
     * for this we dare not sabotage this by short-circuiting this to 0. Instead
     * we rely on that      {@link org.tigris.gef.graph.presentation.JGraph#setDiagram(
     * org.tigris.gef.base.Diagram)} actually removes this object from the
     * {@link javax.swing.RepaintManager} and registers it again when resetting
     * the diagram id.
     * <p>
     *
     * This is based on the assumption that the function {@link #equals(Object)}
     * must work as it does. I (Linus) have not understood why it must. Could
     * someone please explain that in the javadoc.
     */
    public int hashCode() {
        if (getCurrentDiagramId() == null) {
            return 0;
        } else {
            return getCurrentDiagramId().hashCode();
        }
    }

    public void addMouseListener(MouseListener listener) {
        drawingPane.addMouseListener(listener);
    }

    public void addMouseMotionListener(MouseMotionListener listener) {
        drawingPane.addMouseMotionListener(listener);
    }

    public void addKeyListener(KeyListener listener) {
        drawingPane.addKeyListener(listener);
    }

    /**
     * Make a copy of this JGraph so that it can be shown in another window.
     */
    public Object clone() {
        JGraph newJGraph = new JGraph((Editor) editor.clone());
        return newJGraph;
    }

    /* Set up some standard keystrokes and the Cmds that they invoke. */
    public void initKeys() {
        int shift = KeyEvent.SHIFT_MASK;
        int alt = KeyEvent.ALT_MASK;
        int meta = KeyEvent.META_MASK;

        bindKey(new SelectNextAction("Select Next", true), KeyEvent.VK_TAB, 0);
        bindKey(new SelectNextAction("Select Previous", false),
                KeyEvent.VK_TAB, shift);

        bindKey(new NudgeAction(NudgeAction.LEFT), KeyEvent.VK_LEFT, 0);
        bindKey(new NudgeAction(NudgeAction.RIGHT), KeyEvent.VK_RIGHT, 0);
        bindKey(new NudgeAction(NudgeAction.UP), KeyEvent.VK_UP, 0);
        bindKey(new NudgeAction(NudgeAction.DOWN), KeyEvent.VK_DOWN, 0);

        bindKey(new NudgeAction(NudgeAction.LEFT, 8), KeyEvent.VK_LEFT, shift);
        bindKey(new NudgeAction(NudgeAction.RIGHT, 8), KeyEvent.VK_RIGHT, shift);
        bindKey(new NudgeAction(NudgeAction.UP, 8), KeyEvent.VK_UP, shift);
        bindKey(new NudgeAction(NudgeAction.DOWN, 8), KeyEvent.VK_DOWN, shift);

        bindKey(new NudgeAction(NudgeAction.LEFT, 18), KeyEvent.VK_LEFT, alt);
        bindKey(new NudgeAction(NudgeAction.RIGHT, 18), KeyEvent.VK_RIGHT, alt);
        bindKey(new NudgeAction(NudgeAction.UP, 18), KeyEvent.VK_UP, alt);
        bindKey(new NudgeAction(NudgeAction.DOWN, 18), KeyEvent.VK_DOWN, alt);

        bindKey(new SelectNearAction(SelectNearAction.LEFT), KeyEvent.VK_LEFT,
                meta);
        bindKey(new SelectNearAction(SelectNearAction.RIGHT),
                KeyEvent.VK_RIGHT, meta);
        bindKey(new SelectNearAction(SelectNearAction.UP), KeyEvent.VK_UP, meta);
        bindKey(new SelectNearAction(SelectNearAction.DOWN), KeyEvent.VK_DOWN,
                meta);
    }

    /**
     * Utility function to bind a keystroke to a Swing Action. Note that GEF
     * Cmds are subclasses of Swing's Actions.
     */
    public void bindKey(ActionListener action, int keyCode, int modifiers) {
        drawingPane.registerKeyboardAction(action, KeyStroke.getKeyStroke(
                keyCode, modifiers), WHEN_FOCUSED);
    }

    // //////////////////////////////////////////////////////////////
    // accessors
    /**
     * Get the Editor that is being displayed
     */
    public Editor getEditor() {
        return editor;
    }

    /**
     * Set the Diagram that should be displayed by setting the GraphModel and
     * Layer that the Editor is using.
     */
    public void setDiagram(Diagram d) {
        if (d == null) {
            return;
        }
        if (_currentDiagramId != null) {
            _viewPortPositions.put(_currentDiagramId, scrollPane.getViewport()
                    .getViewRect());
        } // end if
        setDrawingSize(getDefaultSize());
        updateDrawingSizeToIncludeAllFigs(d.elements());
        editor.getLayerManager().replaceActiveLayer(d.getLayer());
        editor.setGraphModel(d.getGraphModel());
        editor.getSelectionManager().deselectAll();
        editor.setScale(d.getScale());
        String newDiagramId = Integer.toString(d.hashCode());
        if (newDiagramId.equals(_currentDiagramId)) {
            return;
        }
        _currentDiagramId = newDiagramId;
        if (_viewPortPositions.get(_currentDiagramId) != null) {
            Rectangle rect = (Rectangle) _viewPortPositions
                    .get(_currentDiagramId);
            scrollPane.getViewport().setViewPosition(new Point(rect.x, rect.y));
        } else {
            scrollPane.getViewport().setViewPosition(new Point());
        }
    }

    /**
     * Enlarges the JGraphInternalPane dimensions as necessary to insure that
     * all the contained Figs are visible.
     */
    protected void updateDrawingSizeToIncludeAllFigs(Enumeration iter) {
        if (iter == null) {
            return;
        }
        Dimension drawingSize = new Dimension(defaultSize.width,
                defaultSize.height);
        while (iter.hasMoreElements()) {
            Fig fig = (Fig) iter.nextElement();
            Rectangle rect = fig.getBounds();
            Point point = rect.getLocation();
            Dimension dim = rect.getSize();
            if ((point.x + dim.width + 5) > drawingSize.width) {
                drawingSize
                        .setSize(point.x + dim.width + 5, drawingSize.height);
            }
            if ((point.y + dim.height + 5) > drawingSize.height) {
                drawingSize
                        .setSize(drawingSize.width, point.y + dim.height + 5);
            }
        }
        setDrawingSize(drawingSize.width, drawingSize.height);
    }

    public void setDrawingSize(int width, int height) {
        setDrawingSize(new Dimension(width, height));
    }

    public void setDrawingSize(Dimension dim) {
        editor.drawingSizeChanged(dim);
    }

    /**
     * Set the GraphModel the Editor is using.
     */
    public void setGraphModel(GraphModel gm) {
        editor.setGraphModel(gm);
    }

    /**
     * Get the GraphModel the Editor is using.
     */
    public GraphModel getGraphModel() {
        return editor.getGraphModel();
    }

    /**
     * Get and set the Renderer used to make FigNodes for nodes in the
     * GraphModel.
     */
    public void setGraphNodeRenderer(GraphNodeRenderer r) {
        editor.setGraphNodeRenderer(r);
    }

    public GraphNodeRenderer getGraphNodeRenderer() {
        return editor.getGraphNodeRenderer();
    }

    /**
     * Get and set the Renderer used to make FigEdges for edges in the
     * GraphModel.
     */
    public void setGraphEdgeRenderer(GraphEdgeRenderer r) {
        editor.setGraphEdgeRenderer(r);
    }

    public GraphEdgeRenderer getGraphEdgeRenderer() {
        return editor.getGraphEdgeRenderer();
    }

    /**
     * When the JGraph is hidden, hide its internal pane
     */
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        drawingPane.setVisible(visible);
    }

    /**
     * Tell Swing/AWT that JGraph handles tab-order itself.
     */
    public boolean isManagingFocus() {
        return true;
    }

    /**
     * Tell Swing/AWT that JGraph can be tabbed into.
     */
    public boolean isFocusTraversable() {
        return true;
    }

    // //////////////////////////////////////////////////////////////
    // events
    /**
     * Add listener to the objects to notify whenever the Editor changes its
     * current selection.
     */
    public void addGraphSelectionListener(GraphSelectionListener listener) {
        getEditor().addGraphSelectionListener(listener);
    }

    public void removeGraphSelectionListener(GraphSelectionListener listener) {
        getEditor().removeGraphSelectionListener(listener);
    }

    public void addModeChangeListener(ModeChangeListener listener) {
        getEditor().addModeChangeListener(listener);
    }

    public void removeModeChangeListener(ModeChangeListener listener) {
        getEditor().removeModeChangeListener(listener);
    }

    // //////////////////////////////////////////////////////////////
    // Editor facade
    /**
     * The JGraph is painted by simply painting its Editor.
     */
    // public void paint(Graphics g) { _editor.paint(getGraphics()); }
    // //////////////////////////////////////////////////////////////
    // selection methods
    /**
     * Add the given item to this Editor's selections.
     */
    public void select(Fig f) {
        if (f == null) {
            deselectAll();
        } else {
            editor.getSelectionManager().select(f);
        }
    }

    /**
     * Find the Fig that owns the given item and select it.
     */
    public void selectByOwner(Object owner) {
        Layer lay = editor.getLayerManager().getActiveLayer();
        if (lay instanceof LayerDiagram) {
            select(((LayerDiagram) lay).presentationFor(owner));
        }
    }

    /**
     * Find Fig that owns the given item, or the item if it is a Fig, and select
     * it.
     */
    public void selectByOwnerOrFig(Object owner) {
        if (owner instanceof Fig) {
            select((Fig) owner);
        } else {
            selectByOwner(owner);
        }
    }

    /**
     * Add the Fig that owns the given item to this Editor's selections.
     */
    public void selectByOwnerOrNoChange(Object owner) {
        Layer lay = editor.getLayerManager().getActiveLayer();
        if (lay instanceof LayerDiagram) {
            Fig f = ((LayerDiagram) lay).presentationFor(owner);
            if (f != null) {
                select(f);
            }
        }
    }

    /**
     * Remove the given item from this editors selections.
     */
    public void deselect(Fig f) {
        editor.getSelectionManager().deselect(f);
    }

    /**
     * Select the given item if it was not already selected, and vis-a-versa.
     */
    public void toggleItem(Fig f) {
        editor.getSelectionManager().toggle(f);
    }

    /**
     * Deslect everything that is currently selected.
     */
    public void deselectAll() {
        editor.getSelectionManager().deselectAll();
    }

    /**
     * Select a collection of Figs.
     */
    public void select(Collection<DiagramElement> items) {
        editor.getSelectionManager().selectFigs(items);
    }

    /**
     * Select a collection of Figs. in GEF 0.13.1 use
     * select(Collection<DiagramElement>);
     */
    public void select(Vector items) {
        editor.getSelectionManager().select(items);
    }

    /**
     * Toggle the selection of a collection of Figs.
     */
    public void toggleItems(Vector items) {
        editor.getSelectionManager().toggle(items);
    }

    /**
     * reply a Vector of all selected Figs. Used in many Cmds.
     */
    public Vector selectedFigs() {
        return editor.getSelectionManager().getFigs();
    }

    // public Dimension getPreferredSize() { return new Dimension(1000, 1000); }
    // public Dimension getMinimumSize() { return new Dimension(1000, 1000); }
    // public Dimension getSize() { return new Dimension(1000, 1000); }
    public void setDefaultSize(int width, int height) {
        defaultSize = new Dimension(width, height);
    }

    public void setDefaultSize(Dimension dim) {
        defaultSize = dim;
    }

    public Dimension getDefaultSize() {
        return defaultSize;
    }

    /**
     * Get the position of the editor's scrollpane.
     */
    public Point getViewPosition() {
        return scrollPane.getViewport().getViewPosition();
    }

    /**
     * Set the position of the editor's scrollpane.
     */
    public void setViewPosition(Point p) {
        if (p != null) {
            scrollPane.getViewport().setViewPosition(p);
        }
    }

    /**
     * Establishes alternate MouseWheelListener object that's only active when
     * the alt/shift/ctrl keys are held down.
     *
     * @param listener MouseWheelListener that will receive MouseWheelEvents
     * generated by this JGraph.
     * @param mask logical OR of key modifier values as defined by
     * java.awt.event.KeyEvent constants. This has been tested with ALT_MASK,
     * SHIFT_MASK, and CTRL_MASK.
     */
    public void establishAlternateMouseWheelListener(
            MouseWheelListener listener, int mask) {

        WheelKeyListenerToggleAction keyListener = new WheelKeyListenerToggleAction(
                this.drawingPane, listener, mask);

        this.drawingPane.addKeyListener(keyListener);
    }

    static final long serialVersionUID = -5459241816919316496L;

    /**
     * @return Returns the _currentDiagramId.
     */
    protected String getCurrentDiagramId() {
        return _currentDiagramId;
    }

    public void adjustmentValueChanged(AdjustmentEvent e) {
        TextEditor textEditor = FigText.getActiveTextEditor();
        if (textEditor != null) {
            textEditor.endEditing();
        }
        editor.damageAll();
    }

    /**
     * Zooms diagram in and out when mousewheel is rolled while holding down
     * ctrl and/or alt key. Alt, because alt + mouse motion pans the diagram &
     * zooming while panning makes more sense than scrolling while panning.
     * Ctrl, because Ctrl/+ and Ctrl/- are used to zoom using the keyboard.
     */
    public void mouseWheelMoved(MouseWheelEvent e) {
        if (e.isAltDown() || e.isControlDown()) {

            if (e.getWheelRotation() < 0) {
                this.zoomOut.actionPerformed(null);
            } else if (e.getWheelRotation() > 0) {
                this.zoomIn.actionPerformed(null);
            }

            e.consume();
        }
    }
} /* end class JGraph */


class JGraphInternalPane extends JPanel {

    private Editor _editor;

    private boolean registeredWithTooltip;

    public JGraphInternalPane(Editor e) {
        _editor = e;
        setLayout(null);
        setDoubleBuffered(false);
    }

    public void paintComponent(Graphics g) {
        _editor.paint(g);
    }

    public Graphics getGraphics() {
        Graphics res = super.getGraphics();
        if (res == null) {
            return res;
        }
        Component parent = getParent();

        if (parent instanceof JViewport) {
            JViewport view = (JViewport) parent;
            Rectangle bounds = view.getBounds();
            Point pos = view.getViewPosition();
            res.clipRect(bounds.x + pos.x - 1, bounds.y + pos.y - 1,
                    bounds.width + 1, bounds.height + 1);
        }
        return res;
    }

    public Point getToolTipLocation(MouseEvent event) {
        event = Globals.curEditor().retranslateMouseEvent(event);
        return (super.getToolTipLocation(event));
    }

    public void setToolTipText(String text) {
        if ("".equals(text)) {
            text = null;
        }
        putClientProperty(TOOL_TIP_TEXT_KEY, text);
        ToolTipManager toolTipManager = ToolTipManager.sharedInstance();
        // if (text != null) {
        if (!registeredWithTooltip) {
            toolTipManager.registerComponent(this);
            registeredWithTooltip = true;
        }
    }

    protected void processMouseEvent(MouseEvent e) {
        if (e.getID() == MouseEvent.MOUSE_PRESSED) {
            requestFocus();
        }

        super.processMouseEvent(e);
    }

    /**
     * Tell Swing/AWT that JGraph handles tab-order itself.
     */
    public boolean isManagingFocus() {
        return true;
    }

    /**
     * Tell Swing/AWT that JGraph can be tabbed into.
     */
    public boolean isFocusTraversable() {
        return true;
    }

    static final long serialVersionUID = -5067026168452437942L;

} /* end class JGraphInternalPane */


class WheelKeyListenerToggleAction implements KeyListener {

    private int mask;
    private int down;

    private MouseWheelListener listener;
    private JPanel panel;

    /**
     * Creates KeyListener that adds and removes MouseWheelListener from
     * indicated JPanel so that it's only active when the modifier keys
     * (indicated by modifiersMask) are held down. Otherwise, the scrollbars
     * automatically managed by the JScrollPanel would never see the wheel
     * events.
     *
     * @param panel JPanel object that will be listening for MouseWheelEvents on
     * demand.
     * @param listener MouseWheelListener that listens for MouseWheelEvents
     * @param modifiersMask the logical OR of the AWT modifier keys values
     * defined as constants by the KeyEvent class. This has been tested with
     * ALT_MASK, CTRL_MASK, and SHIFT_MASK.
     */
    public WheelKeyListenerToggleAction(JPanel panel,
            MouseWheelListener listener, int modifiersMask) {
        this.panel = panel;
        this.listener = listener;
        this.mask = modifiersMask;
    }

    public synchronized void keyPressed(KeyEvent e) {
        if ((e.getModifiers() | mask) != mask) {
            return;
        }

        if (down == 0) {
            panel.addMouseWheelListener(listener);
        }
        down |= e.getModifiers();
    }

    public synchronized void keyReleased(KeyEvent e) {
        if ((e.getModifiers() & mask) == 0) {
            panel.removeMouseWheelListener(listener);
        }
        down = e.getModifiers();
    }

    public void keyTyped(KeyEvent e) {
    }
}
