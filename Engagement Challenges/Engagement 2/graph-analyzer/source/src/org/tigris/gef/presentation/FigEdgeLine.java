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
// File: FigEdgeLine.java
// Classes: FigEdgeLine
// Original Author: jrobbins@ics.uci.edu
// $Id: FigEdgeLine.java 1153 2008-11-30 16:14:45Z bobtarling $
package org.tigris.gef.presentation;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;

/**
 * An FigEdge that shows a straight line from the source port to the destination
 * port.
 */
public class FigEdgeLine extends FigEdge {

    private static final long serialVersionUID = 7010064753682104591L;

    /**
     * Instanciate a new FigLine as the contained Fig. By default it is black
     * and the FigEdge has no ArrowHeads.
     */
    protected Fig makeEdgeFig() {
        return new FigLine(0, 0, 0, 0, Color.black);
    }

    /**
     * Compute the shape of the line that presents an Edge.
     */
    public void computeRouteImpl() {
        Fig sourcePortFig = getSourcePortFig();
        Fig destPortFig = getDestPortFig();
        Point srcPt = sourcePortFig.getCenter();
        Point dstPt = destPortFig.getCenter();

        if (_useNearest) {
            // ? two iterations of refinement, maybe should be a for-loop
            srcPt = sourcePortFig.connectionPoint(dstPt);
            dstPt = destPortFig.connectionPoint(srcPt);
            srcPt = sourcePortFig.connectionPoint(dstPt);
            dstPt = destPortFig.connectionPoint(srcPt);
        }

        ((FigLine) getFig()).setShape(srcPt, dstPt);
        calcBounds();
    }

    public void paint(Graphics graphicContext) {
        super.paint(graphicContext);
        Graphics g = (Graphics) graphicContext;
        if (_highlight) {
            FigLine f = (FigLine) getFig();
            paintHighlightLine(g, f.getX1(), f.getY1(), f.getX2(), f.getY2());
        }
    }
} /* end class FigEdgeLine */
