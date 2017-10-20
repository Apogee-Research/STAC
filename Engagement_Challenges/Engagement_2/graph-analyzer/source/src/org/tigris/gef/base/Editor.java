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
// File: Editor.java
// Classes: Editor
// Original Author: ics125 spring 1996
// $Id: Editor.java 1305 2011-04-17 20:26:38Z bobtarling $
package org.tigris.gef.base;

import org.graph.commons.logging.Log;
import org.graph.commons.logging.LogFactory;
import org.tigris.gef.event.GraphSelectionListener;
import org.tigris.gef.event.ModeChangeListener;
import org.tigris.gef.graph.GraphEdgeRenderer;
import org.tigris.gef.graph.GraphModel;
import org.tigris.gef.graph.GraphNodeRenderer;
import org.tigris.gef.presentation.Fig;
import org.tigris.gef.presentation.FigText;
import org.tigris.gef.presentation.FigTextEditor;
import org.tigris.gef.presentation.TextEditor;

import javax.swing.*;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.*;
import java.io.Serializable;
import java.util.List;
import org.tigris.gef.base.CmdSavePNG.SpecialGraphics;

/**
 * This class provides an editor for manipulating graphical documents. The
 * editor is the central class of the graph editing framework, but it does not
 * contain very much code. It can be this small because all the net-level
 * models, graphical objects, layers, editor modes, editor commands, and
 * supporting dialogs and frames are implemented in their own classes.
 * <p>
 *
 * An Editor's LayerManager has a stack of Layers. Normally Layers contain Figs.
 * Some Figs are linked to NetPrimitives. When Figs are selected the
 * SelectionManager holds a Selection object. The behaviour of the Editor is
 * determined by its current Mode. The Editor's ModeManager keeps track of all
 * the active Modes. Modes interpret user input events and decide how to change
 * the state of the diagram. The Editor acts as a shell for executing Commands
 * that modify the document or the Editor itself.
 * <p>
 *
 * When Figs change visible state (e.g. color, size, or position) they tell
 * their Layer that they are damageAll and need to be repainted. The Layer tells
 * all Editors that are editing the Fig.
 *
 * A major goal of GEF is to make it easy to extend the framework for
 * application to a specific domain. It is very important that new functionality
 * can be added without modifying what is already there. The fairly small size
 * of the Editor is a good indicator that it is not a bottleneck for enhancing
 * the framework.
 * <p>
 *
 * @see Layer
 * @see Fig
 * @see org.tigris.gef.graph.presentation.NetPrimitive
 * @see Selection
 * @see Mode
 * @see Cmd
 */
public class Editor implements Serializable, MouseListener,
        MouseMotionListener, KeyListener {
    // //////////////////////////////////////////////////////////////
    // constants

    /**
     *
     */
    private static final long serialVersionUID = 2324579872610012639L;

    /**
     * Clicking exactly on a small shape is hard for users to do. GRIP_MARGIN
     * gives them a chance to have the mouse outside a Fig by a few pixels and
     * still hit it.
     */
    public static final int GRIP_SIZE = 8;

    // //////////////////////////////////////////////////////////////
    // instance variables
    /**
     * The user interface mode that the Editor is currently in. Generally Modes
     * that the user has to think about are a bad idea. But even in a very easy
     * to use editor there are plenty of "spring-loaded" modes that change the
     * way the system interprets input. For example, when placing a new node,
     * the editor is in ModePlace, and when dragging a handle of an object the
     * editor is in ModeModify. In each case moving or dragging the mouse has a
     * different effect.
     *
     * @see ModeModify
     * @see ModeSelect
     * @see ModePlace
     */
    protected ModeManager _modeManager = new ModeManager(this);

    /**
     * This points to the document object that the user is working on. At this
     * point the framework does not have a very strong concept of document and
     * there is no class Document. For now the meaning of this pointer is in the
     * hands of the person applying this framework to an application.
     */
    protected Object _document;

    /**
     * All the selection objects for what the user currently has selected.
     */
    protected SelectionManager _selectionManager = new SelectionManager(this);

    /**
     * The LayerManager for this Editor.
     */
    protected LayerManager _layerManager = new LayerManager(this);

    /**
     * The grid to snap points to.
     */
    protected Guide _guide = new GuideGrid(8);

    /**
     * The Fig that the mouse is in.
     */
    private Fig _curFig = null;

    /**
     * The Selection object that the mouse is in.
     */
    private Selection _curSel = null;

    /**
     * The scale at which to draw the diagram
     */
    private double _scale = 1.0;

    /**
     * Should elements in this editor be selectable?
     */
    protected boolean _canSelectElements = true;

    /**
     * Should this editor be repainted?
     */
    private transient boolean _shouldPaint = true;

    /**
     * The swing panel that the Editor draws to.
     */
    private transient JComponent jComponent;

    /**
     * The width of the swing panel before scaling.
     */
    private transient int _naturalComponentWidth;

    /**
     * The height of the swing panel before scaling.
     */
    private transient int _naturalComponentHeight;

    /**
     * The ancestor of _jComponent that has a peer that can create an image.
     */
    private transient Component _peer_component = null;

    private RenderingHints _renderingHints = new RenderingHints(null);

    /**
     * The context menu for this editor
     */
    private transient JPopupMenu _popup = null;

    private static Log LOG = LogFactory.getLog(Editor.class);

    private FigTextEditor _activeTextEditor = null;

    // //////////////////////////////////////////////////////////////
    // constructors and related functions
    /**
     * Construct a new Editor to edit the given NetList
     */
    public Editor(GraphModel gm, JComponent jComponent) {
        this(gm, jComponent, null);
    }

    public Editor(GraphModel gm) {
        this(gm, null, null);
    }

    public Editor() {
        this(null, null, null);
    }

    public Editor(Diagram d) {
        this(d.getGraphModel(), null, d.getLayer());
    }

    public Editor(GraphModel gm, JComponent jComponent, Layer lay) {
        this.jComponent = jComponent;
        defineLayers(gm, lay);

        // Push the default modes onto the mode stack (or those configured
        // by the client application).
        List<ModeFactory> modeFactories = Globals.getDefaultModeFactories();
        for (ModeFactory factory : modeFactories) {
            pushMode(factory.createMode(this));
        }

        Globals.curEditor(this);

        _renderingHints.put(RenderingHints.KEY_FRACTIONALMETRICS,
                RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        _renderingHints.put(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_SPEED);
    }

    protected void defineLayers(GraphModel gm, Layer lay) {
        _layerManager.addLayer(new LayerGrid());
        // _layerManager.addLayer(new LayerPageBreaks());
        // the following line is an example of another "grid"
        // _layerManager.addLayer(new LayerPolar());
        if (lay != null) {
            _layerManager.addLayer(lay);
        } else if (gm == null) {
            _layerManager.addLayer(new LayerGrid());
        } else {
            _layerManager.addLayer(new LayerPerspective("untitled", gm));
        }
    }

    /**
     * Called before the Editor is saved to a file.
     */
    public void preSave() {
        _layerManager.preSave();
    }

    /**
     * Called after the Editor is saved to a file.
     */
    public void postSave() {
        _layerManager.postSave();
    }

    public void setPopupMenu(JPopupMenu p) {
        _popup = p;
    }

    public JPopupMenu getPopupMenu() {
        return _popup;
    }

    /**
     * Called after the Editor is loaded from a file.
     */
    public void postLoad() {
        _layerManager.postLoad();
    }

    /**
     * Return true if the Grid layer is currently hidden.
     */
    public boolean getGridHidden() {
        boolean h = false;
        Layer l = _layerManager.findLayerNamed("Grid");
        if (l != null) {
            h = l.getHidden();
        }
        return h;
    }

    /**
     * Set the hidden state of the Grid layer.
     */
    public void setGridHidden(boolean b) {
        Layer l = _layerManager.findLayerNamed("Grid");
        if (l != null) {
            l.setHidden(b);
        }
    }

    /**
     * Clone the receiving editor. Called from ActionSpawn. Subclasses of Editor
     * should override this method. TODO shouldn't this just call super.clone()
     * instead of using reflection? Bob 29 Jan 2004
     */
    public Object clone() {
        try {
            Editor ed = (Editor) this.getClass().newInstance();
            ed.getLayerManager().addLayer(_layerManager.getActiveLayer());
            // needs-more-work: does not duplicate layer stack!
            ed.document(document());
            return ed;
        } catch (java.lang.IllegalAccessException ignore) {
            LOG.error("IllegalAccessException in spawn");
        } catch (java.lang.InstantiationException ignore) {
            LOG.error("InstantiationException in spawn");
        }
        return null;
    }

    // //////////////////////////////////////////////////////////////
    // / methods related to editor state: graphical attributes, modes, view
    public ModeManager getModeManager() {
        return _modeManager;
    }

    /**
     * Pushes a new mode to the mode manager
     */
    public void pushMode(FigModifyingMode mode) {
        _modeManager.push(mode);
        mode.setEditor(this);
        Globals.showStatus(mode.instructions());
    }

    /**
     * Set this Editor's current Mode to the next global Mode.
     */
    public void finishMode() {
        _modeManager.pop();
        pushMode((FigModifyingMode) Globals.mode());
        Globals.clearStatus();
    }

    /**
     * Return the LayerComposite that holds the diagram being edited.
     */
    public LayerManager getLayerManager() {
        return _layerManager;
    }

    public double getScale() {
        return _scale;
    }

    /**
     * Set this Editor's drawing scale. A value of 1.0 draws at 1 to 1. A value
     * greater than 1 draws larger, less than 1 draws smaller. Conceptually the
     * scale is an attribute of JGraph, but the editor needs to know it to paint
     * accordingly.
     */
    public void setScale(double scale) {
        _scale = scale;
        _layerManager.setScale(_scale);
        jComponent.setPreferredSize(new Dimension(
                (int) (_naturalComponentWidth * _scale),
                (int) (_naturalComponentHeight * _scale)));
        damageAll();
    }

    /**
     * Returns this Editor's current value for the selection flag.
     *
     * @return The current value of the selection flag.
     */
    public boolean canSelectElements() {
        return _canSelectElements;
    }

    /**
     * Set's the selection flag for the Editor. If the flag is set to true
     * (default), elements in this Editor are selectable. Otherwise, elements
     * are not selectable, neither by keyboard nor by mouse activity.
     *
     * @param selectable New value for the flag.
     */
    public void setElementsSelectable(boolean selectable) {
        _canSelectElements = selectable;
    }

    /**
     * Return the net under the diagram being edited.
     */
    public GraphModel getGraphModel() {
        Layer active = _layerManager.getActiveLayer();
        if (active instanceof LayerPerspective) {
            return ((LayerPerspective) active).getGraphModel();
        }
        return null;
    }

    public void setGraphModel(GraphModel gm) {
        Layer active = _layerManager.getActiveLayer();
        if (active instanceof LayerPerspective) {
            ((LayerPerspective) active).setGraphModel(gm);
        }
    }

    /**
     * Get the renderer object that decides how to display nodes
     */
    public GraphNodeRenderer getGraphNodeRenderer() {
        Layer active = _layerManager.getActiveLayer();
        if (active instanceof LayerPerspective) {
            return ((LayerPerspective) active).getGraphNodeRenderer();
        }
        return null;
    }

    public void setGraphNodeRenderer(GraphNodeRenderer rend) {
        Layer active = _layerManager.getActiveLayer();
        if (active instanceof LayerPerspective) {
            ((LayerPerspective) active).setGraphNodeRenderer(rend);
        }
    }

    /**
     * Get the renderer object that decides how to display edges
     */
    public GraphEdgeRenderer getGraphEdgeRenderer() {
        Layer active = _layerManager.getActiveLayer();
        if (active instanceof LayerPerspective) {
            return ((LayerPerspective) active).getGraphEdgeRenderer();
        }
        return null;
    }

    public void setGraphEdgeRenderer(GraphEdgeRenderer rend) {
        Layer active = _layerManager.getActiveLayer();
        if (active instanceof LayerPerspective) {
            ((LayerPerspective) active).setGraphEdgeRenderer(rend);
        }
    }

    // //////////////////////////////////////////////////////////////
    // methods related to adding, removing, and accessing Figs
    // shown in the editor
    /**
     * Returns a list of all Figs in the layer currently being edited.
     */
    public List<Fig> getFigs() {
        return _layerManager.getContents();
    }

    /**
     * Add a Fig to the diagram being edited.
     */
    public void add(Fig f) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Adding fig " + f);
        }
        getLayerManager().add(f);
    }

    /**
     * Remove a Fig from the diagram being edited.
     */
    public void remove(Fig f) {
        getLayerManager().remove(f);
    }

    /**
     * Temp var used to implement hit() without doing memory allocation.
     */
    protected static Rectangle _hitRect = new Rectangle(0, 0, GRIP_SIZE,
            GRIP_SIZE);

    /**
     * Reply the top Fig in the current layer that contains the given point.
     * This is used in determining what the user clicked on, among other uses.
     */
    public final Fig hit(Point p) {
        _hitRect.setLocation(p.x - GRIP_SIZE / 2, p.y - GRIP_SIZE / 2);
        return hit(_hitRect);
    }

    public final Fig hit(int x, int y) {
        _hitRect.setLocation(x - GRIP_SIZE / 2, y - GRIP_SIZE / 2);
        return hit(_hitRect);
    }

    public final Fig hit(int x, int y, int w, int h) {
        return hit(new Rectangle(x, y, w, h));
    }

    /**
     * Reply the top Fig in the current layer that contains the given rectangle.
     * This is called by all other hit methods.
     */
    public Fig hit(Rectangle r) {
        Fig f = getLayerManager().hit(r);
        return f;
    }

    /**
     * Find the Fig under the mouse, and the node it represents, if any
     */
    protected void setUnderMouse(MouseEvent me) {
        int x = me.getX(), y = me.getY();
        Fig f = hit(x, y);
        if (f != _curFig) {
            if (_curFig instanceof MouseListener) {
                ((MouseListener) _curFig).mouseExited(me);
            }
            if (f instanceof MouseListener) {
                ((MouseListener) f).mouseEntered(me);
            }
        }
        _curFig = f;

        if (_canSelectElements) {
            Selection sel = _selectionManager.findSelectionAt(x, y);
            if (sel != _curSel) {
                if (_curSel != null) {
                    _curSel.mouseExited(me);
                }
                if (sel != null) {
                    sel.mouseEntered(me);
                }
            }
            _curSel = sel;
        }
    }

    // //////////////////////////////////////////////////////////////
    // document related methods
    /**
     * Get and set document being edited. There are no deep semantics here yet,
     * a "document" is up to you to define.
     */
    public Object document() {
        return _document;
    }

    public void document(Object d) {
        _document = d;
    }

    // //////////////////////////////////////////////////////////////
    // Guide and layout related commands
    /**
     * Modify the given point to be on the guideline (In this case, a gridline).
     */
    public void snap(Point p) {
        if (_guide != null) {
            _guide.snap(p);
        }
    }

    public Guide getGuide() {
        return _guide;
    }

    public void setGuide(Guide g) {
        _guide = g;
    }

    // //////////////////////////////////////////////////////////////
    // recording damage to the display for later repair
    /**
     * Calling any one of the following damageAll() methods adds a damageAll
     * region (rectangle) that will be redrawn asap.
     */
    public void damaged(Rectangle r) {
        damaged(r.x, r.y, r.width, r.height);
    }

    /**
     * Calling any one of the following damageAll() methods adds a damageAll
     * region (rectangle) that will be redrawn asap. The given bounds must
     * already be scaled accordingly.
     */
    public void damaged(int x, int y, int width, int height) {
        getJComponent().repaint(0, x, y, width, height);
    }

    /**
     * This method will take the current scale into account
     *
     * @param sel
     */
    public void damaged(Selection sel) {
        Rectangle bounds = sel.getBounds();
        scaleRect(bounds);

        damaged(bounds);
    }

    public void damaged(Fig f) {
        // - if (_redrawer == null) _redrawer = new RedrawManager(this);
        // the line above should not be needed, but without it I get
        // NullPointerExceptions...
        // - if (f != null) _redrawer.add(f);
        ((JComponent) getJComponent()).repaint();
    }

    public void scaleRect(Rectangle bounds) {
        bounds.x = (int) Math.floor(bounds.x * _scale);
        bounds.y = (int) Math.floor(bounds.y * _scale);
        bounds.width = (int) Math.floor(bounds.width * _scale) + 1;
        bounds.height = (int) Math.floor(bounds.height * _scale) + 1;
    }

    /**
     * Mark the entire visible area of this Editor as damageAll. Currently
     * called when a LayerGrid is adjusted. This will be useful for
     * ActionRefresh if I get around to it. Also some Actions may prefer to do
     * this instead of keeping track of all modified objects, but only in cases
     * where most of the visible area is expected to change anyway.
     */
    public void damageAll() {
        if (jComponent != null) {
            Rectangle r = jComponent.getVisibleRect();
            jComponent.revalidate();
            jComponent.repaint(r.x, r.y, r.width, r.height);
        }
    }

    // //////////////////////////////////////////////////////////////
    // display methods
    /**
     * Paints the graphs nodes by calling paint() on layers, selections, and
     * mode.
     *
     */
    public void paint(Graphics g) {
        if (!shouldPaint()) {
            return;
        }

        if (g instanceof Graphics2D) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHints(_renderingHints);
            g2.scale(_scale, _scale);
        }
        getLayerManager().paint(g);
        // getLayerManager().getActiveLayer().paint(g);
        if (_canSelectElements) {
            _selectionManager.paint(g);
            _modeManager.paint(g);
        }
    }

    public void print(Graphics g) {
        getLayerManager().paint(g);
    }

    public void print(GraphicsSpecial sg, Graphics g) {
        getLayerManager().paint(sg, g);
    }

    /**
     * Scroll the JGraph so that the given point is visible. This is used when
     * the user wants to drag an object a long distance. This is commented out
     * right now because it causes too many out of memory errors and the size of
     * the JGraphInternalPanel is not set properly.
     */
    public void scrollToShow(int x, int y) {
        // Component c = getJComponent();
        // if (c != null && c.getParent() instanceof JViewport) {
        // JViewport view = (JViewport) c.getParent();
        // view.scrollRectToVisible(new Rectangle(x - 10, y - 10, 20, 20));
        // }
    }

    /**
     * Scroll the JGraph so that the given Fig is entirely visible.
     */
    public void scrollToShow(Fig fig) {
        Rectangle bounds = new Rectangle((int) (fig.getX() * _scale),
                (int) (fig.getY() * _scale), (int) (fig.getWidth() * _scale),
                (int) (fig.getHeight() * _scale));
        bounds.grow((int) (50 * _scale), (int) (50 * _scale));
        JComponent c = getJComponent();
        if (c != null) {
            c.scrollRectToVisible(bounds);
        }
    }

    /**
     * Reply the current SelectionManager of this Editor.
     */
    public SelectionManager getSelectionManager() {
        return _selectionManager;
    }

    public Fig getCurrentFig() {
        return _curFig;
    }

    // //////////////////////////////////////////////////////////////
    // Frame and panel related methods
    public JComponent getJComponent() {
        return jComponent;
    }

    public void setJComponent(JComponent c) {
        jComponent = c;
        _peer_component = null;
    }

    public void setCursor(Cursor c) {
        if (getJComponent() != null) {
            getJComponent().setCursor(c);
            java.awt.Toolkit.getDefaultToolkit().sync();
        }
    }

    /**
     * Find the AWT Frame that this Editor is being displayed in. This is needed
     * to open a dialog box.
     */
    public Frame findFrame() {
        Component c = jComponent;
        while (c != null && !(c instanceof Frame)) {
            c = c.getParent();
        }
        return (Frame) c;
    }

    /**
     * Create an Image (an off-screen bit-map) to be used to reduce flicker in
     * redrawing.
     * <p>
     *
     * The image is also useable for other purposes, e.g. to put a bitmap of a
     * diagram on the system clipboard.
     */
    public Image createImage(int w, int h) {
        if (jComponent == null) {
            return null;
        }
        if (_peer_component == null) {
            _peer_component = jComponent;
            //while (_peer_component instanceof JComponent)
            // getPeer() is deprecated
            //    _peer_component = _peer_component.getParent();
        }
        // try { if (_jComponent.getPeer() == null) _jComponent.addNotify(); }
        // catch (java.lang.NullPointerException ignore) { }
        // This catch works around a bug:
        // Sometimes there is an exception in the AWT peer classes,
        // but the next line should still work, despite the exception
        return _peer_component.createImage(w, h);
    }

    /**
     * Get the backgrund color of the Editor. Often, none of the background will
     * be visible because LayerGrid covers the entire drawing area.
     */
    public Color getBackground() {
        if (jComponent == null) {
            return Color.lightGray;
        }
        return jComponent.getBackground();
    }

    public void setActiveTextEditor(FigTextEditor fte) {
        FigTextEditor oldTextEditor = _activeTextEditor;
        _activeTextEditor = fte;
        if (oldTextEditor != null) {
            oldTextEditor.endEditing();
        }
    }

    public TextEditor getActiveTextEditor() {
        if (_activeTextEditor != null) {
            return FigText.getActiveTextEditor();
        } else {
            return null;
        }
    }

    /**
     * This method is called when the Editor is notified that the drawing
     * panel's natural size has changed, typically because a new diagram has
     * been set.
     */
    public void drawingSizeChanged(Dimension dim) {
        _naturalComponentWidth = dim.width;
        _naturalComponentHeight = dim.height;
        if (jComponent != null) {
            jComponent.setPreferredSize(new Dimension(
                    (int) (_naturalComponentWidth * _scale),
                    (int) (_naturalComponentHeight * _scale)));
            jComponent.revalidate();
        }
    }

    // //////////////////////////////////////////////////////////////
    // event handlers
    /**
     * Remember to notify listener whenever the selection changes.
     */
    public void addGraphSelectionListener(GraphSelectionListener listener) {
        _selectionManager.addGraphSelectionListener(listener);
    }

    /**
     * Stop notifing listener of selection changes.
     */
    public void removeGraphSelectionListener(GraphSelectionListener listener) {
        _selectionManager.removeGraphSelectionListener(listener);
    }

    /**
     * Remember to notify listener whenever the mode changes.
     */
    public void addModeChangeListener(ModeChangeListener listener) {
        _modeManager.addModeChangeListener(listener);
    }

    /**
     * Stop notifying listener of mode changes.
     */
    public void removeModeChangeListener(ModeChangeListener listener) {
        _modeManager.removeModeChangeListener(listener);
    }

    // //////////////////////////////////////////////////////////////
    // JDK 1.1 AWT event handlers
    /**
     * Scales the mouse coordinates (which match the drawing scale) back to the
     * model scale.
     *
     * @see #setScale(double)
     */
    public MouseEvent translateMouseEvent(MouseEvent me) {
        double xp = me.getX();
        double yp = me.getY();
        me.translatePoint((int) Math.round((xp / _scale) - me.getX()),
                (int) Math.round((yp / _scale) - me.getY()));
        return me;
    }

    /**
     * Scales the mouse coordinates (which match the model scale) back to the
     * drawing scale.
     *
     * @see #setScale(double)
     */
    public MouseEvent retranslateMouseEvent(MouseEvent me) {
        double xp = me.getX();
        double yp = me.getY();
        int dx = (int) (xp * _scale - xp);
        int dy = (int) (yp * _scale - yp);
        me.translatePoint(dx, dy);
        return me;
    }

    /**
     * Invoked after the mouse has been pressed and released. All events are
     * passed on the SelectionManager and then ModeManager.
     */
    public void mouseClicked(MouseEvent me) {
        translateMouseEvent(me);
        Globals.curEditor(this);

        // setUnderMouse(me);
        if (_canSelectElements) {
            _selectionManager.mouseClicked(me);
        }
        if (_curFig instanceof MouseListener) {
            ((MouseListener) _curFig).mouseClicked(me);
        }
        if (_canSelectElements) {
            _modeManager.mouseClicked(me);
        }
    }

    /**
     * Invoked when a mouse button has been pressed.
     */
    public void mousePressed(MouseEvent me) {
        if (me.isConsumed()) {
            if (LOG.isDebugEnabled()) {
                LOG
                        .debug("MousePressed detected but rejected as already consumed");
            }
            return;
        }
        translateMouseEvent(me);
        TextEditor textEditor = FigText.getActiveTextEditor();
        if (textEditor != null) {
            textEditor.endEditing();
        }

        Globals.curEditor(this);
        // setUnderMouse(me);
        if (_curFig instanceof MouseListener) {
            ((MouseListener) _curFig).mousePressed(me);
        }
        if (_canSelectElements) {
            _selectionManager.mousePressed(me);
            _modeManager.mousePressed(me);
        }
    }

    /**
     * Invoked when a mouse button has been released.
     */
    public void mouseReleased(MouseEvent me) {
        translateMouseEvent(me);
        Globals.curEditor(this);

        if (_curFig instanceof MouseListener) {
            ((MouseListener) _curFig).mouseReleased(me);
        }
        if (_canSelectElements) {
            _selectionManager.mouseReleased(me);
            _modeManager.mouseReleased(me);
        }
    }

    /**
     * Invoked when the mouse enters the Editor.
     */
    public void mouseEntered(MouseEvent me) {
        translateMouseEvent(me);
        Globals.curEditor(this);
        pushMode((FigModifyingMode) Globals.mode());
        setUnderMouse(me);
        if (_canSelectElements) {
            _modeManager.mouseEntered(me);
        }
    }

    /**
     * Invoked when the mouse exits the Editor.
     */
    public void mouseExited(MouseEvent me) {
        translateMouseEvent(me);
        setUnderMouse(me);
        if (_curFig instanceof MouseListener) {
            ((MouseListener) _curFig).mouseExited(me);
        }
    }

    /**
     * Invoked when a mouse button is pressed in the Editor and then dragged.
     * Mouse drag events will continue to be delivered to the Editor where the
     * first originated until the mouse button is released (regardless of
     * whether the mouse position is within the bounds of the Editor). BTW, this
     * makes drag and drop editing almost impossible.
     */
    public void mouseDragged(MouseEvent me) {
        translateMouseEvent(me);
        Globals.curEditor(this);
        setUnderMouse(me);
        if (_canSelectElements) {
            _selectionManager.mouseDragged(me);
            _modeManager.mouseDragged(me);
        }
    }

    /**
     * Invoked when the mouse button has been moved (with no buttons no down).
     */
    public void mouseMoved(MouseEvent me) {
        translateMouseEvent(me);
        Globals.curEditor(this);
        setUnderMouse(me);
        if (_curFig != null && Globals.getShowFigTips()) {
            String tip = _curFig.getTipString(me);
            if (tip != null && tip.length() > 0 && !tip.endsWith(" ")) {
                tip += " ";
            }
            if (tip != null && (jComponent instanceof JComponent)) {
                jComponent.setToolTipText(tip);
            }
        } else {
            jComponent.setToolTipText(null); // was ""
        }
        if (_canSelectElements) {
            _selectionManager.mouseMoved(me);
            _modeManager.mouseMoved(me);
        }
    }

    /**
     * Invoked when a key has been pressed and released. The KeyEvent has its
     * keyChar ivar set to something, keyCode ivar is junk.
     */
    public void keyTyped(KeyEvent ke) {
        Globals.curEditor(this);
        if (_canSelectElements) {
            _selectionManager.keyTyped(ke);
            _modeManager.keyTyped(ke);
        }
    }

    /**
     * Invoked when a key has been pressed. The KeyEvent has its keyCode ivar
     * set to something, keyChar ivar is junk.
     */
    public void keyPressed(KeyEvent ke) {
        Globals.curEditor(this);
        if (_canSelectElements) {
            _selectionManager.keyPressed(ke);
            _modeManager.keyPressed(ke);
        }
    }

    /**
     * Invoked when a key has been released.
     */
    public void keyReleased(KeyEvent ke) {
        Globals.curEditor(this);
        if (_canSelectElements) {
            _selectionManager.keyReleased(ke);
            _modeManager.keyReleased(ke);
        }
    }

    // //////////////////////////////////////////////////////////////
    // Command-related methods
    /**
     * The editor acts as a shell for Cmds. This method executes the given Cmd
     * in response to the given event (some Cmds look at the Event that invoke
     * them, even though this is discouraged). The Editor executes the Cmd in a
     * safe environment so that buggy actions cannot crash the whole Editor.
     */
    public void executeCmd(Cmd c, InputEvent ie) {
        if (c == null) {
            return;
        }
        try {
            c.doIt();
        } catch (java.lang.Throwable ex) {
            LOG.debug("While executing " + c + " on event " + ie
                    + " the following error occured:", ex);
        }
    }

    // //////////////////////////////////////////////////////////////
    // notifications and updates
    /**
     * The given Fig was removed from the diagram this Editor is showing. Now
     * update the display.
     */
    public void removed(Fig f) {
        _selectionManager.deselect(f);
        remove(f);
    }

    public void setShouldPaint(boolean shouldPaint) {
        _shouldPaint = shouldPaint;
    }

    public boolean shouldPaint() {
        return _shouldPaint;
    }

    /**
     * Gets the selection object the mouse is in
     *
     * @return the selection object or null
     */
    public Selection getCurrentSelection() {
        return _curSel;
    }
}
