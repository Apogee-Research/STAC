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
// File: PathConvPercentPlusConst.java
// Classes: PathConvPercentPlusConst
// Original Author: abonner@ics.uci.edu
// $Id: PathConvPercentPlusConst.java 1153 2008-11-30 16:14:45Z bobtarling $
package org.tigris.gef.base;

import java.awt.Point;

import org.tigris.gef.presentation.*;

/**
 * Used to place labels as specific positions along a FigEdge. For example, a
 * label can be placed in the middle of a FigEdge by using 50%.
 */
public class PathConvPercentPlusConst extends PathConv {

    private static final long serialVersionUID = 365473229021070199L;
    private int percent = 0;
    private int _delta = 0;
    private int offset = 0;

    public PathConvPercentPlusConst(Fig theFig, int newPercent, int delta,
            int newOffset) {
        super(theFig);
        setPercentOffset(newPercent, newOffset);
        _delta = delta;
    }

    public void stuffPoint(Point res) {
        Fig pathFig = getPathFig();
        int figLength = pathFig.getPerimeterLength();
        if (figLength < 10) {
            res.setLocation(pathFig.getCenter());
            return;
        }
        int pointToGet = ((figLength * percent) / 100) + _delta;

        if (pointToGet < 0) {
            pointToGet = 0;
        }
        if (pointToGet > figLength) {
            pointToGet = figLength;
        }

        pathFig.stuffPointAlongPerimeter(pointToGet, res);

        // org.graph.commons.logging.LogFactory.getLog(null).info("lP=" + linePoint + " ptG=" + pointToGet +
        // " figLen=" + figLength);
        applyOffsetAmount(pathFig.pointAlongPerimeter(pointToGet + 5), pathFig
                .pointAlongPerimeter(pointToGet - 5), offset, res);
    }

    public void setPercentOffset(int newPercent, int newOffset) {
        percent = newPercent;
        offset = newOffset;
    }

    public void setClosestPoint(Point newPoint) {
    }
}/* end class PathConvPercentPlusConst */
