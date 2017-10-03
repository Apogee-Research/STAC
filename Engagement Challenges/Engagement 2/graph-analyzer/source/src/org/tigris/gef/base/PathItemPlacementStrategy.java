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
// File: PathConv.java
// Classes: PathConv
// Original Author: abonner@ics.uci.edu
// $Id: PathConv.java 349 2003-12-27 22:47:11Z bobtarling $
package org.tigris.gef.base;

import java.awt.Graphics;
import java.awt.Point;

/**
 * An interface defining the methods required for path item placement.
 */
public interface PathItemPlacementStrategy {

    Point getPoint();

    /**
     * Method to visualize the algorithm of the path item placement strategy.
     *
     * This is implemented as an empty method in PathConv so any class extending
     * that has no painting of the algorithm by default.
     *
     * It is up to the client application to provide the algorithm when required
     * and also to call the strategy at the appropriate time.
     *
     * Should the strategy always be visible then the FigEdge paint method would
     * be responsible for painting the strategy. Should the strategy only appear
     * on selection then the appropriate Selection classes paint method should
     * be responsible.
     */
    void paint(Graphics g);

    /**
     * Method for setting new locations of path item Figs by specifying a new
     * point in the x,y coordinate space.
     *
     * @param newPoint
     */
    public void setPoint(Point newPoint);

}
