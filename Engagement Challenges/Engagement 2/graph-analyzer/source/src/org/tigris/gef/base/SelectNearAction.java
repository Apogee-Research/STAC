// Copyright (c) 1996-06 The Regents of the University of California. All
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
// File: SelectNearAction.java
// Classes: SelectNearAction
// Original Author: andrea.nironi@gmail.com
package org.tigris.gef.base;

import java.awt.event.ActionEvent;

import javax.swing.Icon;

import org.tigris.gef.undo.UndoableAction;
import org.tigris.gef.util.Localizer;

public class SelectNearAction extends UndoableAction {

    private static final long serialVersionUID = -7302592306721995290L;

    // //////////////////////////////////////////////////////////////
    // constants
    public static final int LEFT = 1;

    public static final int RIGHT = 2;

    public static final int UP = 3;

    public static final int DOWN = 4;

    // //////////////////////////////////////////////////////////////
    // instance variables
    private int direction;

    private int magnitude;

    /**
     * Creates a new SelectNearAction
     *
     * @param name The name of the action
     * @param dir The direction of the selection
     */
    public SelectNearAction(int dir) {
        this(dir, 1);
    }

    /**
     * Creates a new SelectNearAction
     *
     * @param name The name of the action
     * @param dir The direction of the selection
     */
    public SelectNearAction(int dir, int mag) {
        super("SelectNear" + wordFor(dir)); // needs-more-work: direction
        direction = dir;
        magnitude = mag;
    }

    private static String wordFor(int d) {
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
     * Creates a new SelectNearAction
     *
     * @param name The name of the action
     * @param dir The direction of the selection
     */
    public SelectNearAction(String name, int dir) {
        this(name, dir, 1, false);
    }

    /**
     * Creates a new SelectNearAction
     *
     * @param name The name of the action
     * @param dir The direction of the selection
     * @param mag The magnitude of the selection
     */
    public SelectNearAction(String name, int dir, int mag) {
        this(name, dir, mag, false);
    }

    /**
     * Creates a new SelectNearAction
     *
     * @param name The name of the action
     * @param icon The icon of the action
     * @param dir The direction of the selection
     */
    public SelectNearAction(String name, Icon icon, int dir) {
        this(name, icon, dir, 1, false);
    }

    /**
     * Creates a new SelectNearAction
     *
     * @param name The name of the action
     * @param icon The icon of the action
     * @param dir The direction of the selection
     * @param mag The magnitude of the selection
     */
    public SelectNearAction(String name, Icon icon, int dir, int mag) {
        this(name, icon, dir, mag, false);
    }

    /**
     * Creates a new SelectNearAction
     *
     * @param name The name of the action
     * @param dir The direction of the selection
     * @param localize Whether to localize the name or not
     */
    public SelectNearAction(String name, int dir, boolean localize) {
        this(name, dir, 1, false);
    }

    /**
     * Creates a new SelectNearAction
     *
     * @param name The name of the action
     * @param dir The direction of the selection
     * @param mag The magnitude of the selection
     * @param localize Whether to localize the name or not
     */
    public SelectNearAction(String name, int dir, int mag, boolean localize) {
        super(localize ? Localizer.localize("GefBase", name) : name);
        this.direction = dir;
        this.magnitude = mag;
    }

    /**
     * Creates a new SelectNearAction
     *
     * @param name The name of the action
     * @param icon The icon of the action
     * @param dir The direction of the selection
     * @param localize Whether to localize the name or not
     */
    public SelectNearAction(String name, Icon icon, int dir, boolean localize) {
        this(name, icon, dir, 1, false);
    }

    /**
     * Creates a new SelectNearAction
     *
     * @param name The name of the action
     * @param icon The icon of the action
     * @param dir The direction of the selection
     * @param mag The magnitude of the selection
     * @param localize Whether to localize the name or not
     */
    public SelectNearAction(String name, Icon icon, int dir, int mag,
            boolean localize) {
        super(localize ? Localizer.localize("GefBase", name) : name, icon);
        this.direction = dir;
        this.magnitude = mag;
    }

    // //////////////////////////////////////////////////////////////
    // Action API
    /**
     * Move the selected items a few pixels in the given direction. Note that
     * the sign convention is the opposite of ScrollAction.
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
        switch (direction) {
            case LEFT:
                dx = 0 - magnitude;
                break;
            case RIGHT:
                dx = magnitude;
                break;
            case UP:
                dy = 0 - magnitude;
                break;
            case DOWN:
                dy = magnitude;
                break;
        }
        // Should I move it so that it aligns with the next grid?
        sm.translate(dx, dy);
        sm.endTrans();
    }
}
