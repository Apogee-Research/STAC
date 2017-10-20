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
// File: FigRRect.java
// Classes: FigRRect
// Original Author: ics125 spring 1996
// $Id: FigRRect.java 1153 2008-11-30 16:14:45Z bobtarling $
package org.tigris.gef.presentation;

import java.awt.Color;
import java.awt.Graphics;

import org.tigris.gef.properties.*;

/**
 * Primitive Fig to paint rounded rectangles on a LayerDiagram.
 */
public class FigRRect extends FigRect {

    private static final long serialVersionUID = -4984437962118691063L;

    protected int _radius = 16;

    // //////////////////////////////////////////////////////////////
    // static initializer
    static {
        PropCategoryManager.categorizeProperty("Geometry", "cornerRadius");
    }

    // //////////////////////////////////////////////////////////////
    // constructors
    /**
     * Construct a new FigRRect w/ the given position and size
     */
    public FigRRect(int x, int y, int w, int h) {
        super(x, y, w, h);
    }

    /**
     * Construct a new FigRRect w/ the given position, size, line color, and
     * fill color
     */
    public FigRRect(int x, int y, int w, int h, Color lineColor, Color fillColor) {
        super(x, y, w, h, lineColor, fillColor);
    }

    /**
     * get and set the "roundness" of the corners. USED by PGML.tee
     */
    public int getCornerRadius() {
        return _radius;
    }

    public void setCornerRadius(int r) {
        _radius = r;
    }

    /**
     * Paint this FigRRect. Dashed lines aren't currently handled.
     */
    public void paint(Graphics g) {
        if (_filled && _fillColor != null) {
            if (_lineColor != null && _lineWidth > 1) {
                drawFilledRRectWithWideLine(g);
            } else {
                drawFilledRRect(g);
            }
        } else if (_lineColor != null && _lineWidth > 1) {
            drawEmptyRRectWithWideLine(g);
        } else {
            drawEmptyRRect(g);
        }
    }

    /**
     * Paint a filled rounded rectangle (with a narrow line or no line)
     *
     * @param g
     */
    private void drawFilledRRect(Graphics g) {
        // assert _lineWidth == 0 || _lineWidth == 1 || _lineColor == null
        // assert filled && filledColor != null
        // Do the actual fill color
        g.setColor(_fillColor);
        g.fillRoundRect(_x, _y, _w, _h, _radius, _radius);

        if (_lineColor != null && _lineWidth == 1) {
            // If we're filled with a narrow border then draw
            // the border over the already filled area.
            g.setColor(_lineColor);
            g.drawRoundRect(_x, _y, _w, _h, _radius, _radius);
        }
    }

    /**
     * Paint a filled rounded rectangle with a wide line
     *
     * @param g
     */
    private void drawFilledRRectWithWideLine(Graphics g) {
        // assert _lineWidth > 1 && _lineColor != null
        // assert filled && filledColor != null
        // If we're filled with a wide border then fill
        // the entire rectangle with the border color and then
        // recalculate area for the actual fill.
        int lineWidth2 = _lineWidth * 2;
        g.setColor(_lineColor);
        g.fillRoundRect(_x, _y, _w, _h, _radius, _radius);

        // Do the actual fill color
        g.setColor(_fillColor);
        g.fillRoundRect(_x + _lineWidth, _y + _lineWidth, _w - lineWidth2, _h
                - lineWidth2, _radius, _radius);
    }

    /**
     * Paint an unfilled rounded rectangle (with a narrow line or no line)
     *
     * @param g
     */
    private void drawEmptyRRect(Graphics g) {
        // If there's no fill but a narrow line then just
        // draw that line.
        if (_lineColor != null && _lineWidth == 1) {
            g.setColor(_lineColor);
            g.drawRoundRect(_x, _y, _w, _h, _radius, _radius);
        }
    }

    /**
     * Paint an unfilled rounded rectangle with a wide line
     *
     * @param g
     */
    private void drawEmptyRRectWithWideLine(Graphics g) {
        // If there's no fill but a wide line then draw repeated
        // rounded rectangles in ever decreasing size.
        if (_lineColor != null && _lineWidth > 1) {
            int xx = _x;
            int yy = _y;
            int ww = _w;
            int hh = _h;
            g.setColor(_lineColor);
            for (int i = 0; i < _lineWidth; ++i) {
                g.drawRoundRect(xx++, yy++, ww, hh, _radius, _radius);
                ww -= 2;
                hh -= 2;
            }
        }
    }
} /* end class FigRRect */
