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
// File: CmdCut.java
// Classes: CmdCut
// Original Author: Thorsten.Sturm@gentleware.de
// $Id: CmdCut.java 1171 2008-12-03 09:57:11Z bobtarling $
package org.tigris.gef.base;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.tigris.gef.presentation.*;

/**
 * in 0.12.3 us CutAction
 */
public class CmdCut extends Cmd {

    private static final long serialVersionUID = -5296601012186667929L;

    public CmdCut() {
        super("Cut");
    }

    public void doIt() {
        org.graph.commons.logging.LogFactory.getLog(null).info("[CmdCut] doIt");
        Editor ce = Globals.curEditor();
        SelectionManager selectionManager = ce.getSelectionManager();
        List<Selection> copiedElements = selectionManager.getSelections();
        List<Fig> figs = new ArrayList<Fig>();
        Iterator<Selection> copies = copiedElements.iterator();
        while (copies.hasNext()) {
            Selection s = copies.next();
            Fig f = s.getContent();
            if (f instanceof FigEdge) {
                continue;
            }
            // needs-more-work: add support for cut-and-paste of edges
            f = (Fig) f.clone();
            figs.add(f);
        }
        Globals.clipBoard = figs;
        selectionManager.removeFromGraph();
    }

    public void undoIt() {
        org.graph.commons.logging.LogFactory.getLog(null).info("Undo does not make sense for CmdCut");
    }

} /* end class CmdCut */
