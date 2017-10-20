// %1031243478619:org.tigris.gef.persistence%
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
// File: SVGWriter.java
// Classes: SVGWriter
// Original Author: Andreas Rueckert <a_rueckert@gmx.net>
// $Id: SvgWriter.java 1326 2011-05-17 09:49:01Z bobtarling $
package org.tigris.gef.persistence.export;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.awt.*;
import java.awt.image.ImageObserver;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;

/**
 * use SVGGraphics2D from Apache Batik
 */
public class SvgWriter extends Graphics {

    /**
     *
     */
    private static class Utf8Writer {

        private OutputStreamWriter _writer;

        public Utf8Writer(OutputStream out) {

            try {
                _writer = new OutputStreamWriter(out, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                System.err
                        .println("[SVGWriter] UTF-8 not supported. Switching to default."
                                + e);
                _writer = new OutputStreamWriter(out);
            }
        }

        public void print(String s) {

            try {
                _writer.write(s);
            } catch (IOException e) {
                System.err.println("[SVGWriter] " + e);
            }
        }

        public void print(char c) {

            try {
                _writer.write(c);
            } catch (IOException e) {
                System.err.println("[SVGWriter] " + e);
            }
        }

        public void println(String s) {

            try {
                _writer.write(s);
                _writer.write('\n');
            } catch (IOException e) {
                System.err.println("[SVGWriter] " + e);
            }
        }

        public void close() {

            try {
                _writer.close();
            } catch (IOException e) {
                System.err.println("[SVGWriter] " + e);
            }
        }
    }

    private Utf8Writer _writer;
    Document _svg;
    Element _root;

    /**
     * The current color;
     */
    private Color _fColor = Color.black;

    /**
     * The current background color.
     */
    private Color _bgColor = Color.white;

    /**
     * The drawing area for the SVG output.
     */
    private Rectangle _drawingArea;

    /**
     * The current font.
     */
    private Font _font = new Font("Verdana", Font.PLAIN, 8);
    private Rectangle _clip;

    // To keep the SVG output as simple as possible, I handle all
    // the transformations and the scaling in the writer.
    private int _xOffset = 0;
    private int _yOffset = 0;
    private int _hInset = 10;
    private int _vInset = 10;
    private double xScale = 1.0;
    private double yScale = 1.0;
    private String SVGns = "http://www.w3.org/2000/svg";

    /**
     * Flag that marks if we need to write the DOCTYPE and the XML document node
     * of the SVG
     */
    private boolean isInline = false;

    public SvgWriter(OutputStream stream, Rectangle drawingArea)
            throws IOException, Exception {
        _writer = new Utf8Writer(stream);
        _drawingArea = drawingArea;
        translate(_hInset - drawingArea.x, _vInset - drawingArea.y);
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

        factory.setNamespaceAware(false);
        factory.setValidating(false);
        DocumentBuilder builder = factory.newDocumentBuilder();

        _svg = builder.newDocument();
        _root = _svg.createElement("svg");
        _root.setAttribute("xmlns", SVGns);
        _root.setAttribute("width", ""
                + (2 * _hInset + scaleX(_drawingArea.width)));
        _root.setAttribute("height", ""
                + (2 * _vInset + scaleY(_drawingArea.height)));
        _root.setAttribute("version", "1.1");
    }

    /**
     *
     * @param stream
     * @param drawingArea
     * @param isInline If false, it writes the DOCTYPE and XML nodes. If true,
     * we don't write them (think on inline SVG)
     * @throws IOException
     * @throws Exception
     */
    public SvgWriter(OutputStream stream, Rectangle drawingArea,
            boolean isInline) throws IOException, Exception {
        this(stream, drawingArea);
        this.isInline = isInline;
    }

    public Graphics create() {
        return this;
    }

    public Graphics create(int x, int y, int width, int height) {
        return this;
    }

    public void dispose() {
        _svg.appendChild(_root);
        printDOMTree(_svg);
        _writer.close();
    }

    public void printDOMTree(Node node) {
        int type = node.getNodeType();

        switch (type) {

            // print the document element
            case Node.DOCUMENT_NODE: {
                if (!isInline) {
                    _writer.println("<?xml version=\"1.0\" encoding=\"utf-8\" ?>");
                    _writer
                            .print("<!DOCTYPE svg PUBLIC "
                                    + "\"-//W3C//DTD SVG 1.1//EN\" "
                                    + "\"http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd\">\n");
                }
                printDOMTree(((Document) node).getDocumentElement());
                break;
            }

            // print element with attributes
            case Node.ELEMENT_NODE: {
                _writer.print("<");
                _writer.print(node.getNodeName());
                NamedNodeMap attrs = node.getAttributes();

                for (int i = 0; i < attrs.getLength(); i++) {
                    Node attr = attrs.item(i);

                    _writer.print(" " + attr.getNodeName() + "=\""
                            + attr.getNodeValue() + "\"");
                }

                NodeList children = node.getChildNodes();

                if (children.getLength() > 0) {
                    _writer.println(">");
                    int len = children.getLength();

                    for (int i = 0; i < len; i++) {
                        printDOMTree(children.item(i));
                    }

                    _writer.print("</");
                    _writer.print(node.getNodeName());
                    _writer.println(">");
                } else {
                    _writer.println("/>");
                }

                break;
            }

            // handle entity reference nodes
            case Node.ENTITY_REFERENCE_NODE: {
                _writer.print("&");
                _writer.print(node.getNodeName());
                _writer.print(";");
                break;
            }

            // print cdata sections
            case Node.CDATA_SECTION_NODE: {
                _writer.print("<![CDATA[");
                _writer.print(node.getNodeValue());
                _writer.print("]]>");
                break;
            }

            // print text
            case Node.TEXT_NODE: {
                String text = node.getNodeValue();

                for (int i = 0; i < text.length(); i++) {

                    // escape reserved characters
                    switch (text.charAt(i)) {

                        case '&': {
                            _writer.print("&amp;");
                            break;
                        }

                        case '<': {
                            _writer.print("&lt;");
                            break;
                        }

                        case '>': {
                            _writer.print("&gt;");
                            break;
                        }

                        default:
                            _writer.print(text.charAt(i));
                    }
                }

                break;
            }

            // print processing instruction
            case Node.PROCESSING_INSTRUCTION_NODE: {
                _writer.print("<?");
                _writer.print(node.getNodeName());
                String data = node.getNodeValue();

                {
                    _writer.print("");
                    _writer.print(data);
                }

                _writer.print("?>");
                break;
            }
        }
    }

    /**
     * Get the current color for drawing operations.
     *
     * @return The current color for drawing operations.
     */
    public Color getColor() {
        return _fColor;
    }

    /**
     * Return a String representation of the the current color.
     *
     * @return The current color as a String (like #FF00BF).
     */
    private String getColorAsString() {

        // Remove the alpha channel info from the string representation.
        return "#" + Integer.toHexString(_fColor.getRGB()).substring(2);
    }

    /**
     * Set the current color for drawing operations.
     *
     * @param c The new color for drawing operations.
     */
    public void setColor(Color c) {
        _fColor = c;
    }

    /**
     * Get the current background color.
     *
     * @return The current background color.
     */
    private Color getBackgroundColor() {
        return _bgColor;
    }

    /**
     * Get a String representation for the current background color.
     *
     * @return The current background color as a String (like #BF00FF).
     */
    private String getBackgroundColorAsString() {

        // Remove the alpha channel info from the string representation.
        return "#" + Integer.toHexString(_bgColor.getRGB()).substring(2);
    }

    /**
     * Set the new background color.
     *
     * @param c The new background color.
     */
    private void setBackgroundColor(Color c) {
        _bgColor = c;
    }

    public void setPaintMode() {
    }

    public void setXORMode(Color otherColor) {
    }

    public Font getFont() {
        return _font;
    }

    public void setFont(Font font) {
        _font = font;
    }

    public FontMetrics getFontMetrics() {
        return getFontMetrics(_font);
    }

    public FontMetrics getFontMetrics(Font font) {
        return FontUtility.getFontMetrics(font);
    }

    public void copyArea(int x, int y, int width, int height, int dx, int dy) {
    }

    public boolean drawImage(Image img, int x, int y, ImageObserver observer) {
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
         * p.print("0"); _writer.println(); } if (ih % 2 == 1) { for (int i = 0;
         * i < 3 * (iw + iw % 2); i++) p.print("0"); _writer.println(); }
         * p.println("grestore");
         */
        return true;
    }

    public boolean drawImage(Image img, int x, int y, Color bgcolor,
            ImageObserver observer) {
        return false;
    }

    public boolean drawImage(Image img, int x, int y, int width, int height,
            Color bgcolor, ImageObserver observer) {
        return false;
    }

    public boolean drawImage(Image img, int dx1, int dy1, int dx2, int dy2,
            int sx1, int sy1, int sx2, int sy2, ImageObserver observer) {
        return false;
    }

    public boolean drawImage(Image img, int dx1, int dy1, int dx2, int dy2,
            int sx1, int sy1, int sx2, int sy2, Color bgcolor,
            ImageObserver observer) {
        return false;
    }

    private int scaleX(int x) {
        return (int) (x * xScale);
    }

    private int scaleY(int y) {
        return (int) (y * yScale);
    }

    private int transformX(int x) {
        return scaleX(x) + _xOffset;
    }

    private int transformY(int y) {
        return scaleY(y) + _yOffset;
    }

    private void drawRect(int x, int y, int w, int h, String fill,
            String stroke, String strokeWidth) {

        Element rect = _svg.createElement("rect");

        rect.setAttribute("x", "" + transformX(x));
        rect.setAttribute("y", "" + transformY(y));
        rect.setAttribute("width", "" + scaleX(w));
        rect.setAttribute("height", "" + scaleY(h));
        rect.setAttribute("fill", fill);
        rect.setAttribute("stroke", stroke);
        rect.setAttribute("stroke-width", strokeWidth);
        _root.appendChild(rect);
    }

    public void drawRect(int x, int y, int w, int h) {
        drawRect(x, y, w, h, "none", getColorAsString(), "1");
    }

    public void fillRect(int x, int y, int w, int h) {
        drawRect(x, y, w, h, getColorAsString(), getColorAsString(), "1");
    }

    public void clearRect(int x, int y, int w, int h) {
        drawRect(x, y, w, h, getBackgroundColorAsString(),
                getBackgroundColorAsString(), "1");
    }

    private void writeEllipsePath(int x, int y, int w, int h, int startAngle,
            int arcAngle) {

        /*
         * p.println("newpath"); int dx = w/2, dy = h/2; writeCoords(x + dx, y +
         * dy); writeCoords(dx, dy);
         * writeCoords(startAngle,-(startAngle+arcAngle)); p.println("ellipse");
         */
    }

    private void drawOval(int x, int y, int w, int h, String fill,
            String stroke, String strokeWidth) {
        Element oval = _svg.createElement("ellipse");

        oval.setAttribute("cx", "" + transformX(x + w / 2));
        oval.setAttribute("cy", "" + transformY(y + h / 2));
        oval.setAttribute("rx", "" + (double) scaleX(w) / 2);
        oval.setAttribute("ry", "" + (double) scaleY(h) / 2);
        oval.setAttribute("fill", fill);
        oval.setAttribute("stroke", stroke);
        oval.setAttribute("stroke-width", strokeWidth);
        _root.appendChild(oval);
    }

    public void drawOval(int x, int y, int w, int h) {
        drawOval(x, y, w, h, "none", getColorAsString(), "1");
    }

    public void fillOval(int x, int y, int w, int h) {
        drawOval(x, y, w, h, getColorAsString(), getColorAsString(), "1");
    }

    public void drawArc(int x, int y, int w, int h, int startAngle, int arcAngle) {

        /*
         * writeEllipsePath(x,y,w+1,h+1,startAngle,arcAngle);
         * p.println("stroke");
         */
    }

    public void fillArc(int x, int y, int w, int h, int startAngle, int arcAngle) {

        /*
         * writeEllipsePath(x,y,w,h,startAngle,arcAngle); p.println("eofill");
         */
    }

    private void drawRoundRect(int x, int y, int w, int h, int arcw, int arch,
            String fill, String stroke, String strokeWidth) {
        Element rect = _svg.createElement("rect");

        rect.setAttribute("x", "" + transformX(x));
        rect.setAttribute("y", "" + transformY(y));
        rect.setAttribute("width", "" + scaleX(w));
        rect.setAttribute("height", "" + scaleY(h));
        rect.setAttribute("rx", "" + scaleX(arcw));
        rect.setAttribute("ry", "" + scaleY(arch));
        rect.setAttribute("fill", fill);
        rect.setAttribute("stroke", stroke);
        rect.setAttribute("stroke-width", strokeWidth);

        _root.appendChild(rect);
    }

    public void drawRoundRect(int x, int y, int w, int h, int arcw, int arch) {
        drawRoundRect(x, y, w, h, arcw, arch, getBackgroundColorAsString(),
                getColorAsString(), "1");
    }

    public void fillRoundRect(int x, int y, int w, int h, int arcw, int arch) {
        drawRoundRect(x, y, w, h, arcw, arch, getColorAsString(),
                getColorAsString(), "1");
    }

    private void drawPolygon(int[] xPoints, int[] yPoints, int nPoints,
            String fill, String stroke, String strokeWidth) {
        double maxX = 0;
        double maxY = 0;
        Element polygon = _svg.createElement("polygon");

        polygon.setAttribute("fill", fill);
        polygon.setAttribute("stroke", stroke);
        polygon.setAttribute("stroke-width", strokeWidth);

        // Create the list of points for this tag.
        // I.e. points="100,100 150,150 200,200"
        StringBuffer pointList = new StringBuffer();

        for (int i = 0; i < nPoints; i++) {

            if (i > 0) {
                pointList.append(" ");
            }

            pointList.append("" + transformX(xPoints[i]) + ","
                    + transformY(yPoints[i]));

            if (transformX(xPoints[i]) > maxX) {
                maxX = transformX(xPoints[i]);
            }

            if (transformY(yPoints[i]) > maxY) {
                maxY = transformY(yPoints[i]);
            }
        }

        polygon.setAttribute("points", pointList.toString());
        _root.appendChild(polygon);
    }

    public void drawPolygon(int[] xPoints, int[] yPoints, int nPoints) {
        drawPolygon(xPoints, yPoints, nPoints, "none", getColorAsString(), "1");
    }

    public void drawPolygon(Polygon poly) {
        drawPolygon(poly.xpoints, poly.ypoints, poly.npoints);
    }

    public void fillPolygon(int[] xPoints, int[] yPoints, int nPoints) {
        drawPolygon(xPoints, yPoints, nPoints, getColorAsString(),
                getColorAsString(), "1");
    }

    public void fillPolygon(Polygon poly) {
        fillPolygon(poly.xpoints, poly.ypoints, poly.npoints);
    }

    public void drawPolyline(int[] xPoints, int[] yPoints, int nPoints) {
        double maxX = 0;
        double maxY = 0;
        Element polyline = _svg.createElement("polyline");

        polyline.setAttribute("fill", "none");
        polyline.setAttribute("stroke", getColorAsString());
        polyline.setAttribute("stroke-width", "1");

        // Create the list of points for this tag.
        // I.e. points="100,100 150,150 200,200"
        StringBuffer pointList = new StringBuffer();

        for (int i = 0; i < nPoints; i++) {

            if (i > 0) {
                pointList.append(" ");
            }

            pointList.append("" + transformX(xPoints[i]) + ","
                    + transformY(yPoints[i]));

            if (transformX(xPoints[i]) > maxX) {
                maxX = transformX(xPoints[i]);
            }

            if (transformY(yPoints[i]) > maxY) {
                maxY = transformY(yPoints[i]);
            }
        }

        polyline.setAttribute("points", pointList.toString());
        _root.appendChild(polyline);
    }

    public void drawLine(int x1, int y1, int x2, int y2) {
        Element line = _svg.createElement("line");

        line.setAttribute("x1", "" + transformX(x1));
        line.setAttribute("y1", "" + transformY(y1));
        line.setAttribute("x2", "" + transformX(x2));
        line.setAttribute("y2", "" + transformY(y2));
        line.setAttribute("fill", getColorAsString());
        line.setAttribute("stroke", getColorAsString());
        line.setAttribute("stroke-width", "1");

        _root.appendChild(line);
    }

    public void setClip(int x, int y, int w, int h) {
        _clip = new Rectangle(x, y, w, h);
    }

    public void setClip(Shape clip) {
        Rectangle bounds = clip.getBounds();
        setClip(bounds.x, bounds.y, bounds.width, bounds.height);
    }

    public Rectangle getClipBounds() {
        return _clip;
    }

    public void clipRect(int x, int y, int w, int h) {
        if (_clip == null) {
            setClip(x, y, w, h);
        } else {
            _clip = _clip.intersection(new Rectangle(x, y, w, h));
        }
    }

    public Shape getClip() {
        return _clip;
    }

    public void translate(int x, int y) {
        this._xOffset = x;
        this._yOffset = y;
    }

    public void scale(double xscale, double yscale) {
        this.xScale = xscale;
        this.yScale = yscale;
    }

    /**
     * Draw a string at a given position.
     *
     * @param t The string to draw.
     * @param x The horizontal position of the text.
     * @param y The vertical position of the text.
     */
    public void drawString(String t, int x, int y) {
        Element text = _svg.createElement("text");

        text.setAttribute("x", "" + transformX(x));
        text.setAttribute("y", "" + transformY(y));
        text.setAttribute("font-family", _font.getFamily());
        text.setAttribute("font-size", "" + _font.getSize());

        // If this is a bold font, add the appropriate attribute.
        if (getFont().isBold()) {
            text.setAttribute("font-weight", "bold");
        }

        // If this is a italic font, add the appropriate attribute.
        if (getFont().isItalic()) {
            text.setAttribute("font-style", "italic");
        }

        text.appendChild(_svg.createTextNode(t));
        _root.appendChild(text);
    }

    // if you want to compile this with jdk1.1, you have to comment out this
    // method.
    // if you want to compile this with jdk1.2, you MUST NOT comment out this
    // method.
    // Did sun make a good job implementing jdk1.2? :-(((
    public void drawString(java.text.AttributedCharacterIterator aci, int i1,
            int i2) {
    }

    public void drawString(java.text.CharacterIterator aci, int i1, int i2) {
    }
}
