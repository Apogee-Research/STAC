//Copyright (c) 2004-2006 The Regents of the University of California. All
//Rights Reserved. Permission to use, copy, modify, and distribute this
//software and its documentation without fee, and without a written
//agreement is hereby granted, provided that the above copyright notice
//and this paragraph appear in all copies.  This software program and
//documentation are copyrighted by The Regents of the University of
//California. The software program and documentation are supplied "AS
//IS", without any accompanying services from The Regents. The Regents
//does not warrant that the operation of the program will be
//uninterrupted or error-free. The end-user understands that the program
//was developed for research purposes and is advised not to rely
//exclusively on the program for any reason.  IN NO EVENT SHALL THE
//UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT,
//SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES, INCLUDING LOST PROFITS,
//ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
//THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
//SUCH DAMAGE. THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY
//WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
//MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
//PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
//CALIFORNIA HAS NO OBLIGATIONS TO PROVIDE MAINTENANCE, SUPPORT,
//UPDATES, ENHANCEMENTS, OR MODIFICATIONS.
//File: CmdSavePNG.java
//Classes: CmdSavePNG
package org.tigris.gef.base;

import graph.Edge;
import graph.Node;
import java.awt.AlphaComposite;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.awt.image.RenderedImage;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.AttributedCharacterIterator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import org.tigris.gef.persistence.ScalableGraphics;
import org.tigris.gef.presentation.Fig;
import org.tigris.gef.presentation.FigCircle;
import org.tigris.gef.presentation.FigLine;
import org.tigris.gef.presentation.FigPainter;
import user.commands.image.CmdDrawPS;
import user.commands.image.DrawingCanvas;

/**
 * This is a rewrite of CmdSaveGIF to use the JDK 1.4 ImageIO library to write
 * PNG files, with both better performance and memory efficiency. Unfortunately
 * though, this is only available to those with JRE1.4 and above.
 *
 * in 0.12.3 use SavePNGAction
 */
public class CmdSavePNG extends CmdSaveGraphics {

    Layer activeOutLayer;
    private static final long serialVersionUID = 2694114560467440132L;

    /**
     * Used as background color in image and set transparent. Chosen because
     * it's unlikely to be selected by the user, and leaves the diagram readable
     * if viewed without transparency.
     */
    public static final int TRANSPARENT_BG_COLOR = 0x00efefef;

    public CmdSavePNG(String fName) {
        super(fName);
    }

    /**
     * Sets up the call to print out the PNG files
     */
    protected void saveGraphics(OutputStream s, Editor ce, Rectangle drawingArea)
            throws IOException {

        //Set up the print process.  This works different than the PS implementation of the same function.
        //In PS we were printing all graphics to one file, so we setup that file then call Editor.print(...)
        //Here, we are printing to many files, so we just setup the starting layer and call the saveGraphicsLayer 
        //function to recursively loop and print files
        activeOutLayer = ce.getLayerManager().getActiveLayer();
        saveGraphicsLayer(s, ce, drawingArea);
    }

    /**
     * This is the function that implements the recursive loop an iterates
     * through the layers with the subgraphs. Each subgraph is printed out.
     * However, unlike PS, this is not an exploitable space attack because
     * repeating graphs overwrite each other. In the PS version
     */
    protected void saveGraphicsLayer(OutputStream s, Editor ce, Rectangle drawingArea)
            throws IOException {

        Iterator<Fig> figsIter = (new ArrayList<Fig>(activeOutLayer.getContents())).iterator();

        //Set up the name for PNG we will be printing here -- PNG name is tied to layer name. 
        //If a layer is encountered again, just overwrite -- MAYBE PUT A SIMPLE CHECK HERE TO SEE IF WE ALREADY PRINTED THIS LAYER
        //A MEMOIZATION STYLE OPTIMIZATION?????
        
        
        String imagef = this.getName() + "-" + activeOutLayer.getName() + ".png";
        try {
            FileOutputStream fos = new FileOutputStream(imagef);
            saveGraphicsInternal(fos, ce, drawingArea);
            fos.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(CmdDrawPS.class.getName()).log(Level.SEVERE, null, ex);
        }

        while (figsIter.hasNext()) {
            Fig fig = (Fig) figsIter.next();

            Object owner = fig.getOwner();
            Node n = null;
            if (owner instanceof Node) {
                n = (Node) owner;
            } else {
                continue;
            }
            //Transfer the coordinates and sizing onfo to the figure for use by Java to display
            //Do it here instead of parser in case any scaling is added
            fig.setX((int) n.x);
            fig.setY((int) n.y);
            fig.setHeight((int) n.h);
            fig.setWidth((int) n.w);

            //Here it is!!! The <b>UN</b>exploitable code
            //Get the Node attribute information set by the Dot parser
            //In particular, get the 'container' type data
            //When the type is 'container', it means that this Node has a whole other graph inside of it  
            String type = n.getType();
            if (type != null && type.startsWith("container:")) {
                String containername = type.substring("container:".length());
                activeOutLayer = ce.getLayerManager().findLayerNamed(containername);

                String imagef2 = this.getName() + "-" + activeOutLayer.getName() + ".png";

                org.graph.commons.logging.LogFactory.getLog(null).info("imagef:" + imagef2);
                try {
                    FileOutputStream fos = new FileOutputStream(imagef2);
                    saveGraphicsInternal(fos, ce, drawingArea);
                    fos.close();
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(CmdDrawPS.class.getName()).log(Level.SEVERE, null, ex);
                }

                //We still recursively loop, like with PS implementation
                saveGraphicsLayer(s, ce, drawingArea);
            }

        }
    }

    /**
     * This is the function that prints out the PNG file from the AWT canvas to
     * a file.
     */
    protected void saveGraphicsInternal(OutputStream s, Editor ce, Rectangle drawingArea)
            throws IOException {
        // Create an offscreen image and render the diagram into it.
        Image i = new BufferedImage(drawingArea.width * scale,
                drawingArea.height * scale, BufferedImage.TYPE_INT_ARGB);
        Graphics graphics = i.getGraphics();
        Graphics2D g = (Graphics2D) graphics;
        g.scale(scale, scale);
        g.setColor(new Color(TRANSPARENT_BG_COLOR, true));
        Composite c = g.getComposite();
        g.setComposite(AlphaComposite.Src);
        g.fillRect(0, 0, drawingArea.width * scale, drawingArea.height * scale);
        g.setComposite(c);

        g.translate(-drawingArea.x, -drawingArea.y);

        //Like with PS implementation. the SpecialGraphics tell the Editor how print out it graphics to a PNG file
        //Pass in a 2D Graphics  Object and an implmenentation of SpecialGraphics
        //the Special graphics implementation is like a Closure. It provides a specific implmentation to a class that does something more general.
        //In this case it passes the Graphics printing class a specific means for printing the figures in a single layer. 
        //In the PS implementation it prints out the entire hierarchy.
        ce.print(new SpecialGraphics(), g);

        /*org.graph.commons.logging.LogFactory.getLog(null).info("<table>");
         for(int ii=stackTrace.length;ii>0;ii--){
        
         org.graph.commons.logging.LogFactory.getLog(null).info("<tr>");
         org.graph.commons.logging.LogFactory.getLog(null).info("<td>");
         org.graph.commons.logging.LogFactory.getLog(null).info("trace step"+"["+(stackTrace.length-ii)+"]:"+stackTrace[ii-1].toString());
         org.graph.commons.logging.LogFactory.getLog(null).info("</td>");
         org.graph.commons.logging.LogFactory.getLog(null).info("<td>");
         org.graph.commons.logging.LogFactory.getLog(null).info("</td>");
         org.graph.commons.logging.LogFactory.getLog(null).info("</tr>");
         }
         org.graph.commons.logging.LogFactory.getLog(null).info("</table>");*/
        //Write the PNG using Java AWT standard built in capability
        ImageIO.write((RenderedImage) i, "png", s);
        
        g.dispose();

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

            //Loop through the edges and draw them
            Iterator<CompleteEdge> edgeerator = edgemap.values().iterator();
            while (edgeerator.hasNext()) {
                CompleteEdge next = edgeerator.next();
                if (next.isComplete()) {
                    next.drawIt(g, painter);
                    edgeerator.remove();
                }
            }
        }
        /*
         * Draws all the graphs
         */

        @Override
        public void paintContents(LayerDiagram layer, Graphics g, FigPainter painter, int x, int y, int scale, Fig parent) {

            Rectangle clipBounds = g.getClipBounds();
            Iterator<Fig> figsIter;

            //Gonna iterate over all the shapes and ask the computing device to draw them
            figsIter = (new ArrayList<Fig>(activeOutLayer.getContents())).iterator();

            if (g instanceof ScalableGraphics) {
                //No scaling needed, since we don't produce a PNG with more than one layer
                //Just leave the code here to emphasize the difference
                ScalableGraphics sg = (ScalableGraphics) g;
            }

            //Iterate through all the Figures (Shapes) in this layer
            while (figsIter.hasNext()) {
                Fig fig = (Fig) figsIter.next();

                //The owner is the Node populated by the Dot parser                
                Object owner = fig.getOwner();
                if (owner instanceof Node) {
                    Node n = (Node) owner;

                    //These next two loops dealwith drawing the edges, 
                    //stores the edeg and the current node in a temporary places. 
                    //Once all nodes involved are foud, the edge is drawn
                    {
                        Vector<Edge> edges = n.in;
                        Iterator<Edge> edgeerator = edges.iterator();
                        while (edgeerator.hasNext()) {
                            Edge edge = edgeerator.next();
                            CompleteEdge cedge = edgemap.get(edge);
                            if (cedge == null) {
                                cedge = new CompleteEdge(edge);
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
                            CompleteEdge cedge = edgemap.get(edge);
                            if (cedge == null) {
                                cedge = new CompleteEdge(edge);
                                edgemap.put(edge, cedge);
                            }
                            cedge.addOutNode((FigCircle) fig);
                        }
                    }

                    //Don't need this, we don't care if it is a container when we draw in PNG
                    //String type = n.getType();
                    //fig.setFillColor(n.color);
                    fig.setLineColor(n.color);

                    //THIS CODE IS ALL COMMENTED BECAUSE WE DON'T NEED IT.  The Image printer only deals with one layer, unlike in the PS
                    //Version of this code
                    /*if (type != null && type.startsWith("container:")) {
                     String containername = type.substring("container:".length());

                     LayerDiagram ldg = (LayerDiagram)Globals.curEditor().getLayerManager().getLayer(containername);
                    
                     ldg.paintContents(g, painter, x,  y,  scale + 1);
                     }*/
                }

                //PAINT IT
                if (clipBounds == null || fig.intersects(clipBounds)) {
                    if (painter == null) {
                        fig.paint(g);
                    } else {
                        painter.paint(g, fig);
                    }
                }
            }
            /*if (g instanceof ScalableGraphics) {
             //No scaling needed, since we don't produce a PNG with more than one layer
             ScalableGraphics sg = (ScalableGraphics) g;
             //sg.setScale(scale);
             }*/
        }
    }

} /* end class CmdSavePNG */
