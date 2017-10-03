/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package user.commands.image;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Component;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import org.tigris.gef.base.Layer;
import org.tigris.gef.presentation.Fig;

public class DrawingCanvas extends Canvas {

    Layer layer;

    private final ArrayList<Fig> shapes;

    public DrawingCanvas(Layer layer, ArrayList<Fig> shapesin) {

        this.layer = layer;
        shapes = shapesin;

        super.setName(layer.getName());
        
        setSize(2000, 2000);


        addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent me) {
                super.mouseClicked(me);
                for (Fig s : shapes) {

                    if (s.getShape().contains(me.getPoint())) {//check if mouse is clicked within shape

                        //we can either just print out the object class name
                        org.graph.commons.logging.LogFactory.getLog(null).info("Clicked a " + s.getClass().getName());

                        org.graph.commons.logging.LogFactory.getLog(null).info("switch to:" + s.containername);

                        List<DrawingCanvas> ls = CmdGUIDisplay.loadlayers;
                        Object[] components = ls.toArray();
                        if (s.containername != null) {
                            //Component[] components = (Component[])CmdGUIDisplay.loadlayers.toArray();//getParent().getComponents();
                            for (int i = 0; i < components.length; i++) {
                                if (components[i] instanceof DrawingCanvas) {
                                    DrawingCanvas dc = (DrawingCanvas) components[i];
                                    if (!dc.getName().equals(s.containername)) {
                                        CmdGUIDisplay.mainFrame.remove(dc);
                                        setVisible(false);
                                        //getParent().paint(getParent().getGraphics());
                                        break;
                                    }
                                }
                            }

                            for (int i = 0; i < components.length; i++) {
                                if (components[i] instanceof DrawingCanvas) {
                                    DrawingCanvas dc = (DrawingCanvas) components[i];
                                    if (dc.getName().equals(s.containername)) {
                                        dc.setVisible(true);
                                        CmdGUIDisplay.mainFrame.add(dc);

                                        break;
                                    }
                                }
                            }
                            getParent().revalidate();
                            getParent().repaint();
                        }


                    }
                }

            }
        });

    }

    /*public static void main(String[] args) {
        Frame f=new Frame("Draw shape and text on Canvas");
         final Canvas canvas=new DrawingCanvas();
		
         f.add(canvas);
		
         f.setSize(300,300);
         f.setVisible(true);
         f.addWindowListener(new WindowAdapter() {
         public void windowClosing(WindowEvent event) {
         saveCanvas(canvas);
         System.exit(0);
         }
         });

    }*/

    public void paint(Graphics g) {

        layer.paint(g);
    }

    public static void saveCanvas(Canvas canvas) {

        BufferedImage image = new BufferedImage(canvas.getWidth(), canvas.getHeight(), BufferedImage.TYPE_INT_RGB);

        Graphics2D g2 = (Graphics2D) image.getGraphics();

        canvas.paint(g2);
        try {
            ImageIO.write(image, "png", new File("canvas.png"));
        } catch (Exception e) {

        }
    }
}
