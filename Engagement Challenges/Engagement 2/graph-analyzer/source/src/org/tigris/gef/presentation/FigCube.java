// $Id: FigCube.java 1234 2009-05-31 16:42:39Z tfmorris $
// Copyright (c) 1996,2009 The Regents of the University of California. All
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
package org.tigris.gef.presentation;

import java.awt.Color;
import java.awt.Graphics;
import java.io.Serializable;

/**
 * This class is needed to paint cubes (the only 3dim Element in UML)
 *
 * @author 5eichler@informatik.uni-hamburg.de
 */
public class FigCube extends Fig implements Serializable {

    private static final long serialVersionUID = 7798364480460523733L;
    private int D = 20;

    public FigCube(int x, int y, int w, int h, Color lColor, Color fColor) {
        super(x, y, w, h, lColor, fColor);
    }

    public FigCube(int x, int y, int w, int h) {
        super(x, y, w, h);
    }

    public void paint(Graphics g) {

        final Color fillColor = getFillColor();
        final Color lineColor = getLineColor();
        final int x = getX();
        final int y = getY();
        final int w = getWidth();
        final int h = getHeight();

        g.setColor(fillColor);
        g.fillRect(x, y, w, h);
        g.setColor(lineColor);
        g.drawRect(x, y, w, h);

        g.setColor(fillColor);
        g.fillPolygon(new int[]{x, x + D, x + w + D, x + w}, new int[]{y,
            y - D, y - D, y}, 4);
        g.setColor(lineColor);
        g.drawPolygon(new int[]{x, x + D, x + w + D, x + w}, new int[]{y,
            y - D, y - D, y}, 4);

        g.setColor(fillColor);
        g.fillPolygon(new int[]{x + w + D, x + w + D, x + w, x + w},
                new int[]{y - D, y + h - D, y + h, y}, 4);
        g.setColor(lineColor);
        g.drawPolygon(new int[]{x + w + D, x + w + D, x + w, x + w},
                new int[]{y - D, y + h - D, y + h, y}, 4);
    }

    /**
     * @return the depth (the 3rd dimension) of the cube
     */
    public int getDepth() {
        return D;
    }

    /**
     * @param d the depth (the 3rd dimension) of the cube
     */
    public void setDepth(int depth) {
        D = depth;
    }

    public void appendSvg(StringBuffer sb) {
        sb.append("<rect id='").append(getId()).append("' x='").append(getX())
                .append("' y='").append(getY()).append("' width='")
                .append(getWidth()).append("' height='").append(getHeight())
                .append("'");
        appendSvgStyle(sb);
        sb.append(" />");
    }
}
