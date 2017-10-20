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
// File: SelectionMove.java
// Classes: SelectionMove
// Original Author: jrobbins@ics.uci.edu
// $Id: SelectionMove.java 1153 2008-11-30 16:14:45Z bobtarling $
package org.tigris.gef.base;

import java.awt.Graphics;
import java.awt.Rectangle;

import org.tigris.gef.presentation.Fig;
import org.tigris.gef.presentation.Handle;

/**
 * Selection object that allows the user to move the selected Fig, but not to
 * resize it.
 */
public class SelectionMove extends Selection {

    private static final long serialVersionUID = 2136083601083895759L;

    /**
     * Construct a new SelectionMove around the given DiagramElement
     */
    public SelectionMove(Fig f) {
        super(f);
    }

    /**
     * Paint the selection.
     */
    public void paint(Graphics g) {
        Fig fig = getContent();
        int x = fig.getX();
        int y = fig.getY();
        int w = fig.getWidth();
        int h = fig.getHeight();
        g.setColor(Globals.getPrefs().handleColorFor(fig));
        g.drawRect(x - BORDER_WIDTH, y - BORDER_WIDTH,
                w + BORDER_WIDTH * 2 - 1, h + BORDER_WIDTH * 2 - 1);
        g.drawRect(x - BORDER_WIDTH - 1, y - BORDER_WIDTH - 1, w + BORDER_WIDTH
                * 2 + 2 - 1, h + BORDER_WIDTH * 2 + 2 - 1);
        g.fillRect(x - HAND_SIZE, y - HAND_SIZE, HAND_SIZE, HAND_SIZE);
        g.fillRect(x + w, y - HAND_SIZE, HAND_SIZE, HAND_SIZE);
        g.fillRect(x - HAND_SIZE, y + h, HAND_SIZE, HAND_SIZE);
        g.fillRect(x + w, y + h, HAND_SIZE, HAND_SIZE);
    }

    /**
     * SelectionMove is used when there are no handles, so dragHandle does
     * nothing. Actually, hitHandle always returns -1 , so this method should
     * never even get called.
     */
    public void dragHandle(int mx, int my, int an_x, int an_y, Handle h) {
        /* do nothing */
    }

    /**
     * Return -1 as a special code to indicate that the user clicked in the body
     * of the Fig and wants to drag it around.
     */
    public void hitHandle(Rectangle r, Handle h) {
        h.index = -1;
        h.instructions = "Move Object(s)";
    }

    /**
     * The bounding box of the selection is the bbox of the contained Fig with
     * added space for the handles. For SelectionMove this is larger than normal
     * so that the edges of the selection box don't touch the edges of the
     * contents.
     */
    public Rectangle getBounds() {
        return new Rectangle(getContent().getX() - BORDER_WIDTH, getContent()
                .getY()
                - BORDER_WIDTH, getContent().getWidth() + BORDER_WIDTH * 2 + 2,
                getContent().getHeight() + BORDER_WIDTH * 2 + 2);
    }
} /* end class SelectionMove */
