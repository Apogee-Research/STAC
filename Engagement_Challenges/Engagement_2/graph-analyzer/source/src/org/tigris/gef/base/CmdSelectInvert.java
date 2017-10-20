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
// File: CmdSelectInvert.java
// Classes: CmdSelectInvert
// Original Author: jrobbins@ics.uci.edu
// $Id: CmdSelectInvert.java 1153 2008-11-30 16:14:45Z bobtarling $
package org.tigris.gef.base;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Cmd to select all the Figs in the editor's current view that were not
 * previously selected.
 *
 * in 0.12.3 use SelectInvertAction
 */
public class CmdSelectInvert extends Cmd {

    private static final long serialVersionUID = -7470920956846786762L;

    public CmdSelectInvert() {
        super("InvertSelection");
    }

    public void doIt() {
        Editor ce = Globals.curEditor();
        List selected = ce.getSelectionManager().getFigs();
        List diagramContents = ce.getLayerManager().getContents();
        List inverse = new ArrayList(diagramContents.size());

        Iterator it = diagramContents.iterator();
        while (it.hasNext()) {
            Object dc = it.next();
            if (!selected.contains(dc)) {
                inverse.add(dc);
            }
        }
        ce.getSelectionManager().select(inverse);
    }

    public void undoIt() {
        org.graph.commons.logging.LogFactory.getLog(null).info("Undo does not make sense for CmdSelectInvert");
    }
}
