// $Id: SvgWriter2D.java 1326 2011-05-17 09:49:01Z bobtarling $
/* 
 * Copyright (c) 2009, Tom Morris and other contributors
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions are met:
 * 
 *  o Redistributions of source code must retain the above copyright notice, 
 *    this list of conditions and the following disclaimer.
 *  o Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *  o The names of the contributors may not be used to endorse or promote 
 *    products derived from this software without specific prior written 
 *    permission.
 *    
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS 
 *  "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED 
 *  TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 *  PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER 
 *  OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, 
 *  EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, 
 *  PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 *  OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, 
 *  WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR 
 *  OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF 
 *  ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
// Copyright (c) 1996-2008 The Regents of the University of California. All
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
package org.tigris.gef.persistence;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.Paint;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.RenderingHints.Key;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ImageObserver;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderableImage;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.AttributedCharacterIterator;
import java.text.NumberFormat;
import java.util.LinkedList;
import java.util.Map;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.tigris.gef.persistence.export.FontUtility;
import org.tigris.gef.presentation.Fig;

/**
 * A Graphics2D implementation which outputs SVG.
 * <p>
 * The original Graphics (as opposed to Graphics2D) implementation by Andreas
 * Rueckert managed all transformations internally in an attempt to simplify the
 * created SVG stream. This implementation uses a different strategy and just
 * pushes all transformations through to SVG directly, allowing (requiring) the
 * SVG renderer to handle the geometry.
 * <p>
 * TODO: This is very much a work-in-progress, but the basic framework for
 * Graphics2D support is included.
 *
 * @author Tom Morris <tfmorris@gmail.com>
 * @author Andreas Rueckert <a_rueckert@gmx.net>
 * since v0.13.3. Use SVGGraphics2D from Apache Batik
 */
public class SvgWriter2D extends Graphics2D {

    private static final Logger LOG
            = Logger.getLogger(SvgWriter2D.class.getCanonicalName());

    private static final Stroke DEFAULT_STROKE = new BasicStroke();
    private static final Color DEFAULT_COLOR = Color.black;
    private static final Color DEFAULT_BACKGROUND = Color.white;

    private PrintWriter writer;
    private Document svg;

    /**
     * Stack of active elements in document
     */
    private Stack<Element> elements = new Stack<Element>();

    private class GraphicsContext {

        Color foreground;
        Color background;
        Composite composite;
        Paint paint;
        Stroke stroke;
        AffineTransform transform;
        Shape clip;
        int lineWidth = 0;
        Font font;
        boolean XOR = false;

        GraphicsContext(Color fg, Color bg, int w) {
            foreground = fg;
            background = bg;
            paint = bg;
            lineWidth = w;
        }
    }

    private Stack<GraphicsContext> graphicsContexts
            = new Stack<GraphicsContext>();

    private GraphicsContext activeGC;

    /**
     * The drawing area for the SVG output.
     */
//    private Rectangle drawingArea;
    private Shape clip;

    private int hInset = 10;

    private int vInset = 10;

    private String svgNamespace = "http://www.w3.org/2000/svg";

    /**
     * Flag that marks if we need to write the DOCTYPE and the XML document node
     * of the SVG
     */
    private boolean isInline = false;

    private RenderingHints renderingHints = new RenderingHints(null);

    private AffineTransform transform = new AffineTransform();

//    private Stroke svgStroke;
//
//    private Paint svgPaint;
//
//    private Color svgFill;
//    
//    private Color svgLine;
//    
//    private Integer svgLineWidth;
//    
//    private AffineTransform svgTransform;
    /**
     * Construct a new SvgWriter2D which will write to the given stream.
     *
     * @param stream OutputStream to write SVG to
     * @param area bounds of the drawing area. These coordinates will be used as
     * the SVG frame.
     * @throws ParserConfigurationException if the XML DocumentBuilder factory
     * can't create the requested document
     * @throws UnsupportedEncodingException if UTF-8 encodings aren't supported
     */
    public SvgWriter2D(OutputStream stream, Rectangle area)
            throws ParserConfigurationException, UnsupportedEncodingException {

        writer = new PrintWriter(new OutputStreamWriter(stream, "UTF-8"));
//        drawingArea = area;
//        setClip(area);
//        translate(hInset - drawingArea.x, vInset - drawingArea.y);
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

        factory.setNamespaceAware(false);
        factory.setValidating(false);
        DocumentBuilder builder = factory.newDocumentBuilder();

        svg = builder.newDocument();

        Element root = svg.createElement("svg");
        root.setAttribute("xmlns", svgNamespace);
        root.setAttribute("xmlns:xlink", "http://www.w3.org/1999/xlink");
        root.setAttribute("width", ""
                + (2 * hInset + area.width));
        root.setAttribute("height", ""
                + (2 * vInset + area.height));
        root.setAttribute("version", "1.1");
        elements.push(root);

        activeGC = new GraphicsContext(Color.black, Color.white, 1);
        activeGC.font = new Font("Verdana", Font.PLAIN, 8);
        graphicsContexts.push(activeGC);

        Element group = svg.createElement("g");

        group.setAttribute("stroke", toHexString(activeGC.foreground));
        group.setAttribute("fill", toHexString(activeGC.background));
        group.setAttribute("font", activeGC.font.getFontName());
        group.setAttribute("stroke-width", "1");
        elements.push(group);
    }

    /**
     * Construct a new SvgWriter2D which will write to the given stream.
     *
     * @param stream OutputStream to write SVG to
     * @param drawingArea bounds of the drawing area. These coordinates will be
     * used as the SVG frame.
     * @param isInline If false, it writes the DOCTYPE and XML nodes. If true,
     * we don't write them (think on inline SVG)
     * @throws ParserConfigurationException if the XML DocumentBuilder factory
     * can't create the requested document
     * @throws UnsupportedEncodingException if UTF-8 encodings aren't supported
     */
    public SvgWriter2D(OutputStream stream, Rectangle drawingArea,
            boolean isInline) throws ParserConfigurationException,
            UnsupportedEncodingException {

        this(stream, drawingArea);
        this.isInline = isInline;
    }

    /**
     * Create a Graphics2D context.
     *
     * @return Returns the same object created by the constructor.
     */
    public Graphics2D create() {
        return this;
    }

    /**
     * Dispose of this Graphics context, first writing all content to the output
     * SVG document.
     *
     * @see java.awt.Graphics#dispose()
     */
    public void dispose() {
        while (!elements.isEmpty()) {
            popElement();
        }
//        popElement(); // outer group
//        popElement(); // root element - this will add it to the document
//        if (!elements.isEmpty()) {
//            throw new IllegalStateException("Element stack not empty");
//        }
        printDOMTree(svg);
        writer.close();
    }

    /**
     * Print the DOM sub tree rooted at the given node to the output stream.
     *
     * @param node root node of the subtree
     */
    public void printDOMTree(Node node) {

        switch (node.getNodeType()) {

            // print the document element
            case Node.DOCUMENT_NODE: {
                if (!isInline) {
                    writer.println("<?xml version=\"1.0\" encoding=\"utf-8\" ?>");
                    writer.println("<!DOCTYPE svg PUBLIC "
                            + "\"-//W3C//DTD SVG 1.1//EN\" "
                            + "\"http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd\">");
                }
                printDOMTree(((Document) node).getDocumentElement());
                break;
            }

            // print element with attributes
            case Node.ELEMENT_NODE: {
                printElementNode(node);
                break;
            }

            // handle entity reference nodes
            case Node.ENTITY_REFERENCE_NODE: {
                writer.print("&");
                writer.print(node.getNodeName());
                writer.print(";");
                break;
            }

            // print cdata sections
            case Node.CDATA_SECTION_NODE: {
                writer.print("<![CDATA[");
                writer.print(node.getNodeValue());
                writer.print("]]>");
                break;
            }

            // print text
            case Node.TEXT_NODE: {
                printTextNode(node);
                break;
            }

            // print processing instruction
            case Node.PROCESSING_INSTRUCTION_NODE: {
                writer.print("<?");

                writer.print(node.getNodeName());
                String data = node.getNodeValue();

                writer.print("");
                writer.print(data);

                writer.print("?>");
                break;
            }
        }
    }

    private void printElementNode(Node node) {
        writer.print("<");
        writer.print(node.getNodeName());
        NamedNodeMap attrs = node.getAttributes();

        for (int i = 0; i < attrs.getLength(); i++) {
            Node attr = attrs.item(i);

            writer.print(" " + attr.getNodeName() + "=\""
                    + attr.getNodeValue() + "\"");
        }

        NodeList children = node.getChildNodes();

        if (children.getLength() > 0) {
            writer.println(">");
            int len = children.getLength();

            for (int i = 0; i < len; i++) {
                printDOMTree(children.item(i));
            }

            writer.print("</");
            writer.print(node.getNodeName());
            writer.println(">");
        } else {
            writer.println("/>");
        }
    }

    private void printTextNode(Node node) {
        String text = node.getNodeValue();

        for (int i = 0; i < text.length(); i++) {

            // escape reserved characters
            switch (text.charAt(i)) {

                case '&': {
                    writer.print("&amp;");
                    break;
                }

                case '<': {
                    writer.print("&lt;");
                    break;
                }

                case '>': {
                    writer.print("&gt;");
                    break;
                }

                default:
                    writer.print(text.charAt(i));
            }
        }
    }

    /**
     * Get the current color for drawing operations.
     *
     * @return The current color for drawing operations.
     */
    public Color getColor() {
        return activeGC.foreground;
    }

    /**
     * Return a String representation of the current color.
     *
     * @return The current color as a String (like #FF00BF).
     */
    private String getColorAsString() {
        return toHexString(activeGC.foreground);
    }

    /**
     * Return a String representation of the given color
     *
     * @return The given color as a String (like #FF00BF).
     */
    private String toHexString(Color c) {
        // Remove the alpha channel info from the string representation.
        return "#" + Integer.toHexString(c.getRGB()).substring(2);
    }

    /**
     * Set the current color for drawing operations.
     *
     * @param c The new color for drawing operations.
     */
    public void setColor(Color c) {
        activeGC.foreground = c;
    }

    /**
     * @see java.awt.Graphics#setPaintMode()
     */
    public void setPaintMode() {
        activeGC.XOR = false;
        // The inverse of XOR mode
        LOG.warning("Unimplemented - setPaintMode");
    }

    public void setXORMode(Color otherColor) {
        activeGC.XOR = true;
        LOG.warning("Unimplemented - setXORMode");
    }

    public Font getFont() {
        return activeGC.font;
    }

    public void setFont(Font f) {
        if (f != null) {
            activeGC.font = f;
            LOG.fine("setFont " + f.getFontName() + " " + f);
        } else {
            LOG.fine("null setFont "
                    + activeGC.font.getFontName() + " " + activeGC.font);
        }
    }

    public FontMetrics getFontMetrics(Font f) {
        return FontUtility.getFontMetrics(f);
    }

    public void copyArea(int x, int y, int width, int height, int dx, int dy) {
        LOG.warning("unimplemented copyArea");
    }

    public boolean drawImage(Image img, int x, int y, ImageObserver observer) {
        LOG.warning("Unimplemented -  drawImage");
        // TODO: Unimplemented
        return false;
    }

    /*
     * privat void handlesinglepixel(int x, int y, int pixel) { if (((pixel >>
     * 24) & 0xff) == 0) { // should be transparent, is printed white: pixel =
     * 0xffffff; } p.print(Integer.toHexString((pixel >> 20) & 0x0f)
     * +Integer.toHexString((pixel >> 12) & 0x0f) +Integer.toHexString((pixel >>
     * 4) & 0x0f)); }
     */
    public boolean drawImage(Image img, int x, int y, int w, int h,
            ImageObserver observer) {

        LOG.warning("Unimplemented -  drawImage");
        // TODO: Unimplemented

        /*
         * int iw = img.getWidth(observer), ih = img.getHeight(observer);
         * p.println("gsave"); writeCoords(x,y+h); p.println("translate");
         * writeCoords(w,-h); p.println("scale"); p.println("/DatenString "+iw+"
         * string def"); writeCoords(iw,-ih); p.println("4 [" + iw +" 0 0 "+
         * (-ih) + " 0 " + ih + "]"); p.println("{currentfile DatenString
         * readhexstring pop} bind"); p.println("false 3 colorimage"); int[]
         * pixels = new int[iw * ih]; PixelGrabber pg = new PixelGrabber(img, 0,
         * 0, iw, ih, pixels, 0, iw); //
         * pg.setColorModel(Toolkit.getDefaultToolkit().getColorModel()); try {
         * pg.grabPixels(); } catch (InterruptedException e) {
         * System.err.println("interrupted waiting for pixels!"); return false; }
         * if ((pg.getStatus() & ImageObserver.ABORT) != 0) {
         * System.err.println("image fetch aborted or errored"); return false; }
         * for (int j = 0; j < ih; j++) { for (int i = 0; i < iw; i++) {
         * handlesinglepixel(i, j, pixels[j * iw + i]); } if (iw % 2 == 1)
         * p.print("0"); writer.println(); } if (ih % 2 == 1) { for (int i = 0;
         * i < 3 * (iw + iw % 2); i++) p.print("0"); writer.println(); }
         * p.println("grestore");
         */
        return true;
    }

    public boolean drawImage(Image img, int x, int y, Color bgcolor,
            ImageObserver observer) {
        LOG.warning("Unimplemented -  drawImage");
        // TODO: Unimplemented
        return false;
    }

    public boolean drawImage(Image img, int x, int y, int width, int height,
            Color bgcolor, ImageObserver observer) {
        LOG.warning("Unimplemented -  drawImage");
        // TODO: Unimplemented
        return false;
    }

    public boolean drawImage(Image img, int dx1, int dy1, int dx2, int dy2,
            int sx1, int sy1, int sx2, int sy2, ImageObserver observer) {
        LOG.warning("Unimplemented -  drawImage");
        // TODO: Unimplemented
        return true;
    }

    public boolean drawImage(Image img, int dx1, int dy1, int dx2, int dy2,
            int sx1, int sy1, int sx2, int sy2, Color bgcolor,
            ImageObserver observer) {
        LOG.warning("Unimplemented -  drawImage");
        // TODO: Unimplemented
        return true;
    }

    private void drawRect(double x, double y, double w, double h,
            Color strokeColor, Color fillColor) {
        LOG.fine("drawRect " + x + " " + y + " " + w + " " + h);
        Element rect = svg.createElement("rect");

        rect.setAttribute("x", "" + x);
        rect.setAttribute("y", "" + (y));
        rect.setAttribute("width", "" + w);
        rect.setAttribute("height", "" + h);

        setDrawingAttributes(rect,
                new GraphicsContext(strokeColor, fillColor, activeGC.lineWidth));

        elements.peek().appendChild(rect);
    }

    public void drawRect(int x, int y, int w, int h) {
        drawRect(x, y, w, h, activeGC.foreground, null);
    }

    private void drawRect(double x, double y, double w, double h) {
        drawRect(x, y, w, h, activeGC.foreground, null);
    }

    private void fillRect(double x, double y, double w, double h) {
        drawRect(x, y, w, h, activeGC.foreground, activeGC.foreground);
    }

    public void fillRect(int x, int y, int w, int h) {
        drawRect(x, y, w, h, activeGC.foreground, activeGC.foreground);
    }

    public void clearRect(int x, int y, int w, int h) {
        drawRect(x, y, w, h, activeGC.background, activeGC.background);
    }

    private void drawOval(double x, double y, double w, double h,
            Color strokeColor, Color fill) {

        LOG.fine("drawOval " + x + " " + y + " " + w + " " + h);

        Element oval = svg.createElement("ellipse");

        oval.setAttribute("cx", "" + (x + w / 2));
        oval.setAttribute("cy", "" + (y + h / 2));
        oval.setAttribute("rx", "" + (w / 2));
        oval.setAttribute("ry", "" + (h / 2));
        setDrawingAttributes(oval,
                new GraphicsContext(strokeColor, fill, activeGC.lineWidth));
        elements.peek().appendChild(oval);
    }

    /**
     * @see java.awt.Graphics#drawOval(int, int, int, int)
     */
    public void drawOval(int x, int y, int w, int h) {
        drawOval(x, y, w, h, activeGC.foreground, null);
    }

    private void drawOval(double x, double y, double w, double h) {
        drawOval(x, y, w, h, activeGC.foreground, null);
    }

    private void fillOval(double x, double y, double w, double h) {
        drawOval(x, y, w, h, activeGC.foreground, activeGC.foreground);
    }

    public void fillOval(int x, int y, int w, int h) {
        drawOval(x, y, w, h, activeGC.foreground, activeGC.foreground);
    }

    public void drawArc(int x, int y, int w, int h, int startAngle,
            int arcAngle) {

        LOG.warning("Not implemented : drawArc");
        // TODO: Unimplemented
        /*
         * writeEllipsePath(x,y,w+1,h+1,startAngle,arcAngle);
         * p.println("stroke");
         */
    }

    public void fillArc(int x, int y, int w, int h, int startAngle,
            int arcAngle) {
        LOG.warning("Not implemented : fillArc");
        // TODO: Unimplemented
        /*
         * writeEllipsePath(x,y,w,h,startAngle,arcAngle); p.println("eofill");
         */
    }

    private void drawRoundRect(double x, double y, double w, double h,
            double arcw, double arch, Color stroke, Color fill) {

        LOG.fine("drawRoundRect " + x + " " + y + " " + w + " " + h);

        Element rect = svg.createElement("rect");

        rect.setAttribute("x", "" + x);
        rect.setAttribute("y", "" + y);
        rect.setAttribute("width", "" + w);
        rect.setAttribute("height", "" + h);
        rect.setAttribute("rx", "" + arcw);
        rect.setAttribute("ry", "" + arch);
        setDrawingAttributes(rect,
                new GraphicsContext(stroke, fill, activeGC.lineWidth));

        elements.peek().appendChild(rect);
    }

    public void drawRoundRect(int x, int y, int w, int h, int arcw, int arch) {
        drawRoundRect(x, y, w, h, arcw, arch, activeGC.foreground,
                activeGC.background);
    }

    private void drawRoundRect(double x, double y, double w, double h,
            double arcw, double arch) {
        drawRoundRect(x, y, w, h, arcw, arch, activeGC.foreground, activeGC.background);
    }

    public void fillRoundRect(int x, int y, int w, int h, int arcw, int arch) {
        drawRoundRect(x, y, w, h, arcw, arch, activeGC.foreground, activeGC.foreground);
    }

    private void fillRoundRect(double x, double y, double w, double h,
            double arcw, double arch) {
        drawRoundRect(x, y, w, h, arcw, arch, activeGC.foreground, activeGC.foreground);
    }

    private void drawPolygon(int[] xPoints, int[] yPoints, int nPoints,
            Color stroke, Color fill) {
        LOG.fine("drawPolygon");

        double maxX = 0;
        double maxY = 0;
        Element polygon = svg.createElement("polygon");

        setDrawingAttributes(polygon,
                new GraphicsContext(stroke, fill, activeGC.lineWidth));

        // Create the list of points for this tag.
        // I.e. points="100,100 150,150 200,200"
        StringBuffer pointList = new StringBuffer();

        for (int i = 0; i < nPoints; i++) {

            if (i > 0) {
                pointList.append(" ");
            }

            pointList.append("" + xPoints[i] + ","
                    + (yPoints[i]));

            if (xPoints[i] > maxX) {
                maxX = xPoints[i];
            }

            if ((yPoints[i]) > maxY) {
                maxY = (yPoints[i]);
            }
        }

        polygon.setAttribute("points", pointList.toString());
        elements.peek().appendChild(polygon);
    }

    public void drawPolygon(int[] xPoints, int[] yPoints, int nPoints) {
        drawPolygon(xPoints, yPoints, nPoints, activeGC.foreground, null);
    }

    public void fillPolygon(int[] xPoints, int[] yPoints, int nPoints) {
        drawPolygon(xPoints, yPoints, nPoints, activeGC.foreground, activeGC.foreground);
    }

    public void drawPolyline(int[] xPoints, int[] yPoints, int nPoints) {
        LOG.fine("drawPolyLine");

        double maxX = 0;
        double maxY = 0;
        Element polyline = svg.createElement("polyline");

        setDrawingAttributes(polyline);

        // Create the list of points for this tag.
        // I.e. points="100,100 150,150 200,200"
        StringBuffer pointList = new StringBuffer();

        for (int i = 0; i < nPoints; i++) {

            if (i > 0) {
                pointList.append(" ");
            }

            pointList.append("" + xPoints[i] + ","
                    + yPoints[i]);

            if (xPoints[i] > maxX) {
                maxX = xPoints[i];
            }

            if (yPoints[i] > maxY) {
                maxY = yPoints[i];
            }
        }

        polyline.setAttribute("points", pointList.toString());
        elements.peek().appendChild(polyline);
    }

    public void drawLine(int x1, int y1, int x2, int y2) {
        drawLine((float) x1, (float) y1, (float) x2, (float) y2);
    }

    private void drawLine(double x1, double y1, double x2, double y2) {
        LOG.fine("drawLine " + x1 + " " + y1 + " " + x2 + " " + y2);
        Element line = svg.createElement("line");

        line.setAttribute("x1", "" + x1);
        line.setAttribute("y1", "" + y1);
        line.setAttribute("x2", "" + x2);
        line.setAttribute("y2", "" + y2);

        setDrawingAttributes(line);

        elements.peek().appendChild(line);
    }

    public void setClip(int x, int y, int w, int h) {
        setClip(new Rectangle(x, y, w, h));
    }

    public void setClip(Shape newClip) {
        clip = newClip;
        LOG.fine("setClip " + newClip);
    }

    public Rectangle getClipBounds() {
        return clip.getBounds();
    }

    public void clipRect(int x, int y, int w, int h) {
        if (clip == null) {
            setClip(x, y, w, h);
        } else {
            clip(new Rectangle(x, y, w, h));
        }
    }

    public Shape getClip() {
        return clip;
    }

    public void clip(Shape s) {
        if (clip == null) {
            clip = s;
        } else {
            Area clipArea = new Area(clip);
            clipArea.intersect(new Area(s));
            GeneralPath clipShape = new GeneralPath();
            clipShape.append(clipArea.getPathIterator(null), true);
            clip = clipShape;
        }
        LOG.fine("Clip with " + s + " result = " + clip);
    }

    public void translate(int x, int y) {
        translate((double) x, (double) y);
    }

    public void scale(double xscale, double yscale) {
        LOG.fine("Scale " + xscale + " " + yscale);
        transform.scale(xscale, yscale);
        transformGroup("scale(" + xscale + "," + yscale + ")");
    }

    @Override
    public void setTransform(AffineTransform tx) {
        transform = tx;
        LOG.fine("setTransform " + tx);
    }

    @Override
    public void shear(double shx, double shy) {
        transform.shear(shx, shy);
        LOG.fine("Shear " + shx + " " + shy);
    }

    @Override
    public void transform(AffineTransform tx) {
        transform.concatenate(tx);
        LOG.fine("Transform " + tx);
    }

    public void translate(double tx, double ty) {
        transform.translate(tx, ty);
        LOG.fine("Translate " + tx + " " + ty);
    }

    /**
     * Draw a string at a given position.
     *
     * @param t The string to draw.
     * @param x The horizontal position of the text.
     * @param y The vertical position of the text.
     */
    public void drawString(String t, int x, int y) {
        drawString(t, (float) x, (float) y);
    }

    public void drawString(java.text.AttributedCharacterIterator aci, int i1,
            int i2) {
    }

    @Override
    public void addRenderingHints(Map<?, ?> hints) {
        renderingHints.putAll(hints);
        LOG.warning("Warning - addRenderingHint not supported");
    }

    @Override
    public Object getRenderingHint(Key hintKey) {
        return renderingHints.get(hintKey);
    }

    @Override
    public RenderingHints getRenderingHints() {
        return renderingHints;
    }

    @Override
    public void setRenderingHint(Key hintKey, Object hintValue) {
        renderingHints.put(hintKey, hintValue);
        LOG.warning("Warning - setRenderingHint not supported");
    }

    @Override
    public void setRenderingHints(Map<?, ?> hints) {
        renderingHints.putAll(hints);
        LOG.warning("Warning - setRenderingHint not supported");
    }

    /**
     * {@inheritDoc}
     *
     * @see java.awt.Graphics2D#draw(java.awt.Shape)
     */
    @Override
    public void draw(Shape s) {
        LOG.fine("Draw - " + s);
        if (s instanceof Line2D) {
            Line2D l = (Line2D) s;
            // TODO: The Graphics2D use needs to be differentiated from the
            // Graphics use so that the right drawing context is used
            drawLine(l.getX1(), l.getY1(), l.getX2(), l.getY2());
        } else if (s instanceof Rectangle2D) {
            Rectangle2D r = (Rectangle2D) s;
            drawRect(r.getX(), r.getY(), r.getWidth(), r.getHeight());
        } else if (s instanceof RoundRectangle2D) {
            RoundRectangle2D r = (RoundRectangle2D) s;
            drawRoundRect(r.getX(), r.getY(), r.getWidth(), r.getHeight(),
                    r.getArcWidth(), r.getArcHeight());
        } else if (s instanceof Polygon) {
            drawPolygon((Polygon) s);
        } else if (s instanceof Ellipse2D) {
            Ellipse2D e = (Ellipse2D) s;
            drawOval(e.getCenterX(), e.getCenterY(), e.getWidth() / 2, e
                    .getHeight() / 2);
        } else {
            drawPath(s, activeGC.foreground, null);
        }
    }

    private void drawPath(Shape gp, Color lineColor, Color fillColor) {
        LOG.fine("draw GeneralPath");
        Element element = svg.createElement("path");
        setDrawingAttributes(element,
                new GraphicsContext(lineColor, fillColor, activeGC.lineWidth));
        element.setAttribute("d", getPath(gp));
        elements.peek().appendChild(element);
    }

    /**
     * @param gp GeneralPath object
     * @return string containing SVG path commands
     */
    private String getPath(Shape gp) {
        StringBuffer path = new StringBuffer();
        PathIterator pi = gp.getPathIterator(null);
        double[] start;
        double[] last;
        while (!pi.isDone()) {
            double[] coords = new double[6];
            int type = pi.currentSegment(coords);
            if (type == pi.SEG_MOVETO) {
                start = new double[2];
                start[0] = coords[0];
                start[1] = coords[1];
                last = start;
                path.append(" M " + coords[0] + " " + coords[1]);
                LOG.fine("MOVETO " + coords[0] + ", " + coords[1]);
            } else if (type == pi.SEG_LINETO) {
                path.append(" L " + coords[0] + " " + coords[1]);
                LOG.fine("LINETO " + coords[0] + ", " + coords[1]);
            } else if (type == pi.SEG_CUBICTO) {
                // TODO: Do Java and SVG specify control points in same order?
                path.append(" C " + coords[0] + " " + coords[1] + " "
                        + coords[2] + " " + coords[3] + " " + coords[4] + " "
                        + coords[5]);
                LOG.fine("CUBICTO " + coords[0] + " " + coords[1] + " "
                        + coords[2] + " " + coords[3] + " " + coords[4] + " "
                        + coords[5]);
            } else if (type == pi.SEG_QUADTO) {
                // TODO: Do Java and SVG specify control points in same order?
                path.append(" Q " + coords[0] + " " + coords[1] + " "
                        + coords[2] + " " + coords[3]);
                LOG.fine("QUADTO " + coords[0] + " " + coords[1] + " "
                        + coords[2] + " " + coords[3]);
            } else if (type == pi.SEG_CLOSE) {
                path.append(" Z");
                LOG.fine("CLOSE path");
            } else {
                LOG.warning("Unsupported GeneralPath segment type " + type);
            }
            pi.next();
        }
        return path.toString();
    }

    @Override
    public void drawGlyphVector(GlyphVector g, float x, float y) {
        // TODO Auto-generated method stub
        LOG.warning("Unimplemented -  drawGlyphVector");
    }

    @Override
    public boolean drawImage(Image img, AffineTransform xform,
            ImageObserver obs) {
        LOG.warning("Unimplemented -  drawImage");
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void drawImage(BufferedImage img, BufferedImageOp op, int x, int y) {
        // TODO Auto-generated method stub

    }

    @Override
    public void drawRenderableImage(RenderableImage img,
            AffineTransform xform) {
        // TODO Auto-generated method stub

    }

    @Override
    public void drawRenderedImage(RenderedImage img, AffineTransform xform) {
        // TODO Auto-generated method stub

    }

    @Override
    public void drawString(String str, float x, float y) {
        Element text = svg.createElement("text");

        text.setAttribute("x", "" + x);
        text.setAttribute("y", "" + y);
        text.setAttribute("font-family", activeGC.font.getFamily());
        text.setAttribute("font-size", "" + activeGC.font.getSize());
        text.setAttribute("fill", getColorAsString());
//        text.setAttribute("stroke", getColorAsString());

        // If this is a bold font, add the appropriate attribute.
        if (getFont().isBold()) {
            text.setAttribute("font-weight", "bold");
        }

        // If this is a italic font, add the appropriate attribute.
        if (getFont().isItalic()) {
            text.setAttribute("font-style", "italic");
        }

        text.appendChild(svg.createTextNode(str));
        elements.peek().appendChild(text);
    }

    @Override
    public void drawString(AttributedCharacterIterator iterator, float x,
            float y) {
        // TODO Auto-generated method stub

    }

    /**
     * {@inheritDoc}
     *
     * @see java.awt.Graphics2D#fill(java.awt.Shape)
     */
    @Override
    public void fill(Shape s) {
        // TODO: We probably shouldn't be sharing drawing methods between
        // the Graphics & Graphics2D methods since they use different drawing
        // models.  At a minimum, we use 1-pixel lines for the old and our
        // current stroke from the new.
        if (s instanceof Line2D) {
            Line2D l = (Line2D) s;
            drawLine(l.getX1(), l.getY1(), l.getX2(), l.getY2());
        } else if (s instanceof Rectangle2D) {
            Rectangle2D r = (Rectangle2D) s;
            fillRect(r.getX(), r.getY(), r.getWidth(), r.getHeight());
        } else if (s instanceof RoundRectangle2D) {
            RoundRectangle2D r = (RoundRectangle2D) s;
            fillRoundRect(r.getX(), r.getY(), r.getWidth(), r.getHeight(),
                    r.getArcWidth(), r.getArcHeight());
        } else if (s instanceof Polygon) {
            fillPolygon((Polygon) s);
        } else if (s instanceof Ellipse2D) {
            Ellipse2D e = (Ellipse2D) s;
            fillOval(e.getCenterX(), e.getCenterY(), e.getWidth() / 2, e
                    .getHeight() / 2);
        } else {
            // TODO: What other shapes do we need?
            // TODO: Whether or not this works may depend of the sophistication
            // of the SVG renderer's rendering model
            drawPath(s, activeGC.foreground, activeGC.foreground);
            LOG.warning("Falling back to fill shape " + s);
        }

    }

    @Override
    public Color getBackground() {
        return activeGC.background;
    }

    @Override
    public Composite getComposite() {
        return activeGC.composite;
    }

    @Override
    public GraphicsConfiguration getDeviceConfiguration() {
        LOG.warning("Unimplemented - getDeviceConfiguration");
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public FontRenderContext getFontRenderContext() {
        LOG.warning("Unimplemented - getFontRenderContext");
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Paint getPaint() {
        return activeGC.paint;
    }

    @Override
    public Stroke getStroke() {
        return activeGC.stroke;
    }

    @Override
    public AffineTransform getTransform() {
        return transform;
    }

    @Override
    public boolean hit(Rectangle rect, Shape s, boolean onStroke) {
        // TODO: This is just a rough approximation, but it should be unused
        return s.intersects(rect);
    }

    @Override
    public void rotate(double theta) {
        transform.rotate(theta);
        transformGroup("rotate(" + degrees(theta) + ")");
    }

    @Override
    public void rotate(double theta, double x, double y) {
        transform.rotate(theta, x, y);
        transformGroup("rotate(" + degrees(theta) + "," + x + "," + y + ")");
    }

    @Override
    public void setBackground(Color color) {
        activeGC.background = color;
    }

    @Override
    public void setComposite(Composite comp) {
        if (comp != null) {
            activeGC.composite = comp;
            LOG.fine("New composite = " + activeGC.composite);
        }
    }

    @Override
    public void setPaint(Paint newPaint) {
        if (newPaint != null) {
            activeGC.paint = newPaint;
            LOG.fine("new paint = " + activeGC.paint);
        }
        // TODO: new group with the current paint mode (minimizing changes)
    }

    /**
     * This will check the current rendering settings against the last ones
     * output to SVG and create a new group with the updated settings if
     * required.
     */
    private void updateRenderGroup() {
//        if (paint.equals(svgPaint) && stroke.equals(svgStroke)) {
//            return;
//        }
//        Element group = svg.createElement("g");
//        
//        if (!getColor().equals(svgLine)) {
//            group.setAttribute("color", getColorAsString());
//        }
//        if (paint.getTransparency() != svgPaint.getTransparency()) {
//            
//        }
//        if (group.getAttributes().getLength() > 0) {
//            elements.peek().appendChild(group);
//        }
    }

    @Override
    public void setStroke(Stroke s) {
        if (s != null) {
            activeGC.stroke = s;
            LOG.fine("new stroke = " + activeGC.stroke);
        }
    }

    /**
     * Convert radians to degrees.
     *
     * @param theta angle in radians
     * @return angle in degrees
     */
    private double degrees(double theta) {
        return theta / Math.PI * 180.0;
    }

    /**
     * Create a group with the given transformation
     *
     * @param transformation
     */
    private void transformGroup(String transformation) {
        Element g = svg.createElement("g");
        g.setAttribute("transform", transformation);
        elements.peek().appendChild(g);
    }

    /**
     * Output rendering attributes which have been changed since last output to
     * SVG.
     * <p>
     * TODO: We need versions of this which differentiate between the Graphics
     * drawing context (color, fill, lineWidth=1) and the Graphics2D context
     * (Paint, Stroke, etc).
     *
     * @param element XML element
     */
    private void setDrawingAttributes(Element element) {
        setDrawingAttributes(element, graphicsContexts.peek(), activeGC);
    }

    /**
     * Output rendering attributes which are different from the last set output.
     *
     * @param element XML element
     * @param gc new graphics context
     */
    private void setDrawingAttributes(Element element, GraphicsContext gc) {
        setDrawingAttributes(element, graphicsContexts.peek(), gc);
    }

    /**
     * Set the drawing attributes on the given element to the difference between
     * the two GraphicContexts. It is assumed that first GraphicsContext
     * represents the current SVG rendering state.
     *
     * @param element XML element to receive the attributes
     * @param gc1 current graphics context
     * @param gc2 new graphics context
     */
    private void setDrawingAttributes(Element element, GraphicsContext gc1,
            GraphicsContext gc2) {
        if (gc2.background != null && !gc2.background.equals(gc1.background)) {
            element.setAttribute("fill", toHexString(gc2.background));
        }
        if (!gc2.foreground.equals(gc1.foreground)) {
            element.setAttribute("stroke", toHexString(gc2.foreground));
        }
        int alpha = gc2.foreground.getAlpha();
        if (alpha != gc1.foreground.getAlpha()) {
            NumberFormat nf = NumberFormat.getNumberInstance();
            nf.setMaximumFractionDigits(3);
            element.setAttribute("opacity", "" + nf.format(alpha / 255.0));
        }

        if (gc2.stroke != null && !gc2.stroke.equals(gc1.stroke)) {
            Stroke s = gc2.stroke;
            if (s instanceof BasicStroke) {
                String dashString = "";
                float[] dashes = ((BasicStroke) s).getDashArray();
                if (dashes != null && dashes.length > 1) {
                    for (float dash : dashes) {
                        dashString = dashString + dash + ", ";
                    }
                    dashString = dashString.substring(0,
                            dashString.length() - 2);
                    element.setAttribute("stroke-dasharray", dashString);
                }
                element.setAttribute("stroke-width", ""
                        + ((BasicStroke) s).getLineWidth());
            } else {
                element.setAttribute("stroke-width", "1");
            }
        }
    }

    /**
     * Begin a new top level fig. All drawing commands between this call and the
     * call to endFig will be grouped in an SVG group.
     *
     * @param fig the fig which be painted next
     */
    public void beginFig(Fig fig) {
        beginFig(fig, null, null);
    }

    /**
     * Begin fig with an optional link and class(s).
     *
     * @param fig
     * @param cssClass
     * @param url
     */
    public void beginFig(Fig fig, String cssClass, String url) {
        if (url != null) {
            createLink(url);
        }

        Element group = svg.createElement("g");
        if (cssClass != null) {
            group.setAttribute("class", cssClass);
        }
        GraphicsContext gc = new GraphicsContext(fig.getLineColor(),
                fig.getFillColor(), fig.getLineWidth());
        setDrawingAttributes(group, graphicsContexts.peek(), gc);
        graphicsContexts.push(gc);
        elements.push(group);
    }

    private void createLink(String url) {
        Element link = svg.createElement("a");
        link.setAttribute("xlink:href", url);
        elements.push(link);
    }

    /**
     * End the current fig and pop related graphic context.
     */
    public void endFig() {
        // Careful - needs to be synchronized with begin fig
        popElement();
        popElement();
        graphicsContexts.pop();
    }

    private Element popElement() {
        Element popped = elements.pop();
        Element top = elements.peek();
        if (top == null) {
            svg.appendChild(popped);
        } else {
            top.appendChild(popped);
        }
        // TODO: Are we more interested in the current top or the last top?
        // or neither?
        return popped;
    }

    private class Stack<T> extends LinkedList<T> {

        public void push(T e) {
            addFirst(e);
        }

        public T pop() {
            return removeFirst();
        }
    }

}
