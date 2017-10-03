// Copyright (c) 1996-99 The Regents of the University of California. All
// Rights Reserved. Permission to use, copy, modify, and distribute this
// software and its documentation without fee, and without a written
// agreement is hereby granted, provided that the above copyright notice
// and this paragraph appear in all copies. This software program and
// documentation are copyrighted by The Regents of the University of
// California. The software program and documentation are supplied "AS
// IS", without any accompanying services from The Regents. The Regents
// does not warrant that the operation of the program will be
// uninterrupted or error-free. The end-user understands that the program
// was developed for research purposes and is advised not to rely
// exclusively on the program for any reason. IN NO EVENT SHALL THE
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
// File: ArrowHead.java
// Classes: ArrowHead
// Original Author: abonner@ics.uci.edu
// $Id: ArrowHead.java 1238 2009-06-27 17:26:13Z bobtarling $
package org.tigris.gef.presentation;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Stroke;

/**
 * Abstract class to draw arrow heads on the ends of FigEdges.
 */
public abstract class ArrowHead extends Decoration {

    public ArrowHead() {
        super();
    }

    public ArrowHead(Color line, Color fill) {
        super(line, fill);
    }

    final public void paintAtHead(Object g, Fig path) {
        paintAtHead((Graphics) g, path);
    }

    public void paintAtHead(Graphics g, Fig path) {
        if (g instanceof Graphics2D) {
            Graphics2D g2 = (Graphics2D) g;
            Stroke oldStroke = g2.getStroke();
            g2.setStroke(new BasicStroke(path.getLineWidth()));
            int[] xs = path.getXs();
            int[] ys = path.getYs();
            paint(g2,
                    new Point(xs[1], ys[1]),
                    new Point(xs[0], ys[0]),
                    path.getLineColor(),
                    path.getFillColor());
            g2.setStroke(oldStroke);
        } else {
            int[] xs = path.getXs();
            int[] ys = path.getYs();
            paint((Graphics) g,
                    new Point(xs[1], ys[1]),
                    new Point(xs[0], ys[0]),
                    path.getLineColor(),
                    path.getFillColor());
        }
    }

    final public void paintAtTail(Object g, Fig path) {
        paintAtTail((Graphics) g, path);
    }

    public void paintAtTail(Graphics g, Fig path) {
        if (g instanceof Graphics2D) {
            Graphics2D g2 = (Graphics2D) g;
            Stroke oldStroke = g2.getStroke();
            g2.setStroke(new BasicStroke(path.getLineWidth()));
            int[] xs = path.getXs();
            int[] ys = path.getYs();
            int pointCount = path.getNumPoints();
            paint(g2,
                    new Point(xs[pointCount - 2], ys[pointCount - 2]),
                    new Point(xs[pointCount - 1], ys[pointCount - 1]),
                    path.getLineColor(),
                    path.getFillColor());
            g2.setStroke(oldStroke);
        } else {
            int pointCount = path.getNumPoints();
            int[] xs = path.getXs();
            int[] ys = path.getYs();
            paint((Graphics) g,
                    new Point(xs[pointCount - 2], ys[pointCount - 2]),
                    new Point(xs[pointCount - 1], ys[pointCount - 1]),
                    path.getLineColor(),
                    path.getFillColor());
        }
    }
} /* end class ArrowHead */
