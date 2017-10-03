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
// File: Handle.java
// Classes: Handle
// Original Author: jrobbins@ics.uci.edu
// $Id: Handle.java 1153 2008-11-30 16:14:45Z bobtarling $
package org.tigris.gef.presentation;

import java.awt.Cursor;

/**
 * This class stores the index of the handle that the user is dragging. I
 * originally used a simple int, but some dragHandle() methods need to change
 * the index because new handles can be added during a drag.
 *
 * @see FigPoly#moveVertex
 */
public class Handle {

    /**
     * The handle in the northwest corner of a FigNode
     */
    public static final int NORTHWEST = 0;
    /**
     * The handle in the north edge of a FigNode
     */
    public static final int NORTH = 1;
    /**
     * The handle in the northeast corner of a FigNode
     */
    public static final int NORTHEAST = 2;
    /**
     * The handle in the west edge of a FigNode
     */
    public static final int WEST = 3;
    /**
     * The handle in the east edge of a FigNode
     */
    public static final int EAST = 4;
    /**
     * The handle in the southwest corner of a FigNode
     */
    public static final int SOUTHWEST = 5;
    /**
     * The handle in the south edge of a FigNode
     */
    public static final int SOUTH = 6;
    /**
     * The handle in the southeast corner of a FigNode
     */
    public static final int SOUTHEAST = 7;

    // //////////////////////////////////////////////////////////////
    // instance variables
    /**
     * Index of the handle on some Fig that was clicked on.
     */
    public int index;

    /**
     * Instructions to be shown when the user's mouse is hovering over or is
     * dragging this handle
     */
    public String instructions = " ";

    /**
     * Mouse cursor Cursor while hovering or dragging
     */
    public Cursor cursor = null;

    // //////////////////////////////////////////////////////////////
    // constructors
    /**
     * Make a new Handle with the given handle index.
     */
    public Handle(int ind) {
        index = ind;
    }

} /* end class Handle */
