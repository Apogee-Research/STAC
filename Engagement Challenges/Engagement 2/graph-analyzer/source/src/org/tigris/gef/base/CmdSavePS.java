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
// File: CmdSavePS.java
// Classes: CmdSavePS
// Original Author: wienberg@informatik.uni-hamburg.de
package org.tigris.gef.base;

import graph.Edge;
import graph.Node;
import graphviz.TestCircleMath;
import java.awt.Graphics;
import java.io.*;
import java.awt.Rectangle;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import org.tigris.gef.persistence.*;
import org.tigris.gef.presentation.Fig;
import org.tigris.gef.presentation.FigCircle;
import org.tigris.gef.presentation.FigLine;
import org.tigris.gef.presentation.FigPainter;

/**
 * Cmd to save a diagram as PostScript in a supplied OutputStream. Requires the
 * CH.ifa.draw.util.PostscriptWriter class. Operates on the diagram in the
 * current editor.
 *
 * Code loosely adapted from CmdSaveGIF.
 *
 * in 0.12.3 use SavePSAction
 *
 * @author Frank Wienberg, wienberg@informatik.uni-hamburg.de
 */
public class CmdSavePS extends CmdSaveGraphics {

    private static final long serialVersionUID = 2283867876416499471L;

    public CmdSavePS() {
        super("SavePostScript");
    }

    protected void saveGraphics(OutputStream s, Editor ce, Rectangle drawingArea)
            throws IOException {
        PostscriptWriter ps = new PostscriptWriter(s);
        ps.translate(32, 32 + 778);
        //Just a bunch of code for setting up the drawing area in PostScript
        double scale = Math.min(535.0 / drawingArea.width,
                778.0 / drawingArea.height);
        if (scale < 1.0) {
            ps.scale(scale, scale);
        }
        ps.translate(-drawingArea.x, -drawingArea.y);
        ps.setClip(drawingArea.x, drawingArea.y, drawingArea.width,
                drawingArea.height);
        //Tell the Editor to print out it graphics to a PS file
        //Pass in a PostScriptWriter Object and an implmenentation of SpecialGraphics
        //the Special graphics implementation is like a Closure. It provides a specific implmentation to a class that does something more general.
        //In this case it passes the Graphics printing class a specific means for printing the Hierarchically organized layers of the dot graphs.
        ce.print(new SpecialGraphics(), ps);
        ps.dispose();
    }

    public class SpecialGraphics implements GraphicsSpecial {

//Collection to store edges until both sides of the edge have been processed
        Map<Edge, CompleteEdge> edgemap = new HashMap<Edge, CompleteEdge>();

        public class CompleteEdge {

            Edge edge;
            FigCircle in;
            FigCircle out;

            public CompleteEdge(Edge edge) {
                this.edge = edge;
            }

            public void addInNode(FigCircle in) {
                this.in = in;
            }

            public void addOutNode(FigCircle out) {
                this.out = out;
            }

            boolean isComplete() {

                if (in != null && out != null && edge != null) {
                    return true;
                }
                return false;
            }

            /**
             * Draws the edge from the center of the in and out nodes
             *
             * @param g
             * @param painter
             */
            void drawIt(Graphics g, FigPainter painter) {
                Ellipse2D.Float inshape = (Ellipse2D.Float) in.getShape();
                Ellipse2D.Float outshape = (Ellipse2D.Float) out.getShape();
                //PAINT IT
                int centerXin = (int) inshape.getCenterX();
                int centerYin = (int) inshape.getCenterY();

                int centerXout = (int) outshape.getCenterX();
                int centerYout = (int) outshape.getCenterY();

                FigLine edgeline = new FigLine(centerXin, centerYin, centerXout, centerYout);

                if (painter == null) {
                    edgeline.paint(g);
                } else {
                    painter.paint(g, edgeline);
                }
            }

        }

        @Override
        public void paintContents(LayerDiagram layer, Graphics g, FigPainter painter) {
            paintContents(layer, g, painter, 0, 0, 1, null);
        }

        public void paintContents(LayerDiagram layer, Graphics g, FigPainter painter, int x, int y, int level, Fig parent) {

            //Get the rectangle that serves as the drawing area -- it's corrdinates are boundaries for drawing in the PS file
            Rectangle clipBounds = g.getClipBounds();

            //Bounded Rectangle to draw into -- this just performs simple math on the rectangle (just enforces that it is a square at this point )
            TestCircleMath.ScaledRectangle scaledDrawingArea = new TestCircleMath.ScaledRectangle(clipBounds, true);
            if (parent != null) {
                if (parent instanceof FigCircle) {
                    //This drawing area has a parent!! The parent is a graph node and is a circle. 
                    //The drawing rectangle must fit inside this circle.  This requires calculating the square inside the circle and then scaling the 
                    //drawing area to match the rectangle in the parent
                    scaledDrawingArea = TestCircleMath.getInnerBounds(scaledDrawingArea, (FigCircle) parent);
                }
            }
            //Commented out debug stuff, just ignore
            //org.graph.commons.logging.LogFactory.getLog(null).info("scaledDrawingArea area -- h | w:" + scaledDrawingArea.getX() + "|" + scaledDrawingArea.getY() + "|" + scaledDrawingArea.getHeight() + " | " + scaledDrawingArea.getWidth());

            //get all the Figures (Shapes) in this layer
            Iterator<Fig> figsIter;

            figsIter = (new ArrayList<Fig>(layer.contents)).iterator();

            //Iterate through all the Figures (Shapes) in this layer
            while (figsIter.hasNext()) {
                Fig fig = figsIter.next();

                //The owner is the Node populated by the Dot parser
                Object owner = fig.getOwner();
                Node n = null;
                if (owner instanceof Node) {
                    n = (Node) owner;
                } else {
                    continue;
                }
//These next two loops dealwith drawing the edges, 
                //stores the edeg and the current node in a temporary places. 
                //Once all nodes involved are foud, the edge is drawn
                {
                    Vector<Edge> edges = n.in;
                    Iterator<Edge> edgeerator = edges.iterator();
                    while (edgeerator.hasNext()) {
                        Edge edge = edgeerator.next();
                        SpecialGraphics.CompleteEdge cedge = edgemap.get(edge);
                        if (cedge == null) {
                            cedge = new SpecialGraphics.CompleteEdge(edge);
                            edgemap.put(edge, cedge);
                        }
                        cedge.addInNode((FigCircle) fig);
                    }
                }
                {
                    Vector<Edge> edges = n.out;
                    Iterator<Edge> edgeerator = edges.iterator();
                    while (edgeerator.hasNext()) {
                        Edge edge = edgeerator.next();
                        SpecialGraphics.CompleteEdge cedge = edgemap.get(edge);
                        if (cedge == null) {
                            cedge = new SpecialGraphics.CompleteEdge(edge);
                            edgemap.put(edge, cedge);
                        }
                        cedge.addOutNode((FigCircle) fig);
                    }
                }
                //The owner should not be null, or this will crash, not robust, but ok
                String type = n.getType();

                //Transfer the coordinates and sizing onfo to the figure for use by Java to display
                //Do it here instead of parser in case any scaling is added
                fig.setX((int) n.x);
                fig.setY((int) n.y);
                fig.setHeight((int) n.h);
                fig.setWidth((int) n.w);

                //Some commented out debugging
                //org.graph.commons.logging.LogFactory.getLog(null).info("drawing area -- h | w:" + drawingArea.getX()+"|"+ drawingArea.getY() + "|"+drawingArea.getHeight()+ " | "+drawingArea.getWidth());
                //org.graph.commons.logging.LogFactory.getLog(null).info("clipBounds area -- h | w:" + clipBounds.getX() + "|" + clipBounds.getY() + "|" + clipBounds.getHeight() + " | " + clipBounds.getWidth());
                //if (parent != null) {
                //Some commented out debugging
                //org.graph.commons.logging.LogFactory.getLog(null).info("parent x:" + parent.getX());
                //org.graph.commons.logging.LogFactory.getLog(null).info("parent y:" + parent.getY());
                //org.graph.commons.logging.LogFactory.getLog(null).info("parent h | w:" + parent.getHeight() + " | " + parent.getWidth());
                //}
                //Set the scale, if there was a parent, get the scale of the parent, else 1 for main layer items
                if (parent != null) {
                    fig.scale = parent.scale;
                } else {
                    fig.scale = 1;
                }

                //org.graph.commons.logging.LogFactory.getLog(null).info("name:" + n.name + " scale:" + fig.scale);
                //org.graph.commons.logging.LogFactory.getLog(null).info("x:" + fig.getX());
                //org.graph.commons.logging.LogFactory.getLog(null).info("y:" + fig.getY());
                //org.graph.commons.logging.LogFactory.getLog(null).info("h | w:" + fig.getHeight() + " | " + fig.getWidth());
                //org.graph.commons.logging.LogFactory.getLog(null).info("scaled draw area x:" + scaledDrawingArea.x + " y:" + scaledDrawingArea.y);
                //Perform scalng calculation for PS. 
                //PS is a stack language so set the scale factor here before printing the type
                if (g instanceof ScalableGraphics) {
                    ScalableGraphics sg = (ScalableGraphics) g;
                    double inversescale = (double) 1 / (double) scaledDrawingArea.scale;
                    sg.setScale(inversescale);
                }

                //Set up painting criteria (consider supporting fill color as well -- 
                //this just does the circle's line)
                g.setColor(n.color);
                //n.rep.fill

                if (painter == null) {
                    if (parent != null) {
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
                    //Set the scale!!! on the circle, this will be how the scale is passed on when this circle becomes a parent
                    //figure for a subgraph
                    fig.scale = scaledDrawingArea.scale;
                    //Set the x and y by adding the scaled offset to make the new circle draw in the parent circle
                    fig.setX(scaledDrawingArea.x + fig.getX());
                    fig.setY(scaledDrawingArea.y + fig.getY());

                    //Draw the circle out to the PS file
                    painter.paint(g, fig);
                }

                Iterator<SpecialGraphics.CompleteEdge> edgeerator = edgemap.values().iterator();
                while (edgeerator.hasNext()) {
                    SpecialGraphics.CompleteEdge next = edgeerator.next();
                    if (next.isComplete()) {
                        next.drawIt(g, painter);
                        edgeerator.remove();
                    }
                }

                //Perform the reverse scalng calculation for PS. 
                //PS is a stack language so set the scale factor here before printing the type
                if (g instanceof ScalableGraphics) {
                    ScalableGraphics sg = (ScalableGraphics) g;
                    sg.setScale(scaledDrawingArea.scale);
                }
                //Denug seperating the steps
                //org.graph.commons.logging.LogFactory.getLog(null).info("-----------------------------" + level);

                //Here it is!!! The exploitable code
                //Get the Node attribute information set by the Dot parser
                //In particular, get the 'container' type data
                //When the type is 'container', it means that this Node has a whole other graph inside of it
                if (type != null && type.startsWith("container:")) {

                    String containername = type.substring("container:".length());

                    //Get the layer object associated with this child graph
                    LayerDiagram ldg = (LayerDiagram) Globals.curEditor().getLayerManager().getLayer(containername);

                    //Recursively call and print out this child graph.
                    paintContents(ldg, g, painter, x, y, level + 1, fig);
                }
            }
        }
    }
} /* end class CmdSavePS */
