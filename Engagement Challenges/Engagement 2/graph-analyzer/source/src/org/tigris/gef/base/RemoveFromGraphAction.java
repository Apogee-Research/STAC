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
// File: RemoveFromGraphAction.java
// Classes: RemoveFromGraphAction
// Original Author: andrea.nironi@gmail.com
package org.tigris.gef.base;

import java.awt.event.ActionEvent;

import javax.swing.Icon;

import org.tigris.gef.undo.UndoableAction;
import org.tigris.gef.util.Localizer;

/**
 * Action to delete Figs from view. This does not do anything to any underlying
 * Net or other model, it is strictly a manipulation of graphical objects.
 * Normally DeleteFromModelAction is the command users will want to execute.
 *
 * @see DeleteFromModelAction
 * @see Editor
 * @see LayerDiagram
 */
public class RemoveFromGraphAction extends UndoableAction {

    private static final long serialVersionUID = -4537382217248274321L;

    /**
     * Creates a new RemoveFromGraphAction
     *
     * @param name The name of the action
     */
    public RemoveFromGraphAction(String name) {
        this(name, false);
    }

    /**
     * Creates a new RemoveFromGraphAction
     *
     * @param name The name of the action
     * @param icon The icon of the action
     */
    public RemoveFromGraphAction(String name, Icon icon) {
        this(name, icon, false);
    }

    /**
     * Creates a new RemoveFromGraphAction
     *
     * @param name The name of the action
     * @param localize Whether to localize the name or not
     */
    public RemoveFromGraphAction(String name, boolean localize) {
        super(localize ? Localizer.localize("GefBase", name) : name);
    }

    /**
     * Creates a new RemoveFromGraphAction
     *
     * @param name The name of the action
     * @param icon The icon of the action
     * @param localize Whether to localize the name or not
     */
    public RemoveFromGraphAction(String name, Icon icon, boolean localize) {
        super(localize ? Localizer.localize("GefBase", name) : name, icon);
    }

    /**
     * Tell the selected Figs to remove themselves from the the diagram it is in
     * (and thus all editors).
     */
    public void actionPerformed(ActionEvent e) {
        super.actionPerformed(e);
        Editor ce = Globals.curEditor();
        SelectionManager sm = ce.getSelectionManager();
        sm.removeFromGraph();
    }

}
