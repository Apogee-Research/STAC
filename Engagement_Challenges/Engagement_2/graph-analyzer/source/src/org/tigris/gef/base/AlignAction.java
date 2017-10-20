// Copyright (c) 1996-2009 The Regents of the University of California. All
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
// Original class name: CmdAlign
// Original Author: ics125 spring 1996
// $Id: AlignAction.java 1267 2009-08-19 08:26:38Z mvw $
package org.tigris.gef.base;

import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.tigris.gef.presentation.Fig;
import org.tigris.gef.undo.UndoableAction;
import org.tigris.gef.util.Localizer;

/**
 * An Action to align 2 or more objects relative to each other.
 */
public class AlignAction extends UndoableAction {

    private static final long serialVersionUID = 4982051206522858526L;

    /* Constants specifying the type of alignment requested. */
    public static final int ALIGN_TOPS = 0;
    public static final int ALIGN_BOTTOMS = 1;
    public static final int ALIGN_LEFTS = 2;
    public static final int ALIGN_RIGHTS = 3;

    public static final int ALIGN_CENTERS = 4;
    public static final int ALIGN_H_CENTERS = 5;
    public static final int ALIGN_V_CENTERS = 6;

    public static final int ALIGN_TO_GRID = 7;

    private List<Fig> figs;

    /**
     * Specification of the type of alignment requested
     */
    private int direction;

    private Map<Fig, Rectangle> boundsByFig;

    /**
     * Construct a new CmdAlign.
     *
     * @param dir The desired alignment direction, one of the constants listed
     * above.
     */
    public AlignAction(int dir) {
        super(Localizer.localize("GefBase", "Align" + wordFor(dir))); // needs-more-work:
        // direction
        direction = dir;
    }

    public AlignAction(int dir, List<Fig> figs) {
        super(Localizer.localize("GefBase", "Align" + wordFor(dir))); // needs-more-work:
        // direction
        direction = dir;
        this.figs = figs;
    }

    private static String wordFor(int d) {
        switch (d) {
            case ALIGN_TOPS:
                return "Tops";
            case ALIGN_BOTTOMS:
                return "Bottoms";
            case ALIGN_LEFTS:
                return "Lefts";
            case ALIGN_RIGHTS:
                return "Rights";

            case ALIGN_CENTERS:
                return "Centers";
            case ALIGN_H_CENTERS:
                return "HorizontalCenters";
            case ALIGN_V_CENTERS:
                return "VerticalCenters";

            case ALIGN_TO_GRID:
                return "ToGrid";
        }
        return "";
    }

    public void actionPerformed(ActionEvent e) {

        super.actionPerformed(e);
        List<Fig> targets = new ArrayList<Fig>();

        Editor ce = Globals.curEditor();
        if (figs == null) {
            SelectionManager sm = ce.getSelectionManager();
            if (sm.getLocked()) {
                Globals.showStatus("Cannot Modify Locked Objects");
                return;
            }
            targets.addAll(sm.getSelectedFigs());
        } else {
            targets.addAll(figs);
        }
        int size = targets.size();
        if (size == 0) {
            return;
        }
        Rectangle bbox = targets.get(0).getBounds();
        for (int i = 1; i < size; i++) {
            bbox.add(targets.get(i).getBounds());
        }

        boundsByFig = new HashMap<Fig, Rectangle>(size);
        for (int i = 0; i < size; i++) {
            Fig f = targets.get(i);
            boundsByFig.put(f, f.getBounds());
            f.align(bbox, direction, ce);
            f.endTrans();
        }
    }
} /* end class AlignAction */
