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
// File: FigRect.java
// Classes: FigRect
// Original Author: ics125 spring 1996
// $Id: FigRect.java 1250 2009-06-28 21:39:46Z bobtarling $
package org.tigris.gef.presentation;

import java.awt.Color;
import java.awt.Graphics;
import java.io.Serializable;

/**
 * Primitive Fig to paint rectangles on a LayerDiagram.
 */
public class FigRect extends Fig implements Serializable {

    private static final long serialVersionUID = -6171328584588911037L;

    /**
     * Construct a new resizable FigRect with the given position and size.
     */
    public FigRect(
            final int x,
            final int y,
            final int w,
            final int h) {
        super(x, y, w, h);
    }

    /**
     * Construct a new resizable FigRect with the given position, size, line
     * color, and fill color.
     */
    public FigRect(
            final int x,
            final int y,
            final int w,
            final int h,
            final Color lColor,
            final Color fColor) {
        super(x, y, w, h, lColor, fColor);
    }

    /**
     * Construct a new FigRect w/ the given position and size.
     */
    public FigRect(
            final int x,
            final int y,
            final int w,
            final int h,
            final boolean resizable) {
        super(x, y, w, h);
        this.resizable = resizable;
    }

    /**
     * Construct a new FigRect w/ the given position, size, line color, and fill
     * color.
     */
    public FigRect(
            final int x,
            final int y,
            final int w,
            final int h,
            final boolean resizable,
            final Color lColor,
            final Color fColor) {
        super(x, y, w, h, lColor, fColor);
        this.resizable = resizable;
    }

    // //////////////////////////////////////////////////////////////
    // painting methods
    /**
     * Paint this FigRect
     */
    public void paint(final Graphics g) {
        drawRect(
                g,
                isFilled(),
                getFillColor(),
                getLineWidth(),
                getLineColor(),
                getX(),
                getY(),
                getWidth(),
                getHeight(),
                getDashed(),
                _dashes,
                _dashPeriod);
    }

    public void appendSvg(StringBuffer sb) {
        sb.append("<rect id='").append(getId()).append("' x='").append(getX())
                .append("' y='").append(getY()).append("' width='").append(
                        getWidth()).append("' height='").append(getHeight())
                .append("'");
        appendSvgStyle(sb);
        sb.append(" />");
    }
}
