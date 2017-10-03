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
// File: RemovePointAction.java
// Classes: RemovePointAction
// Original Author: andrea.nironi@gmail.com
package org.tigris.gef.base;

import java.awt.event.ActionEvent;

import javax.swing.Icon;

import org.tigris.gef.presentation.Fig;
import org.tigris.gef.presentation.FigPoly;
import org.tigris.gef.undo.UndoableAction;
import org.tigris.gef.util.Localizer;

/**
 * Action to remove the selected (last manipulated) point from a FigPoly.
 *
 * @see FigPoly
 */
public class RemovePointAction extends UndoableAction {

    private static final long serialVersionUID = 5176961969863495315L;

    private int selectedHandle = 0;

    /**
     * Creates a new RemovePointAction
     *
     * @param name The name of the action
     * @param selectedHandle The point to be removed
     */
    public RemovePointAction(String name, int selectedHandle) {
        this(name, selectedHandle, false);
    }

    /**
     * Creates a new RemovePointAction
     *
     * @param name The name of the action
     * @param icon The icon of the action
     * @param selectedHandle The point to be removed
     */
    public RemovePointAction(String name, Icon icon, int selectedHandle) {
        this(name, icon, selectedHandle, false);
    }

    /**
     * Creates a new RemovePointAction
     *
     * @param name The name of the action
     * @param selectedHandle The point to be removed
     * @param localize Whether to localize the name or not
     */
    public RemovePointAction(String name, int selectedHandle, boolean localize) {
        super(localize ? Localizer.localize("GefBase", name) : name);
        this.selectedHandle = selectedHandle;
    }

    /**
     * Creates a new RemovePointAction
     *
     * @param name The name of the action
     * @param icon The icon of the action
     * @param selectedHandle The point to be removed
     * @param localize Whether to localize the name or not
     */
    public RemovePointAction(String name, Icon icon, int selectedHandle,
            boolean localize) {
        super(localize ? Localizer.localize("GefBase", name) : name, icon);
        this.selectedHandle = selectedHandle;
    }

    /**
     * Ask the current editor's selected Fig to remove its point.
     */
    public void actionPerformed(ActionEvent e) {
        Fig f = null;
        Selection sel = null;
        Editor ce = Globals.curEditor();
        SelectionManager sm = ce.getSelectionManager();
        if (sm.getLocked()) {
            Globals.showStatus("Cannot Modify Locked Objects");
            return;
        }

        if (sm.selections().isEmpty()) {
            return;
        }
        sel = (Selection) sm.selections().firstElement();
        f = (Fig) sel.getContent();
        f.removePoint(selectedHandle);
        f.endTrans();
    }

}
