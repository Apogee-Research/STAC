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
// File: CmdUngroup.java
// Classes: CmdUngroup
// Original Author: jrobbins@ics.uci.edu
// $Id: CmdUngroup.java 1153 2008-11-30 16:14:45Z bobtarling $
package org.tigris.gef.base;

import org.tigris.gef.presentation.Fig;
import org.tigris.gef.presentation.FigGroup;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.Vector;

/**
 * Cmd to ungroup a selected group object.
 *
 * in 0.12.3 use UngroupAction
 *
 * @see CmdGroup
 * @see FigGroup
 */
public class CmdUngroup extends Cmd {

    private static final long serialVersionUID = 5447608769108715379L;

    public CmdUngroup() {
        super("Ungroup");
    }

    public void doIt() {
        Vector ungroupedItems = new Vector();
        Editor currentEditor = Globals.curEditor();
        Vector selectedFigs = currentEditor.getSelectionManager().getFigs();
        Enumeration eachDE = selectedFigs.elements();
        while (eachDE.hasMoreElements()) {
            Object o = eachDE.nextElement();
            if (o instanceof FigGroup) {
                FigGroup fg = (FigGroup) o;
                Iterator it = fg.getFigs().iterator();
                while (it.hasNext()) {
                    Fig f = (Fig) it.next();
                    currentEditor.add(f);
                    ungroupedItems.addElement(f);
                }
                currentEditor.remove(fg);
            }
        } /* end while each selected object */

        currentEditor.getSelectionManager().deselectAll();
        currentEditor.getSelectionManager().select(ungroupedItems);
    }

    public void undoIt() {
        org.graph.commons.logging.LogFactory.getLog(null).info("not implemented yet");
    }
} /* end class CmdUngroup */
