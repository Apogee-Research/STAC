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
// File: Layer.java
// Classes: Layer
// Original Author: jrobbins@ics.uci.edu
// $Id: Layer.java 1290 2010-09-14 00:42:28Z bobtarling $
package org.tigris.gef.base;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.tigris.gef.di.GraphEdge;
import org.tigris.gef.di.GraphElement;
import org.tigris.gef.di.GraphNode;
import org.tigris.gef.di.DiagramElement;
import org.tigris.gef.presentation.Fig;
import org.tigris.gef.presentation.FigEdge;
import org.tigris.gef.presentation.FigPainter;
import org.tigris.gef.util.EnumerationEmpty;
import org.tigris.gef.util.EnumerationPredicate;
import org.tigris.gef.util.PredFigInRect;
import org.tigris.gef.util.PredFigNodeInRect;

/**
 * A Layer is like a drawing layer in high-end drawing applications (e.g.,
 * MacDraw Pro). A Layer is like a sheet of clear plastic that can contain part
 * of the picture being drawn and multiple layers are put on top of each other
 * to make the overall picture. Different layers can be hidden, locked, or
 * grayed out independently. In GEF the Layer class is more abstract than
 * described above. LayerDiagram is a subclass of Layer that does what is
 * described above. Other subclasses of Layer can provide functionality. For
 * example the background drawing grid is a subclass of Layer that computes its
 * display rather than displaying what is stored in a data structure.
 * Generalizing the concept of a layer to handle grids and other computed
 * display features gives more power and allows the framework to be extended in
 * building various applications. For example an application that needs polar
 * coordinates might use LayerPolar, and an application that used a world map
 * might implement LayerMap. But since layers can be composed, the user could
 * put a grid in front of or behind the map.
 * <p>
 *
 * This approach to implementing drawing editors is similar to that described in
 * a published paper: "Using the Multi-Layer Model for Building Interactive
 * Graphical Applications" Fekete, et al. UIST'96. pp. 109-117. GEF might be
 * improved by making it more like the system described in that paper: basically
 * by moving some of the XXXManage functionality into Layers, or merging Layers
 * and Modes.
 *
 * @see LayerDiagram
 * @see LayerPerspective
 * @see LayerGrid
 * @see LayerPolar
 */
public abstract class Layer implements java.io.Serializable {

    // //////////////////////////////////////////////////////////////
    // instance variables
    /**
     * The name of the layer as it should appear in a menu.
     */
    private String name = "aLayer";

    /**
     * The type of FigNodes that should appear in this layer.
     */
    private String type = "aLayer";

    /**
     * Does the user not want to see this Layer right now? Needs-More-Work.
     */
    private boolean hidden = false;

    /**
     * Is this Layer demphasized by making everything in it gray?
     * Needs-More-Work.
     */
    private boolean grayed = false;

    /**
     * Is this Layer locked so that the user can not modify it? Needs-More-Work.
     */
    private boolean locked = false;

    /**
     * Should this layer always stay on top (i.e. always be the active layer)?
     */
    private boolean alwaysOnTop = false;
    /**
     * The current zooming scale this layer is displayed in
     */
    private double scale = 1;

    /**
     * Should the user be able to hide, lock, or gray this layer?
     * Needs-More-Work.
     *
     * use getOnMenu
     */
    protected boolean _onMenu = false;

    /**
     * A list of the Editors that are displaying this Layer. Use addEditor(),
     * removeEditor() and getEditors() to access this.
     */
    private transient List<Editor> editors = new ArrayList<Editor>();

    // //////////////////////////////////////////////////////////////
    // constructors
    /**
     * Construct a new Layer. This abstract class really does nothing in its
     * constructor, but subclasses may have meaningful constructors.
     */
    public Layer() {
    }

    /**
     * Construct a new layer with the given name.
     */
    public Layer(String name) {
        this.name = name;
    }

    /**
     * Construct a new layer with the given name and type.
     */
    public Layer(String name, String type) {
        this.name = name;
        this.type = type;
    }

    // TODO - I'd query whether this is the best way to write a clone
    // method. Why doesn't it call super.clone()? Why is it trying to
    // create it descendants by reflection. The descendants should
    // contain the clone method.
    // Also, shouldn't this class implement clonable if it contains clone?
    // Bob Tarling 29 Dec 2003
    public Object clone() {
        Layer lay;
        try {
            lay = (Layer) (this.getClass().newInstance());
        } catch (java.lang.IllegalAccessException ignore) {
            return null;
        } catch (java.lang.InstantiationException ignore) {
            return null;
        }
        lay.name = this.name;
        lay.type = this.type;
        lay._onMenu = this._onMenu;
        lay.grayed = this.grayed;
        lay.setHidden(this.hidden);
        lay.setGrayed(this.grayed);
        lay.setScale(this.scale);
        lay.setLocked(this.locked);
        lay.setAlwaysOnTop(this.alwaysOnTop);
        return lay;
    }

    // //////////////////////////////////////////////////////////////
    // accessors
    /**
     * Reply a string useful for debugging
     */
    public String toString() {
        return super.toString() + "[" + name + "]";
    }

    /**
     * Get and set methods
     */
    public String getName() {
        if (name == null) {
            return "";
        }
        return name;
    }

    public void setName(String n) {
        name = n;
    }

    public void setHidden(boolean b) {
        hidden = b;
    }

    public boolean getHidden() {
        return hidden;
    }

    public void setGrayed(boolean b) {
        grayed = b;
    }

    public boolean getGrayed() {
        return grayed;
    }

    public void setLocked(boolean b) {
        locked = b;
    }

    public boolean getLocked() {
        return locked;
    }

    public void setAlwaysOnTop(boolean onTop) {
        alwaysOnTop = onTop;
    }

    public boolean isAlwaysOnTop() {
        return alwaysOnTop;
    }

    public void setScale(double scale) {
        this.scale = scale;
    }

    public double getScale() {
        return scale;
    }

    public void setOnMenu(boolean b) {
        _onMenu = b;
    }

    public boolean getOnMenu() {
        return _onMenu;
    }

    /**
     * Get the figs that make up this layer.
     *
     * @return the figs
     */
    abstract public List<Fig> getContents();

    /**
     * Reply the contents of this layer that are of the given type.
     *
     * @param figClass the type of Figs required
     * @return the figs
     */
    public List<? extends Fig> getContents(Class<? extends Fig> figClass) {
        return Collections.emptyList();
    }

    public List<DiagramElement> getDiagramElements() {
        List<Fig> contents = getContents();
        List<DiagramElement> list = new ArrayList<DiagramElement>(contents.size());
        for (Fig f : contents) {
            list.add(f);
        }

        return list;
    }

    public List<GraphEdge> getGraphEdges() {
        List<Fig> contents = getContents();
        List<GraphEdge> list = new ArrayList<GraphEdge>(contents.size() * 8 / 10);
        for (Fig f : contents) {
            if (f instanceof GraphEdge) {
                list.add((GraphEdge) f);
            }
        }

        return list;
    }

    public List<GraphNode> getGraphNodes() {
        List<Fig> contents = getContents();
        List<GraphNode> list = new ArrayList<GraphNode>(contents.size() * 8 / 10);
        for (Fig f : contents) {
            if (f instanceof GraphNode) {
                list.add((GraphNode) f);
            }
        }

        return list;
    }

    /**
     * USED BY SVG.tee && PGML.tee
     *
     * use PgmlUtility.getContentsNoEdges
     */
    public List getContentsNoEdges() {
        List contents = getContents();
        int size = contents.size();
        Vector v = new Vector(size);
        for (int i = 0; i < size; i++) {
            Object o = contents.get(i);
            if (!(o instanceof FigEdge)) {
                v.add(o);
            }
        }
        return v;
    }

    /**
     * USED BY SVG.tee && PGML.tee
     *
     * use PgmlUtility.getContentsEdgesOnly
     */
    public List getContentsEdgesOnly() {
        List contents = getContents();
        int size = contents.size();
        ArrayList list = new ArrayList(size);
        for (int i = 0; i < size; i++) {
            Object o = contents.get(i);
            if (o instanceof FigEdge) {
                list.add(o);
            }
        }
        return list;
    }

    /**
     * Return the list of Editors that are showing this Layer.
     */
    public List<Editor> getEditors() {
        return Collections.unmodifiableList(new ArrayList<Editor>(editors));
    }

    /**
     * Most Layers contain Fig, so I have empty implementations of add, remove,
     * removeAll, elements, and hit.
     *
     * @see LayerDiagram
     */
    public void add(Fig f) {
    }

    public void remove(Fig f) {
    }

    public void add(DiagramElement f) {
        add((Fig) f);
    }

    public void remove(DiagramElement f) {
        remove((Fig) f);
    }

    public void removeAll() {
    }

    /**
     * in 0.13 use getContents and build your own iterator
     */
    public Enumeration elements() {
        return EnumerationEmpty.theInstance();
    }

    public Fig hit(Rectangle r) {
        return null;
    }

    /**
     * Reply an enumeration of all the Figs in this Layer that intersect given
     * Rectangle.
     */
    public Enumeration elementsIn(Rectangle r) {
        return new EnumerationPredicate(elements(), new PredFigInRect(r));
    }

    /**
     * Reply an enumeration of all the FigNodes in this Layer that intersect
     * given Rectangle.
     */
    public Enumeration nodesIn(Rectangle r) {
        return new EnumerationPredicate(elements(), new PredFigNodeInRect(r));
    }

    /**
     * Given an object from the net-level model (e.g., NetNode or NetPort),
     * reply the graphical depiction of that object in this layer, if there is
     * one. Otherwise reply null.
     */
    public abstract Fig presentationFor(Object obj);

    /**
     * Return a string that can be used to make some Layers show nodes in one
     * way and other Layers show the same nodes in a different way. By default
     * just use the name of the layer, but in general names are for users to
     * specify as reminders to themselves and the perspectiveType controls what
     * kinds of node FigNodes will be added to that view.
     */
    public String getPerspectiveType() {
        return type;
    }

    public void setPerspectiveType(String t) {
        type = t;
    }

    /**
     * Most Layers will contain things in back to front order, so I define empty
     * reordering functions here. Subclasses can implement these if appropriate.
     */
    public void sendToBack(Fig f) {
    }

    public void bringForward(Fig f) {
    }

    public void sendBackward(Fig f) {
    }

    public void bringToFront(Fig f) {
    }

    public void bringInFrontOf(Fig f1, Fig f2) {
    }

    public void reorder(Fig f, int function) {
    }

    // //////////////////////////////////////////////////////////////
    // painting methods
    /**
     * Draw the Layer on a PrintGraphics. By default, just calls paint(g).
     */
    public void print(Graphics g) {
        paint(g);
    }

    /**
     * Paint this Layer on the given Graphics. Sublasses should define methods
     * for paintContents, which is called from here if the Layer is not hidden.
     */
    public void paint(Graphics g) { // kept for backwards compatibility
        paint(g, null);
    }

    /**
     * Paint this Layer on the given Graphics using the given FigPainter.
     * Sublasses should define methods for paintContents, which is called from
     * here if the Layer is not hidden.
     */
    public void paint(Graphics g, FigPainter painter) {
        if (hidden) {
            return;
        }

        if (!grayed) {
            paintContents(g, painter);
        } else {
            paintGrayContents(g);
        }

    }

    public void paint(GraphicsSpecial gs, Graphics g, FigPainter painter) {
        if (hidden) {
            return;
        }

        if (this instanceof LayerDiagram) {
            gs.paintContents((LayerDiagram) this, g, painter);
        }
    }

    /**
     * Method to paint the contents of this layer using a given painter. The
     * default implementation ignores the painter.
     */
    public void paintContents(Graphics g, FigPainter painter) {
        paintContents(g);
    }

    /**
     * Abstract method to paint the contents of this layer, subclasses must
     * define this. For example, LayerDiagram paints itself by painting a list
     * of Figs and LayerGrid paints itself by painting a lot lines.
     */
    public abstract void paintContents(Graphics g);

    /**
     * Paint the contents in a dimmed, demphasized way. Calls paintContents.
     * Needs-More-Work: really needs a new kind of Graphics to work right.
     */
    public void paintGrayContents(Graphics g) {
        g.setColor(Color.lightGray);
        // g.lockColor(); // Needs-More-Work: not implemented
        paintContents(g);
        // g.unlockColor(); // Needs-More-Work: not implemented
    }

    public Rectangle getDrawingArea() {

        Rectangle drawingArea = new Rectangle(0, 0, Globals.displaywidth, Globals.displayheight);

        return drawingArea;
    }

    public Rectangle calcDrawingArea() {
        Enumeration iter = elements();
        if (!iter.hasMoreElements()) {
            return new Rectangle();
        }

        Fig f = (Fig) iter.nextElement();
        Rectangle drawingArea = new Rectangle(f.getBounds());
        while (iter.hasMoreElements()) {
            f = (Fig) iter.nextElement();
            drawingArea.add(f.getBounds());
        }

        drawingArea.grow(4, 4); // security border

        return drawingArea;
    }

    // //////////////////////////////////////////////////////////////
    // notifications and updates
    /**
     * A Fig in this Layer has changed state and needs to be redrawn. Notify all
     * Editors showing this Layer that they should record the damage.
     */
    public void damageAll() {
        if (editors == null) {
            return;
        }

        int count = editors.size();
        for (int editorIndex = 0; editorIndex < count; ++editorIndex) {
            Editor editor = (Editor) editors.get(editorIndex);
            editor.damageAll();
        }
    }

    /**
     * A Fig in this Layer has been deleted. Notify all Editors so that they can
     * deselect the Fig.
     */
    public void deleted(Fig f) {
        if (editors == null) {
            return;
        }

        int editorCount = editors.size();
        for (int editorIndex = 0; editorIndex < editorCount; ++editorIndex) {
            Editor editor = (Editor) editors.get(editorIndex);
            editor.removed(f);
        }
    }

    /**
     * Ask all Editors to completely redraw their display.
     */
    public void refreshEditors() {
        if (editors == null) {
            return;
        }
        int editorCount = editors.size();
        for (int editorIndex = 0; editorIndex < editorCount; ++editorIndex) {
            Editor editor = (Editor) editors.get(editorIndex);
            editor.damageAll();
        }
    }

    /**
     * Add an Editor to the list of Editors showing this Layer.
     */
    public void addEditor(Editor ed) {
        if (editors == null) {
            editors = new ArrayList();
        }
        editors.add(ed);
    }

    public void removeEditor(Editor ed) {
        if (editors == null) {
            return;
        }
        editors.remove(ed);
    }

    public void preSave() {
    }

    public void postSave() {
    }

    public void postLoad() {
    }

    // //////////////////////////////////////////////////////////////
    // user interface
    /**
     * Allow the user to edit the properties of this layer (not the properties
     * of the contents of this layer). For example, in LayerGrid this could set
     * the grid size. By default, does nothing.
     *
     * @see LayerGrid
     */
    public void adjust() {
    }

    /**
     * Allow the user to edit the properties of this layer (not the properties
     * of the contents of this layer). For example, in LayerGrid this could set
     * the grid size. By default, does nothing.
     *
     * @see LayerGrid
     * @param map a hashmap with properties
     */
    public void adjust(HashMap map) {
    }

} /* end class Layer */
