/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package graphviz;

import java.awt.*;
import java.awt.geom.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

/**
 * A panel to draw a graph
 */
public class GraphPanel extends JPanel {

    /**
     * Constructs a graph panel.
     *
     * @param aToolBar the tool bar with the node and edge tools
     * @param aGraph the graph to be displayed and edited
     */
    public GraphPanel(ToolBar aToolBar, Graph aGraph) {
        toolBar = aToolBar;
        graph = aGraph;
        setBackground(Color.WHITE);

        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent event) {
                Point2D mousePoint = event.getPoint();
                Node n = graph.findNode(mousePoint);
                Edge e = graph.findEdge(mousePoint);
                Object tool = toolBar.getSelectedTool();
                if (tool == null) // select
                {
                    if (e != null) {
                        selected = e;
                    } else if (n != null) {
                        selected = n;
                        dragStartPoint = mousePoint;
                        dragStartBounds = n.getBounds();
                    } else {
                        selected = null;
                    }
                } else if (tool instanceof Node) {
                    Node prototype = (Node) tool;
                    Node newNode = (Node) prototype.clone();
                    boolean added = graph.add(newNode, mousePoint);
                    if (added) {
                        selected = newNode;
                        dragStartPoint = mousePoint;
                        dragStartBounds = newNode.getBounds();
                    } else if (n != null) {
                        selected = n;
                        dragStartPoint = mousePoint;
                        dragStartBounds = n.getBounds();
                    }
                } else if (tool instanceof Edge) {
                    if (n != null) {
                        rubberBandStart = mousePoint;
                    }
                }
                lastMousePoint = mousePoint;
                repaint();
            }

            public void mouseReleased(MouseEvent event) {
                Object tool = toolBar.getSelectedTool();
                if (rubberBandStart != null) {
                    Point2D mousePoint = event.getPoint();
                    Edge prototype = (Edge) tool;
                    Edge newEdge = (Edge) prototype.clone();
                    if (graph.connect(newEdge,
                            rubberBandStart, mousePoint)) {
                        selected = newEdge;
                    }
                }

                Point2D mousePoint = event.getPoint();
                Node n = graph.findNode(mousePoint);
                if (n != null) {

                }

                validate();
                repaint();

                lastMousePoint = null;
                dragStartBounds = null;
                rubberBandStart = null;
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent event) {
                Point2D mousePoint = event.getPoint();
                if (dragStartBounds != null) {
                    if (selected instanceof Node) {
                        Node n = (Node) selected;
                        Rectangle2D bounds = n.getBounds();
                        n.translate(
                                dragStartBounds.getX() - bounds.getX()
                                + mousePoint.getX() - dragStartPoint.getX(),
                                dragStartBounds.getY() - bounds.getY()
                                + mousePoint.getY() - dragStartPoint.getY());
                    }
                }
                lastMousePoint = mousePoint;
                repaint();
            }
        });
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        Rectangle2D bounds = getBounds();
        Rectangle2D graphBounds = graph.getBounds(g2);
        graph.draw(g2);

        if (selected instanceof Node) {
            Rectangle2D grabberBounds = ((Node) selected).getBounds();
            drawGrabber(g2, grabberBounds.getMinX(), grabberBounds.getMinY());
            drawGrabber(g2, grabberBounds.getMinX(), grabberBounds.getMaxY());
            drawGrabber(g2, grabberBounds.getMaxX(), grabberBounds.getMinY());
            drawGrabber(g2, grabberBounds.getMaxX(), grabberBounds.getMaxY());
        }

        if (selected instanceof Edge) {
            Line2D line = ((Edge) selected).getConnectionPoints();
            drawGrabber(g2, line.getX1(), line.getY1());
            drawGrabber(g2, line.getX2(), line.getY2());
        }

        if (rubberBandStart != null) {
            Color oldColor = g2.getColor();
            g2.setColor(PURPLE);
            g2.draw(new Line2D.Double(rubberBandStart, lastMousePoint));
            g2.setColor(oldColor);
        }
    }

    /**
     * Removes the selected node or edge.
     */
    public void removeSelected() {
        if (selected instanceof Node) {
            graph.removeNode((Node) selected);
        } else if (selected instanceof Edge) {
            graph.removeEdge((Edge) selected);
        }
        selected = null;
        repaint();
    }

    /**
     * Draws a single "grabber", a filled square
     *
     * @param g2 the graphics context
     * @param x the x coordinate of the center of the grabber
     * @param y the y coordinate of the center of the grabber
     */
    public static void drawGrabber(Graphics2D g2, double x, double y) {
        final int SIZE = 5;
        Color oldColor = g2.getColor();
        g2.setColor(PURPLE);
        g2.fill(new Rectangle2D.Double(x - SIZE / 2,
                y - SIZE / 2, SIZE, SIZE));
        g2.setColor(oldColor);
    }

    public Dimension getPreferredSize() {
        Rectangle2D bounds
                = graph.getBounds((Graphics2D) getGraphics());
        return new Dimension(
                (int) bounds.getMaxX(),
                (int) bounds.getMaxY());
    }

    private Graph graph;
    private ToolBar toolBar;
    private Point2D lastMousePoint;
    private Point2D rubberBandStart;
    private Point2D dragStartPoint;
    private Rectangle2D dragStartBounds;
    private Object selected;
    private static final Color PURPLE = new Color(0.7f, 0.4f, 0.7f);
}
