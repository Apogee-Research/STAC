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
// File: CmdDelete.java
// Classes: CmdDelete
// $Id: CmdRemoveFromGraph.java 1153 2008-11-30 16:14:45Z bobtarling $
package org.tigris.gef.base;

import org.tigris.gef.undo.UndoManager;

/**
 * Cmd to delete Figs from view. This does not do anything to any underlying Net
 * or other model, it is strictly a manipulation of graphical objects. Normally
 * CmdDeleteFromModel is the command users will want to execute. This replaces
 * CmdDelete
 *
 * in 0.12.3 use RemoveFromGraphAction
 *
 * @see CmdDeleteFromModel
 * @see Editor
 * @see LayerDiagram
 */
public class CmdRemoveFromGraph extends Cmd {

    private static final long serialVersionUID = 8789982494893113775L;

    public CmdRemoveFromGraph() {
        super("RemoveFromGraph");
    }

    /**
     * Tell the selected Figs to remove themselves from the the diagram it is in
     * (and thus all editors).
     */
    public void doIt() {
        UndoManager.getInstance().startChain();
        Editor ce = Globals.curEditor();
        SelectionManager sm = ce.getSelectionManager();
        sm.removeFromGraph();
    }

    public void undoIt() {
    }
} /* end class CmdDelete */
