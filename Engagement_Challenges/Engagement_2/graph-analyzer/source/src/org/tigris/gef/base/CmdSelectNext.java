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
// File: CmdSelectNext.java
// Classes: CmdSelectNext
// Original Author: jrobbins@ics.uci.edu
// $Id: CmdSelectNext.java 1153 2008-11-30 16:14:45Z bobtarling $
package org.tigris.gef.base;

import org.tigris.gef.presentation.Fig;

import java.util.List;

/**
 * Cmd to select the next (or previous) Fig in the editor's current view. This
 * is very convienent for moving among lots of small Figs. It also provides a
 * simple example of an Cmd that is bound to a key.
 *
 * in 0.12.3 use SelectNextAction
 *
 * @see org.tigris.gef.graph.presentation.JGraph#initKeys()
 */
public class CmdSelectNext extends Cmd {

    private static final long serialVersionUID = -5038323715965138457L;

    public static final String DIR = "Direction";
    public static final String DIR_NEXT = "Next";
    public static final String DIR_PREV = "Previous";

    // //////////////////////////////////////////////////////////////
    // constructors
    public CmdSelectNext() {
        this(true);
    }

    public CmdSelectNext(boolean next) {
        this(next ? DIR_NEXT : DIR_PREV);
    }

    public CmdSelectNext(String d) {
        super("Select" + d);
        setArg(DIR, d);
    }

    // //////////////////////////////////////////////////////////////
    // Cmd API
    public void doIt() {
        Selection curSel;
        Fig newFig = null;
        int offset = 1;
        String dir = (String) getArg(DIR);
        if (DIR_PREV.equals(dir)) {
            offset = -1;
        }
        Editor ce = Globals.curEditor();
        SelectionManager sm = ce.getSelectionManager();
        List diagramContents = ce.getLayerManager().getContents();
        int diagramSize = diagramContents.size();
        int newIndex = diagramSize + 1;

        if (sm.size() == 0) {
            newIndex = 0;
        } else if (sm.size() == 1) {
            Fig curFig;
            curSel = (Selection) sm.selections().firstElement();
            curFig = curSel.getContent();
            int curIndex = diagramContents.indexOf(curFig);
            newIndex = (curIndex + offset + diagramSize) % diagramSize;
        }
        if (diagramSize > newIndex) {
            newFig = (Fig) diagramContents.get(newIndex);
        }
        if (newFig != null) {
            ce.getSelectionManager().select(newFig);
        }
    }

    public void undoIt() {
        org.graph.commons.logging.LogFactory.getLog(null).info("Undo does not make sense for CmdSelectNext");
    }
}
