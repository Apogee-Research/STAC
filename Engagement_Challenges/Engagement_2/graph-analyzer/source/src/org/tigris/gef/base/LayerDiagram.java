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
// File: LayerDiagram.java
// Classes: LayerDiagram
// Original Author: jrobbins@ics.uci.edu
// $Id: LayerDiagram.java 1203 2008-12-20 19:20:01Z bobtarling $
package org.tigris.gef.base;

import graph.Node;
import graphviz.CircleNode;
import graphviz.TestCircleMath;
import graphviz.TestCircleMath.ScaledRectangle;
import java.awt.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Iterator;

import org.graph.commons.logging.Log;
import org.graph.commons.logging.LogFactory;
import org.tigris.gef.persistence.ScalableGraphics;
import org.tigris.gef.presentation.Fig;
import org.tigris.gef.presentation.FigCircle;
import org.tigris.gef.presentation.FigNode;
import org.tigris.gef.presentation.FigPainter;

/**
 * A Layer like found in many drawing applications. It contains a collection of
 * Fig's, ordered from back to front. Each LayerDiagram contains part of the
 * overall picture that the user is drawing. Needs-More-Work: eventually add a
 * "Layers" menu to the Editor. <A HREF="../features.html#graph_visualization">
 * <TT>FEATURE: graph_visualization</TT></A>
 */
public class LayerDiagram extends Layer {

    private static final long serialVersionUID = 6193765162314431069L;

    /**
     * The Fig's that are contained in this layer.
     */
    public List<Fig> contents = new ArrayList<Fig>();

    /**
     * A counter so that layers have default names like 'One', 'Two', ...
     */
    private static int nextLayerNumbered = 1;

    private static final Log LOG = LogFactory.getLog(LayerDiagram.class);

    /**
     * Construct a new LayerDiagram with a default name and do not put it on the
     * Layer's menu.
     */
    public LayerDiagram() {
        this("Layer" + numberWordFor(nextLayerNumbered++));
    }

    /**
     * Construct a new LayerDiagram with the given name, and add it to the menu
     * of layers. Needs-More-Work: I have not implemented a menu of layers yet.
     * I don't know if that is really the right user interface.
     */
    public LayerDiagram(String name) {
        super(name);
        setOnMenu(true);
    }

    public Enumeration elements() {
        return Collections.enumeration(contents);
    }

    /**
     * A utility function to give the spelled-out word for numbers.
     */
    protected static String numberWordFor(int n) {
        switch (n) {

            case 1:
                return "One";

            case 2:
                return "Two";

            case 3:
                return "Three";

            case 4:
                return "Four";

            case 5:
                return "Five";

            case 6:
                return "Six";

            case 7:
                return "Seven";

            case 8:
                return "Eight";

            case 9:
                return "Nine";

            default:
                return "Layer " + n;
        }
    }

    // //////////////////////////////////////////////////////////////
    // accessors
    /**
     * Add a Fig to the contents of this layer. Items are added on top of all
     * other items.
     *
     * @param f the fig to add
     * @throws IllegalArgumentException if the fig is null
     */
    public void add(Fig f) {
        if (f == null) {
            throw new IllegalArgumentException(
                    "Attempted to add a null fig to a LayerDiagram");
        }

        contents.remove(f); // act like a set
        contents.add(f);
        f.setLayer(this);
        f.endTrans();
    }

    /**
     * Add a Fig to the contents of this layer. Items are added on top of all
     * other items.
     *
     * @param f the fig to insert
     * @throws IllegalArgumentException if the fig is null
     */
    public void insertAt(Fig f, int index) {
        if (f == null) {
            throw new IllegalArgumentException(
                    "Attempted to insert a null fig to a LayerDiagram");
        }

        contents.remove(f); // act like a set
        contents.add(index, f);
        f.setLayer(this);
        f.endTrans();
    }

    /**
     * Add a Fig to the contents of this layer. Items are added on top of all
     * other items.
     *
     * @param f the fig to insert
     * @throws IllegalArgumentException if the fig is null
     */
    public int indexOf(Fig f) {
        if (f == null) {
            throw new IllegalArgumentException(
                    "Attempted to find the index of a null fig in a LayerDiagram");
        }

        return contents.indexOf(f);
    }

    /**
     * Remove the given Fig from this layer.
     */
    public void remove(Fig f) {
        contents.remove(f);
        f.endTrans();
        f.setLayer(null);
    }

    /**
     * Test if the given Fig is in this layer.
     *
     * @param f
     * @return
     */
    public boolean contains(Fig f) {
        return contents.contains(f);
    }

    /**
     * Reply the contents of this layer.
     */
    public List<Fig> getContents() {
        return Collections.unmodifiableList(contents);
    }

    /**
     * Reply the contents of this layer that are of the given type.
     *
     * @param figClass the type of Figs required
     * @return the figs
     */
    public List<? extends Fig> getContents(Class<? extends Fig> figClass) {
        List<Fig> list = new ArrayList<Fig>(contents.size());
        for (Fig f : contents) {
            if (f.getClass().isAssignableFrom(figClass)) {
                list.add(f);
            }
        }
        return Collections.unmodifiableList(list);
    }

    /**
     * Reply the 'top' Fig under the given (mouse) coordinates. Needs-More-Work:
     * For now, just do a linear search. Later, optimize this routine using Quad
     * Trees (or other) techniques.
     */
    public Fig hit(Rectangle r) {

        /* search backward so that highest item is found first */
        for (int i = contents.size() - 1; i >= 0; i--) {
            Fig f = (Fig) contents.get(i);
            if (f.hit(r)) {
                return f;
            }
        }

        return null;
    }

    /**
     * Delete all Fig's from this layer.
     */
    public void removeAll() {
        for (int i = contents.size() - 1; i >= 0; i--) {
            Fig f = (Fig) contents.get(i);
            f.setLayer(null);
        }

        contents.clear();
        // notify?
    }

    /**
     * Find the FigNode that is being used to visualize the given NetPort, or
     * null if there is none in this layer.
     */
    public FigNode getPortFig(Object port) {
        for (Fig f : getContents()) {
            if (f instanceof FigNode) {
                FigNode fn = (FigNode) f;
                Fig port_fig = fn.getPortFig(port);
                if (port_fig != null) {
                    return fn;
                }
            }
        }

        return null;
    }

    /**
     * Find the Fig that visualise the given model element in this layer, or
     * null if there is none.
     */
    public Fig presentationFor(Object obj) {
        int figCount = contents.size();
        for (int figIndex = 0; figIndex < figCount; ++figIndex) {
            Fig fig = (Fig) contents.get(figIndex);
            if (fig.getOwner() == obj) {
                return fig;
            }
        }

        return null;
    }

    /**
     * Find the all Figs that visualise the given model element in this layer,
     * or null if there is none.
     */
    public List presentationsFor(Object obj) {
        ArrayList presentations = new ArrayList();
        int figCount = contents.size();
        for (int figIndex = 0; figIndex < figCount; ++figIndex) {
            Fig fig = (Fig) contents.get(figIndex);
            if (fig.getOwner() == obj) {
                presentations.add(fig);
            }
        }

        return presentations;
    }

    public int presentationCountFor(Object obj) {
        int count = 0;
        int figCount = contents.size();
        for (int figIndex = 0; figIndex < figCount; ++figIndex) {
            Fig fig = (Fig) contents.get(figIndex);
            if (fig.getOwner() == obj) {
                count++;
            }
        }

        return count;
    }

    // //////////////////////////////////////////////////////////////
    // painting methods
    /**
     * Paint all the Fig's that belong to this layer.
     */
    public void paintContents(Graphics g) { // kept for backwards compatibility
        paintContents(g, null);
    }

    public void paintContents(Graphics g, FigPainter painter) {
        paintContents(g, null, 0, 0, 1);
    }

    /**
     * Paint all the Fig's that belong to this layer using a given FigPainter.
     * If painter is null, the Fig's are painted directly.
     */
    public void paintContents(Graphics g, FigPainter painter, int x, int y, int scale) {
        paintContents(g, painter, x, y, scale, null);
    }

    /**
     * Paint all the Fig's that belong to this layer using a given FigPainter.
     * If painter is null, the Fig's are painted directly.
     */
    public void paintContents(Graphics g, FigPainter painter, int x, int y, int scale, Fig parent) {

        Rectangle clipBounds = g.getClipBounds();
        Iterator<Fig> figsIter;
        synchronized (contents) {
            figsIter = (new ArrayList<Fig>(contents)).iterator();
        }

        if (g instanceof ScalableGraphics) {
            ScalableGraphics sg = (ScalableGraphics) g;
            //sg.setScale(1%scale);
        }

        while (figsIter.hasNext()) {
            Fig fig = (Fig) figsIter.next();

            Object owner = fig.getOwner();
            if (owner instanceof Node) {
                Node n = (Node) owner;

                org.graph.commons.logging.LogFactory.getLog(null).info("n:" + n.name);
                String type = n.getType();
                org.graph.commons.logging.LogFactory.getLog(null).info("--0");

                fig.setLineColor(n.color);
                /*if (type != null && type.startsWith("container:")) {
                 org.graph.commons.logging.LogFactory.getLog(null).info("0.1");
                 String containername = type.substring("container:".length());

                 LayerDiagram ldg = (LayerDiagram)Globals.curEditor().getLayerManager().getLayer(containername);
                    
                 ldg.paintContents(g, painter, x,  y,  scale + 1);
                    
                 //Graph subg = graphs.get(containername);
                 org.graph.commons.logging.LogFactory.getLog(null).info("1");

                 //write( subg, os);
                 }*/

            }

            if (clipBounds == null || fig.intersects(clipBounds)) {
                if (painter == null) {

                    fig.paint(g);
                } else {
                    painter.paint(g, fig);
                }
            }
        }
        if (g instanceof ScalableGraphics) {
            ScalableGraphics sg = (ScalableGraphics) g;
            //sg.setScale(scale);
        }
    }

    /**
     * Paint all the Fig's that belong to this layer using a given FigPainter.
     * If painter is null, the Fig's are painted directly.
     */
    public void paintContentsAlmostDefault(Graphics g, FigPainter painter, int x, int y, int scale, Fig parent) {
        Rectangle clipBounds = g.getClipBounds();
        Iterator<Fig> figsIter;
        synchronized (contents) {
            figsIter = (new ArrayList<Fig>(contents)).iterator();
        }

        if (g instanceof ScalableGraphics) {
            ScalableGraphics sg = (ScalableGraphics) g;
            //sg.setScale(1%scale);
        }

        while (figsIter.hasNext()) {
            Fig fig = (Fig) figsIter.next();

            Object owner = fig.getOwner();
            if (owner instanceof Node) {
                Node n = (Node) owner;

                org.graph.commons.logging.LogFactory.getLog(null).info("n:" + n.name);
                String type = n.getType();
                org.graph.commons.logging.LogFactory.getLog(null).info("--0");

                g.setColor(n.color);
                fig.setFillColor(n.color);
                fig.setLineColor(n.color);
                /*if (type != null && type.startsWith("container:")) {
                 org.graph.commons.logging.LogFactory.getLog(null).info("0.1");
                 String containername = type.substring("container:".length());

                 LayerDiagram ldg = (LayerDiagram)Globals.curEditor().getLayerManager().getLayer(containername);
                    
                 ldg.paintContents(g, painter, x,  y,  scale + 1);
                    
                 //Graph subg = graphs.get(containername);
                 org.graph.commons.logging.LogFactory.getLog(null).info("1");

                 //write( subg, os);
                 }*/

            }

            if (clipBounds == null || fig.intersects(clipBounds)) {
                if (painter == null) {

                    fig.paint(g);
                } else {
                    painter.paint(g, fig);
                }
            }
        }
        if (g instanceof ScalableGraphics) {
            ScalableGraphics sg = (ScalableGraphics) g;
            //sg.setScale(scale);
        }
    }

    /**
     * Paint all the Fig's that belong to this layer using a given FigPainter.
     * If painter is null, the Fig's are painted directly.
     */
    public void paintContentsPNG(Graphics g, FigPainter painter, int x, int y, int scale, Fig parent) {
        Rectangle clipBounds = g.getClipBounds();
        Iterator<Fig> figsIter;
        synchronized (contents) {
            figsIter = (new ArrayList<Fig>(contents)).iterator();
        }

        if (g instanceof ScalableGraphics) {
            ScalableGraphics sg = (ScalableGraphics) g;
            //sg.setScale(1%scale);
        }

        while (figsIter.hasNext()) {
            Fig fig = (Fig) figsIter.next();

            Object owner = fig.getOwner();
            if (owner instanceof Node) {
                Node n = (Node) owner;

                org.graph.commons.logging.LogFactory.getLog(null).info("n:" + n.name);
                String type = n.getType();
                org.graph.commons.logging.LogFactory.getLog(null).info("--0");

                /*if (type != null && type.startsWith("container:")) {
                 org.graph.commons.logging.LogFactory.getLog(null).info("0.1");
                 String containername = type.substring("container:".length());

                 LayerDiagram ldg = (LayerDiagram)Globals.curEditor().getLayerManager().getLayer(containername);
                    
                 ldg.paintContents(g, painter, x,  y,  scale + 1);
                    
                 //Graph subg = graphs.get(containername);
                 org.graph.commons.logging.LogFactory.getLog(null).info("1");

                 //write( subg, os);
                 }*/
            }

            if (clipBounds == null || fig.intersects(clipBounds)) {
                if (painter == null) {

                    fig.paint(g);
                } else {
                    painter.paint(g, fig);
                }
            }
        }
        if (g instanceof ScalableGraphics) {
            ScalableGraphics sg = (ScalableGraphics) g;
            //sg.setScale(scale);
        }
    }

    public void paintContentsX(Graphics g, FigPainter painter, int x, int y, int level, Fig parent) {

        //Debug information
        //org.graph.commons.logging.LogFactory.getLog(null).info("x:" + x + "y:" + y + "scale:" + scale);// + " parent:" + parent.getId());
        Rectangle clipBounds = g.getClipBounds();

        //Rectangle drawingArea = calcDrawingArea();
        TestCircleMath.ScaledRectangle scaledDrawingArea = new TestCircleMath.ScaledRectangle(clipBounds, true);
        if (parent != null) {
            if (parent instanceof FigCircle) {
                scaledDrawingArea = TestCircleMath.getInnerBounds(scaledDrawingArea, (FigCircle) parent);
            }
        }
        //org.graph.commons.logging.LogFactory.getLog(null).info("scaledDrawingArea area -- h | w:" + scaledDrawingArea.getX() + "|" + scaledDrawingArea.getY() + "|" + scaledDrawingArea.getHeight() + " | " + scaledDrawingArea.getWidth());

        Iterator<Fig> figsIter;
        synchronized (contents) {
            figsIter = (new ArrayList<Fig>(contents)).iterator();
        }

        while (figsIter.hasNext()) {
            Fig fig = (Fig) figsIter.next();

            fig.setHeight(150);
            fig.setWidth(150);

            Object owner = fig.getOwner();
            Node n = null;
            if (owner instanceof Node) {
                n = (Node) owner;
            } else {
                continue;
            }

            String type = n.getType();

            fig.setX((int) n.x);
            fig.setY((int) n.y);
            fig.setHeight((int) n.h);
            fig.setWidth((int) n.w);

                //org.graph.commons.logging.LogFactory.getLog(null).info("drawing area -- h | w:" + drawingArea.getX()+"|"+ drawingArea.getY() + "|"+drawingArea.getHeight()+ " | "+drawingArea.getWidth());
            //org.graph.commons.logging.LogFactory.getLog(null).info("clipBounds area -- h | w:" + clipBounds.getX() + "|" + clipBounds.getY() + "|" + clipBounds.getHeight() + " | " + clipBounds.getWidth());
            if (parent != null) {
                    //org.graph.commons.logging.LogFactory.getLog(null).info("parent x:" + parent.getX());
                //org.graph.commons.logging.LogFactory.getLog(null).info("parent y:" + parent.getY());
                //org.graph.commons.logging.LogFactory.getLog(null).info("parent h | w:" + parent.getHeight() + " | " + parent.getWidth());
            }

            /*
             ScaledRectangle brect1 = new ScaledRectangle(0, 0, 1255, 1255);
             ScaledRectangle newrect1 = TestCircleMath.getInnerBounds( brect1, cs);
             CircleNode cs1 = new CircleNode(Color.RED);
             cs1.x = 108;
             cs1.y = 54;
             cs1.size = 150;
             printScaledFig(cs1, newrect1);
             */
            if (parent != null) {
                fig.scale = parent.scale;
            } else {
                fig.scale = 1;
            }

            org.graph.commons.logging.LogFactory.getLog(null).info("name:" + n.name + " scale:" + fig.scale);
            org.graph.commons.logging.LogFactory.getLog(null).info("x:" + fig.getX());
            org.graph.commons.logging.LogFactory.getLog(null).info("y:" + fig.getY());
            org.graph.commons.logging.LogFactory.getLog(null).info("h | w:" + fig.getHeight() + " | " + fig.getWidth());

            org.graph.commons.logging.LogFactory.getLog(null).info("scaled draw area x:" + scaledDrawingArea.x + " y:" + scaledDrawingArea.y);

            //if (clipBounds == null || fig.intersects(clipBounds)) {
            if (g instanceof ScalableGraphics) {
                ScalableGraphics sg = (ScalableGraphics) g;
                double inversescale = (double) 1 / (double) scaledDrawingArea.scale;
                sg.setScale(inversescale);
            }
            g.setColor(n.color);
            if (painter == null) {

                if (parent != null) {
                    //n.rep.fill

                    fig.scale = scaledDrawingArea.scale;
                    fig.setX(scaledDrawingArea.x + fig.getX());
                    fig.setY(scaledDrawingArea.y + fig.getY());
                } else {
                    fig.scale = 1;
                }
                g.setColor(n.color);
                fig.paint(g);
            } else {
                //System.out.print("" + (childdrawspace.x + ns.getX()) + " -" + (childdrawspace.y + ns.getY()) + " " + ns.getHeight() + " -" + ns.getWidth() + " 0 360 ");
                //Set the scale!!! on the circle
                fig.scale = scaledDrawingArea.scale;
                fig.setX(scaledDrawingArea.x + fig.getX());
                fig.setY(scaledDrawingArea.y + fig.getY());

                org.graph.commons.logging.LogFactory.getLog(null).info("painter.paint");
                painter.paint(g, fig);
            }

            if (g instanceof ScalableGraphics) {
                ScalableGraphics sg = (ScalableGraphics) g;
                sg.setScale(scaledDrawingArea.scale);
            }
            org.graph.commons.logging.LogFactory.getLog(null).info("-----------------------------" + level);

            if (type != null && type.startsWith("container:")) {
                    //Get the drawing area

                //org.graph.commons.logging.LogFactory.getLog(null).info("0.1");
                String containername = type.substring("container:".length());

                LayerDiagram ldg = (LayerDiagram) Globals.curEditor().getLayerManager().getLayer(containername);

                ldg.paintContents(g, painter, x, y, level + 1, fig);

                    //Graph subg = graphs.get(containername);
                //org.graph.commons.logging.LogFactory.getLog(null).info("1");
                //write( subg, os);
            }
            //}
        }

    }

    // //////////////////////////////////////////////////////////////
    // ordering of Figs
    /**
     * Reorder the given Fig in this layer.
     */
    public void sendToBack(Fig f) {
        contents.remove(f);
        contents.add(0, f);
    }

    /**
     * Reorder the given Fig in this layer.
     */
    public void bringToFront(Fig f) {
        contents.remove(f);
        contents.add(f);
    }

    /**
     * Reorder the given Fig in this layer. Needs-more-work: Should come
     * backward/forward until they change positions with an object they overlap.
     * Maybe...
     */
    public void sendBackward(Fig f) {
        int i = contents.indexOf(f);
        if (i == -1 || i == 0) {
            return;
        }

        final Fig prevFig = contents.get(i - 1);
        contents.set(i, prevFig);
        contents.set(i - 1, f);
    }

    /**
     * Reorder the given Fig in this layer.
     */
    public void bringForward(Fig f) {
        int i = contents.indexOf(f);
        if (i == -1 || i == contents.size() - 1) {
            return;
        }

        final Fig nextFig = this.contents.get(i + 1);
        contents.set(i, nextFig);
        contents.set(i + 1, f);
    }

    /**
     * Reorder the given Fig in this layer.
     */
    public void bringInFrontOf(Fig f1, Fig f2) {
        int i1 = this.contents.indexOf(f1);
        int i2 = this.contents.indexOf(f2);
        if (i1 == -1) {
            return;
        }

        if (i2 == -1) {
            return;
        }

        if (i1 >= i2) {
            return;
        }

        this.contents.remove(f1);
        this.contents.add(i2, f1);
        // Object frontFig = this.contents.elementAt(i1);
        // Object backFig = this.contents.elementAt(i2);
        // this.contents.setElementAt(frontFig, i2);
        // this.contents.setElementAt(backFig, i1);
    }

    /**
     * Reorder the given Fig in this layer.
     */
    public void reorder(Fig f, int function) {
        switch (function) {

            case ReorderAction.SEND_TO_BACK:
                sendToBack(f);
                break;

            case ReorderAction.BRING_TO_FRONT:
                bringToFront(f);
                break;

            case ReorderAction.SEND_BACKWARD:
                sendBackward(f);
                break;

            case ReorderAction.BRING_FORWARD:
                bringForward(f);
                break;
        }
    }

    public void preSave() {
        validate();
        for (int i = 0; i < this.contents.size(); i++) {
            Fig f = (Fig) this.contents.get(i);
            f.preSave();
        }
    }

    /**
     * Scan the contents of the layer before a save takes place to validate its
     * state is legal.
     */
    private boolean validate() {
        for (int i = this.contents.size() - 1; i >= 0; --i) {
            Fig f = (Fig) this.contents.get(i);
            if (f.isRemoveStarted()) {
                // TODO: Once JRE1.4 is minimum support we should use assertions
                LOG.error("A fig has been found that should have been removed "
                        + f.toString());
                this.contents.remove(i);
                return false;
            } else if (f.getLayer() != this) {
                // TODO: Once JRE1.4 is minimum support we should use assertions
                LOG
                        .error("A fig has been found that doesn't refer back to the correct layer "
                                + f.toString() + " - " + f.getLayer());
                f.setLayer(this);
                return false;
            }
        }
        return true;
    }

    public void postSave() {
        for (int i = 0; i < this.contents.size(); i++) {
            ((Fig) this.contents.get(i)).postSave();
        }
    }

    public void postLoad() {
        for (int i = 0; i < this.contents.size(); i++) {
            ((Fig) this.contents.get(i)).postLoad();
        }
    }
}
