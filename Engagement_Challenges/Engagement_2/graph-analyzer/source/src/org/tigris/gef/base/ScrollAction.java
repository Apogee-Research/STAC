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
// File: ScrollAction.java
// Classes: ScrollAction
// Original Author: andrea.nironi@gmail.com
package org.tigris.gef.base;

import java.awt.event.ActionEvent;

import javax.swing.Icon;

import org.tigris.gef.undo.UndoableAction;
import org.tigris.gef.util.Localizer;

/**
 * Action to scroll the view. Needs-More-Work: not implemented yet.
 *
 */
public class ScrollAction extends UndoableAction {

    private static final long serialVersionUID = -3240224240491643821L;

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

    // //////////////////////////////////////////////////////////////
    // constructor
    /**
     * Creates a new ScrollAction
     *
     * @param dir The direction of the scroll
     */
    public ScrollAction(int dir) {
        direction = dir;
        magnitude = 16; // Needs-More-Work: prefs
    }

    /**
     * Creates a new ScrollAction
     *
     * @param name The name of the action
     * @param dir The direction of the scroll
     */
    public ScrollAction(String name, int dir) {
        this(name, dir, false);
    }

    /**
     * Creates a new ScrollAction
     *
     * @param name The name of the action
     * @param icon The icon of the action
     * @param dir The direction of the scroll
     */
    public ScrollAction(String name, Icon icon, int dir) {
        this(name, icon, dir, false);
    }

    /**
     * Creates a new ScrollAction
     *
     * @param name The name of the action
     * @param dir The direction of the scroll
     * @param localize Whether to localize the name or not
     */
    public ScrollAction(String name, int dir, boolean localize) {
        super(localize ? Localizer.localize("GefBase", name) : name);
        direction = dir;
        magnitude = 16; // Needs-More-Work: prefs
    }

    /**
     * Creates a new ScrollAction
     *
     * @param name The name of the action
     * @param icon The icon of the action
     * @param dir The direction of the scroll
     * @param localize Whether to localize the name or not
     */
    public ScrollAction(String name, Icon icon, int dir, boolean localize) {
        super(localize ? Localizer.localize("GefBase", name) : name, icon);
        direction = dir;
        magnitude = 16; // Needs-More-Work: prefs
    }

    /**
     * Scroll the view of the current editor in the given direction.
     * Needs-More-Work: not implemented yet.
     */
    public void actionPerformed(ActionEvent e) {
        int deltaX = 0, deltaY = 0;
        // Needs-More-Work
        switch (direction) {
            case LEFT:
                deltaX = magnitude;
                break;
            case RIGHT:
                deltaX = 0 - magnitude;
                break;
            case UP:
                deltaY = magnitude;
                break;
            case DOWN:
                deltaY = 0 - magnitude;
                break;
        }
        // Needs-More-Work: now do something with deltas...
        org.graph.commons.logging.LogFactory.getLog(null).info("Scrolling by " + deltaX + ", " + deltaY);
    }
}
