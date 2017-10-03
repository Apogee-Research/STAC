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
// File: ArrowHeadComposite.java
// Classes: ArrowHeadComposite
// Original Author: abonner@ics.uci.edu
// $Id: ArrowHeadComposite.java 1238 2009-06-27 17:26:13Z bobtarling $
package org.tigris.gef.presentation;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.util.*;

/**
 * Abstract class to draw arrow heads on the ends of FigEdges.
 */
public class ArrowHeadComposite extends ArrowHead {

    /**
     *
     */
    private static final long serialVersionUID = -9079999150349589867L;
    protected Vector _arrowHeads = new Vector();

    public ArrowHeadComposite() {
    }

    public ArrowHeadComposite(ArrowHead ah1, ArrowHead ah2) {
        _arrowHeads.addElement(ah1);
        _arrowHeads.addElement(ah2);
    }

    public void addArrowHead(ArrowHead ah) {
        _arrowHeads.addElement(ah);
    }

    public void paint(
            final Graphics g,
            final Point start,
            final Point end,
            final Color lineColor,
            final Color fillColor) {
        System.out
                .println("paint3 in ArrowHeadComposite should never be called");
    }

    public void paintAtHead(Graphics g, Fig path) {
        int size = _arrowHeads.size();
        for (int i = 0; i < size; i++) {
            ArrowHead ah = (ArrowHead) _arrowHeads.elementAt(i);
            ah.paint((Graphics) g,
                    path.pointAlongPerimeter((i + 1) * getHeight() * 2),
                    path.pointAlongPerimeter(i * getHeight() * 2),
                    path.getLineColor(),
                    path.getFillColor());
        }
    }

    public void paintAtTail(Graphics g, Fig path) {
        int len = path.getPerimeterLength();
        int size = _arrowHeads.size();
        for (int i = 0; i < size; i++) {
            ArrowHead ah = (ArrowHead) _arrowHeads.elementAt(i);
            ah.paint((Graphics) g,
                    path.pointAlongPerimeter(len - 1 - (i + 1) * getHeight() * 2),
                    path.pointAlongPerimeter(len - 1 - i * getHeight() * 2),
                    path.getLineColor(),
                    path.getFillColor());
        }
    }
} /* end class ArrowHeadComposite */
