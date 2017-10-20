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
// File: CmdNudge.java
// Classes: CmdNudge
// Original Author: jrobbins@ics.uci.edu
// $Id: NudgeAction.java 1153 2008-11-30 16:14:45Z bobtarling $
package org.tigris.gef.base;

import java.awt.event.ActionEvent;

import org.tigris.gef.graph.MutableGraphSupport;
import org.tigris.gef.undo.UndoableAction;
import org.tigris.gef.util.Localizer;

/**
 * Cmd to Nudge Figs by a small distance. This is useful when you want to get
 * diagrams to look just right and you are not to steady with the mouse. Also
 * allows user to keep hands on keyboard.
 *
 * @see org.tigris.gef.presentation.Fig
 */
public class NudgeAction extends UndoableAction {

    private static final long serialVersionUID = 2121611741541853360L;

    public static final int LEFT = 1;
    public static final int RIGHT = 2;
    public static final int UP = 3;
    public static final int DOWN = 4;

    private int _direction;
    private int _magnitude;

    public NudgeAction(int dir) {
        this(dir, 1);
    }

    public NudgeAction(int dir, int mag) {
        super(Localizer.localize("GefBase", "Nudge" + wordFor(dir))); // needs-more-work:
        // direction
        _direction = dir;
        _magnitude = mag;
    }

    protected static String wordFor(int d) {
        switch (d) {
            case LEFT:
                return "Left";
            case RIGHT:
                return "Right";
            case UP:
                return "Up";
            case DOWN:
                return "Down";
        }
        return "";
    }

    /**
     * Move the selected items a few pixels in the given direction. Note that
     * the sign convention is the opposite of CmdScroll.
     */
    public void actionPerformed(ActionEvent e) {

        super.actionPerformed(e);

        Editor ce = Globals.curEditor();
        SelectionManager sm = ce.getSelectionManager();
        if (sm.getLocked()) {
            Globals.showStatus("Cannot Modify Locked Objects");
            return;
        }

        int dx = 0, dy = 0;
        switch (_direction) {
            case LEFT:
                dx = 0 - _magnitude;
                break;
            case RIGHT:
                dx = _magnitude;
                break;
            case UP:
                dy = 0 - _magnitude;
                break;
            case DOWN:
                dy = _magnitude;
                break;
        }
        // Should I move it so that it aligns with the next grid?
        sm.translate(dx, dy);
        MutableGraphSupport.enableSaveAction();
        sm.endTrans();
    }

    public void undoIt() {
        org.graph.commons.logging.LogFactory.getLog(null).info("Cannot undo CmdNudge, yet.");
    }
} /* end class CmdNudge */
