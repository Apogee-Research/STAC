// Copyright (c) 1996-2009 The Regents of the University of California. All
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
// File: ModeCreateFigPoly.java
// Classes: ModeCreateFigPoly
// Original Author: jrobbins@ics.uci.edu
// $Id: ModeCreateFigPoly.java 1259 2009-08-18 06:53:37Z mvw $
package org.tigris.gef.base;

import java.awt.Point;
import java.awt.event.MouseEvent;

import org.tigris.gef.presentation.Fig;
import org.tigris.gef.presentation.FigPoly;
import org.tigris.gef.presentation.Handle;
import org.tigris.gef.util.Localizer;

/**
 * A Mode to interprete user input while creating a FigPoly. All of the actual
 * event handling is inherited from ModeCreate. This class just implements the
 * differences needed to make it specific to polygons.
 */
public class ModeCreateFigPoly extends ModeCreate {

    private static final long serialVersionUID = 2839607058696197299L;

    /**
     * The number of points added so far.
     */
    protected int _npoints = 0;
    protected int _lastX, _lastY, _startX, _startY;
    protected Handle _handle = new Handle(-1);

    // //////////////////////////////////////////////////////////////
    // Mode API
    public String instructions() {
        return Localizer.localize("GefBase", "ModeCreateFigPolyInstructions");
    }

    // //////////////////////////////////////////////////////////////
    // ModeCreate API
    /**
     * Create a new FigRect instance based on the given mouse down event and the
     * state of the parent Editor.
     */
    public Fig createNewItem(MouseEvent me, int snapX, int snapY) {
        FigPoly p = new FigPoly(snapX, snapY);
        p.addPoint(snapX, snapY); // add the first point twice
        _startX = _lastX = snapX;
        _startY = _lastY = snapY;
        _npoints = 2;
        return p;
    }

    // //////////////////////////////////////////////////////////////
    // Event handlers
    public void mousePressed(MouseEvent me) {
        if (me.isConsumed()) {
            return;
        }
        int x = me.getX();
        int y = me.getY();
        if (_npoints == 0) {
            super.mousePressed(me);
        }
        if (!nearLast(x, y)) {
            editor.damageAll();
            Point snapPt = new Point(x, y);
            editor.snap(snapPt);
            ((FigPoly) _newItem).addPoint(snapPt.x, snapPt.y);
            _npoints++;
            editor.damageAll();
        }
        me.consume();
    }

    public void mouseReleased(MouseEvent me) {
        if (me.isConsumed()) {
            return;
        }
        int x = me.getX(), y = me.getY();
        if (_npoints > 2 && nearLast(x, y)) {
            FigPoly p = (FigPoly) _newItem;
            editor.damageAll();
            _handle.index = p.getNumPoints() - 1;
            p.moveVertex(_handle, _startX, _startY, true);
            _npoints = 0;
            editor.damageAll();
            editor.add(p);
            editor.getSelectionManager().select(p);
            _newItem = null;
            done();
            me.consume();
            return;
        }
        _lastX = x;
        _lastY = y;
        me.consume();
    }

    public void mouseMoved(MouseEvent me) {
        mouseDragged(me);
    }

    public void mouseDragged(MouseEvent me) {
        if (me.isConsumed()) {
            return;
        }
        int x = me.getX(), y = me.getY();
        if (_npoints == 0) {
            me.consume();
            return;
        }
        FigPoly p = (FigPoly) _newItem;
        editor.damageAll(); // startTrans?
        Point snapPt = new Point(x, y);
        editor.snap(snapPt);
        _handle.index = p.getNumPoints() - 1;
        p.moveVertex(_handle, snapPt.x, snapPt.y, true);
        editor.damageAll(); // endTrans?
        me.consume();
    }

    /**
     * Internal function to see if the user clicked twice on the same spot.
     */
    protected boolean nearLast(int x, int y) {
        return x > _lastX - Editor.GRIP_SIZE && x < _lastX + Editor.GRIP_SIZE
                && y > _lastY - Editor.GRIP_SIZE
                && y < _lastY + Editor.GRIP_SIZE;
    }
} /* end class ModeCreateFigPoly */
