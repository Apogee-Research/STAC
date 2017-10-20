/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.tigris.gef.base;

import graph.Edge;
import graph.Node;
import java.awt.Rectangle;
import java.awt.geom.Ellipse2D;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import org.tigris.gef.persistence.PostscriptWriter;
import org.tigris.gef.presentation.Fig;
import org.tigris.gef.presentation.FigCircle;
import org.tigris.gef.presentation.FigLine;
import user.commands.image.DrawingCanvas;

/**
 *
 * @author user
 */
public class CmdDisplayGUI  extends Cmd{

    List<DrawingCanvas> loadlayers;
            
    Editor editor;
    
    public CmdDisplayGUI(Editor editor, List<DrawingCanvas> loadlayers) {
        super("DisplayGUI");
        this.editor=editor;
        this.loadlayers=loadlayers;
    }

    //Collection to store edges until both sides of the edge have been processed
    Map<Edge, CompleteEdge> edgemap = new HashMap<Edge, CompleteEdge>();

    @Override
    public void doIt() {
        loadlayers = loadlayers();
         //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void undoIt() {
    }

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


            FigLine getEdge() {
                Ellipse2D.Float inshape = (Ellipse2D.Float) in.getShape();
                Ellipse2D.Float outshape = (Ellipse2D.Float) out.getShape();
                //PAINT IT
                int centerXin = (int) inshape.getCenterX();
                int centerYin = (int) inshape.getCenterY();

                int centerXout = (int) outshape.getCenterX();
                int centerYout = (int) outshape.getCenterY();

                FigLine edgeline = new FigLine(centerXin, centerYin, centerXout, centerYout);

                return edgeline;
            }
    }

    // main
    public List<DrawingCanvas> loadlayers() {

        List<DrawingCanvas> canvases =loadlayers;//= new ArrayList<DrawingCanvas>();
        List<Layer> layers = editor.getLayerManager()._layers;

        Iterator<Layer> lit = layers.iterator();

        while (lit.hasNext()) {
            Layer l = lit.next();//.findLayerNamed("main");

            ArrayList<Fig> shapesin = new ArrayList<Fig>();

            //Rectangle clipBounds = g.getClipBounds();
            Iterator<Fig> figsIter = l.getContents().iterator();

            while (figsIter.hasNext()) {
                Fig fig = (Fig) figsIter.next();

                Object owner = fig.getOwner();

                if (owner instanceof Node) {
                    Node n = (Node) owner;
                    String type = n.getType();

                    if (type != null && type.startsWith("container:")) {

                        fig.containername = type.substring("container:".length());
                    } else {
                        fig.containername = null;
                    }
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
                }

                shapesin.add(fig);

                

            }
            
            Iterator<CompleteEdge> edgeerator = edgemap.values().iterator();
                while (edgeerator.hasNext()) {
                    CompleteEdge next = edgeerator.next();
                    if (next.isComplete()) {
                        //next.drawIt(g, painter);
                        FigLine edge = next.getEdge();
                        //shapesin.add(edge);
                        l.add(edge);
                        edgeerator.remove();
                    }
                }

            DrawingCanvas canvas = new DrawingCanvas(l, shapesin);
            if (canvas.getName().equals("main")) {
                canvas.setVisible(true);
                //org.graph.commons.logging.LogFactory.getLog(null).info(" SET VISIBLE!! canvas.getName(): " + canvas.getName());
                //canvases.add(canvas);
            } else {
                canvas.setVisible(false);
            }
            canvases.add(canvas);
        }
        return canvases;

    }
}
