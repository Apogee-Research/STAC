// %1031243478619:org.tigris.gef.persistence%
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
// File: SVGWriter.java
// Classes: SVGWriter
// Original Author: Andreas Rueckert <a_rueckert@gmx.net>
// $Id: SVGWriter.java 1326 2011-05-17 09:49:01Z bobtarling $
package org.tigris.gef.persistence;

//import javax.xml.parsers.DocumentBuilder;
//import javax.xml.parsers.DocumentBuilderFactory;
import org.tigris.gef.persistence.export.SvgWriter;
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
 * since v0.12.4. Use SVGGraphics2D from Apache Batik
 */
public class SVGWriter extends Graphics {

    private SvgWriter writer;

    public SVGWriter(OutputStream stream, Rectangle drawingArea)
            throws IOException, Exception {
        writer = new SvgWriter(stream, drawingArea);
    }

    public Graphics create() {
        return writer.create();
    }

    public Graphics create(int x, int y, int width, int height) {
        return writer.create(x, y, width, height);
    }

    public void dispose() {
        writer.dispose();
    }

    /*public void printDOMTree(Node node) {
     writer.printDOMTree(node);
     }*/
    /**
     * Get the current color for drawing operations.
     *
     * @return The current color for drawing operations.
     */
    public Color getColor() {
        return writer.getColor();
    }

    /**
     * Set the current color for drawing operations.
     *
     * @param c The new color for drawing operations.
     */
    public void setColor(Color c) {
        writer.setColor(c);
    }

    public void setPaintMode() {
        writer.setPaintMode();
    }

    public void setXORMode(Color otherColor) {
        writer.setXORMode(otherColor);
    }

    public Font getFont() {
        return writer.getFont();
    }

    public void setFont(Font font) {
        writer.setFont(font);
    }

    public FontMetrics getFontMetrics() {
        return writer.getFontMetrics();
    }

    public FontMetrics getFontMetrics(Font font) {
        return writer.getFontMetrics(font);
    }

    public void copyArea(int x, int y, int width, int height, int dx, int dy) {
        writer.copyArea(x, y, width, height, dx, dy);
    }

    public boolean drawImage(Image img, int x, int y, ImageObserver observer) {
        return writer.drawImage(img, x, y, observer);
    }

    public boolean drawImage(Image img, int x, int y, int w, int h,
            ImageObserver observer) {
        return writer.drawImage(img, x, y, w, h, observer);
    }

    public boolean drawImage(Image img, int x, int y, Color bgcolor,
            ImageObserver observer) {
        return writer.drawImage(img, x, y, bgcolor, observer);
    }

    public boolean drawImage(Image img, int x, int y, int width, int height,
            Color bgcolor, ImageObserver observer) {
        return writer.drawImage(img, x, y, width, height, bgcolor, observer);
    }

    public boolean drawImage(Image img, int dx1, int dy1, int dx2, int dy2,
            int sx1, int sy1, int sx2, int sy2, ImageObserver observer) {
        return writer.drawImage(img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2,
                observer);
    }

    public boolean drawImage(Image img, int dx1, int dy1, int dx2, int dy2,
            int sx1, int sy1, int sx2, int sy2, Color bgcolor,
            ImageObserver observer) {
        return writer.drawImage(img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2,
                bgcolor, observer);
    }

    public void drawRect(int x, int y, int w, int h) {
        writer.drawRect(x, y, w, h);
    }

    public void fillRect(int x, int y, int w, int h) {
        writer.fillRect(x, y, w, h);
    }

    public void clearRect(int x, int y, int w, int h) {
        writer.clearRect(x, y, w, h);
    }

    public void drawOval(int x, int y, int w, int h) {
        writer.drawOval(x, y, w, h);
    }

    public void fillOval(int x, int y, int w, int h) {
        writer.fillOval(x, y, w, h);
    }

    public void drawArc(int x, int y, int w, int h, int startAngle, int arcAngle) {
        writer.drawArc(x, y, w, h, startAngle, arcAngle);
    }

    public void fillArc(int x, int y, int w, int h, int startAngle, int arcAngle) {
        writer.fillArc(x, y, w, h, startAngle, arcAngle);
    }

    public void drawRoundRect(int x, int y, int w, int h, int arcw, int arch) {
        writer.drawRoundRect(x, y, w, h, arcw, arch);
    }

    public void fillRoundRect(int x, int y, int w, int h, int arcw, int arch) {
        writer.fillRoundRect(x, y, w, h, arcw, arch);
    }

    public void drawPolygon(int[] xPoints, int[] yPoints, int nPoints) {
        writer.drawPolygon(xPoints, yPoints, nPoints);
    }

    public void drawPolygon(Polygon poly) {
        writer.drawPolygon(poly);
    }

    public void fillPolygon(int[] xPoints, int[] yPoints, int nPoints) {
        writer.fillPolygon(xPoints, yPoints, nPoints);
    }

    public void fillPolygon(Polygon poly) {
        writer.fillPolygon(poly);
    }

    public void drawPolyline(int[] xPoints, int[] yPoints, int nPoints) {
        writer.drawPolyline(xPoints, yPoints, nPoints);
    }

    public void drawLine(int x1, int y1, int x2, int y2) {
        writer.drawLine(x1, y1, x2, y2);
    }

    public void setClip(int x, int y, int w, int h) {
        writer.setClip(x, y, w, h);
    }

    public void setClip(Shape clip) {
        writer.setClip(clip);
    }

    public Rectangle getClipBounds() {
        return writer.getClipBounds();
    }

    public void clipRect(int x, int y, int w, int h) {
        writer.clipRect(x, y, w, h);
    }

    public Shape getClip() {
        return writer.getClip();
    }

    public void translate(int x, int y) {
        writer.translate(x, y);
    }

    public void scale(double xscale, double yscale) {
        writer.scale(xscale, yscale);
    }

    /**
     * Draw a string at a given position.
     *
     * @param t The string to draw.
     * @param x The horizontal position of the text.
     * @param y The vertical position of the text.
     */
    public void drawString(String t, int x, int y) {
        writer.drawString(t, x, y);
    }

    // if you want to compile this with jdk1.1, you have to comment out this
    // method.
    // if you want to compile this with jdk1.2, you MUST NOT comment out this
    // method.
    // Did sun make a good job implementing jdk1.2? :-(((
    public void drawString(java.text.AttributedCharacterIterator aci, int i1,
            int i2) {
        writer.drawString(aci, i1, i2);
    }

    public void drawString(java.text.CharacterIterator aci, int i1, int i2) {
        writer.drawString(aci, i1, i2);
    }
}
