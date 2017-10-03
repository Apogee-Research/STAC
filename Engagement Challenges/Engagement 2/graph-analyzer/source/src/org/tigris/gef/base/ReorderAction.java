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
// File: ReorderAction.java
// Classes: ReorderAction
// Original Author: andrea.nironi@gmail.com
package org.tigris.gef.base;

import java.awt.event.ActionEvent;

import javax.swing.Icon;

import org.tigris.gef.undo.UndoableAction;
import org.tigris.gef.util.Localizer;

/**
 * Cmd to change the back-to-front ordering of Fig's.
 *
 * @see LayerDiagram#reorder
 */
public class ReorderAction extends UndoableAction {

    private static final long serialVersionUID = -2190865994915716779L;

    // //////////////////////////////////////////////////////////////
    // constants
    public static final int SEND_TO_BACK = 1;
    public static final int BRING_TO_FRONT = 2;
    public static final int SEND_BACKWARD = 3;
    public static final int BRING_FORWARD = 4;

    public static ReorderAction SendToBack = new ReorderAction(SEND_TO_BACK);
    public static ReorderAction BringToFront = new ReorderAction(BRING_TO_FRONT);
    public static ReorderAction SendBackward = new ReorderAction(SEND_BACKWARD);
    public static ReorderAction BringForward = new ReorderAction(BRING_FORWARD);

    // //////////////////////////////////////////////////////////////
    // instance variables
    private int function;

    // //////////////////////////////////////////////////////////////
    // constructor
    /**
     * Construct a new ReorderAction with the given reordering constrant (see
     * above)
     *
     * @param function The function of the reorder hardcoded names are going to
     * be removed. Use ReorderAction(name, function) instead
     */
    public ReorderAction(int function) {
        this(wordFor(function), function, false);
    }

    /**
     * Construct a new ReorderAction with the given reordering constrant (see
     * above)
     *
     * @param name The name of the action
     * @param function The function of the reorder
     */
    public ReorderAction(String name, int function) {
        this(name, function, false);
    }

    /**
     * Construct a new ReorderAction with the given reordering constrant (see
     * above)
     *
     * @param name The name of the action
     * @param icon The icon of the action
     * @param function The function of the reorder
     */
    public ReorderAction(String name, Icon icon, int function) {
        this(name, icon, function, false);
    }

    /**
     * Construct a new ReorderAction with the given reordering constrant (see
     * above)
     *
     * @param name The name of the action
     * @param function The function of the reorder
     * @param localize Whether to localize the name or not
     */
    public ReorderAction(String name, int function, boolean localize) {
        super(localize ? Localizer.localize("GefBase", name) : name);
        this.function = function;
    }

    /**
     * Construct a new ReorderAction with the given reordering constrant (see
     * above)
     *
     * @param name The name of the action
     * @param icon The icon of the action
     * @param function The function of the reorder
     * @param localize Whether to localize the name or not
     */
    public ReorderAction(String name, Icon icon, int function, boolean localize) {
        super(localize ? Localizer.localize("GefBase", name) : name, icon);
        this.function = function;
    }

    /**
     * hardcoded names are going to be removed
     */
    protected static String wordFor(int f) {
        switch (f) {
            case SEND_BACKWARD:
                return "Backward";
            case SEND_TO_BACK:
                return "ToBack";
            case BRING_FORWARD:
                return "Forward";
            case BRING_TO_FRONT:
                return "ToFront";
        }
        return "";
    }

    // //////////////////////////////////////////////////////////////
    // Action API
    public void actionPerformed(ActionEvent e) {
        super.actionPerformed(e);
        Editor ce = Globals.curEditor();
        LayerManager lm = ce.getLayerManager();
        SelectionManager sm = ce.getSelectionManager();
        sm.reorder(function, lm.getActiveLayer());
        sm.endTrans();
        // ce.repairDamage();
    }
}
