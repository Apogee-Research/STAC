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
// File: CutAction.java
// Classes: CutAction
// Original Author: andrea.nironi@gmail.com
package org.tigris.gef.base;

import java.awt.event.ActionEvent;
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.Icon;

import org.tigris.gef.presentation.Fig;
import org.tigris.gef.presentation.FigEdge;
import org.tigris.gef.undo.UndoableAction;
import org.tigris.gef.util.Localizer;

public class CutAction extends UndoableAction {

    private static final long serialVersionUID = -5008218014474881077L;

    /**
     * Creates a new CutAction
     *
     * @param name The name of the action
     */
    public CutAction(String name) {
        this(name, false);
    }

    /**
     * Creates a new CutAction
     *
     * @param name The name of the action
     * @param icon The icon of the action
     */
    public CutAction(String name, Icon icon) {
        this(name, icon, false);
    }

    /**
     * Creates a new CutAction
     *
     * @param name The name of the action
     * @param localize Whether to localize the name or not
     */
    public CutAction(String name, boolean localize) {
        super(localize ? Localizer.localize("GefBase", name) : name);
    }

    /**
     * Creates a new CutAction
     *
     * @param name The name of the action
     * @param icon The icon of the action
     * @param localize Whether to localize the name or not
     */
    public CutAction(String name, Icon icon, boolean localize) {
        super(localize ? Localizer.localize("GefBase", name) : name, icon);
    }

    public void actionPerformed(ActionEvent e) {
        super.actionPerformed(e);
        org.graph.commons.logging.LogFactory.getLog(null).info("[CmdCut] doIt");
        Editor ce = Globals.curEditor();
        SelectionManager selectionManager = ce.getSelectionManager();
        Vector copiedElements = selectionManager.selections();
        Vector figs = new Vector();
        Enumeration copies = copiedElements.elements();
        while (copies.hasMoreElements()) {
            Selection s = (Selection) copies.nextElement();
            Fig f = s.getContent();
            if (f instanceof FigEdge) {
                continue;
            }
            // needs-more-work: add support for cut-and-paste of edges
            f = (Fig) f.clone();
            figs.addElement(f);
        }
        Globals.clipBoard = figs;
        selectionManager.removeFromGraph();
    }
}
