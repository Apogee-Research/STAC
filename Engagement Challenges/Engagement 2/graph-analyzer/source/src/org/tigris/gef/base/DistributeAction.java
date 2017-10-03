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
// Original class name: CmdDistribute
// Original Author: jrobbins@ics.uci.edu
// $Id: DistributeAction.java 1268 2009-08-19 08:37:36Z mvw $
package org.tigris.gef.base;

import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import org.tigris.gef.presentation.Fig;
import org.tigris.gef.undo.UndoableAction;
import org.tigris.gef.util.Localizer;

/**
 * An Action to align 2 or more objects relative to each other.
 */
public class DistributeAction extends UndoableAction {

    private static final long serialVersionUID = 3630014084522093432L;
    /**
     * Constants specifying the type of distribution requested.
     */
    public static final int H_SPACING = 0;
    public static final int H_CENTERS = 1;
    public static final int H_PACK = 2;
    public static final int V_SPACING = 4;
    public static final int V_CENTERS = 5;
    public static final int V_PACK = 6;

    /**
     * Specification of the type of distribution requested
     */
    private int _request;
    private Rectangle _bbox = null;
    private List<Fig> figs;
    private Integer gap;

    /**
     * Construct a new DistributeAction.
     *
     * @param r The desired alignment direction, one of the constants listed
     * above.
     */
    public DistributeAction(int r) {
        super(Localizer.localize("GefBase", "Distribute" + wordFor(r)));
        _request = r;
    }

    public DistributeAction(int r, List<Fig> figs) {
        this(r);
        this.figs = figs;
    }

    private static String wordFor(int r) {
        switch (r) {
            case H_SPACING:
                return "HorizontalSpacing";
            case H_CENTERS:
                return "HorizontalCenters";
            case H_PACK:
                return "Leftward";
            case V_SPACING:
                return "VerticalSpacing";
            case V_CENTERS:
                return "VerticalCenters";
            case V_PACK:
                return "Upward";
        }
        return "";
    }

    public void setBoundingBox(Rectangle bbox) {
        _bbox = bbox;
    }

    public void setGap(Integer gap) {
        this.gap = gap;
    }

    public void actionPerformed(ActionEvent e) {

        super.actionPerformed(e);
        List<Fig> targets = new ArrayList<Fig>();

        Editor ce = Globals.curEditor();
        int packGap = 8;
        if (gap != null) {
            packGap = gap.intValue();
        }
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
        int leftMostCenter = 0, rightMostCenter = 0;
        int topMostCenter = 0, bottomMostCenter = 0;
        int size = targets.size();
        if (size == 0) {
            return;
        }

        // find the bbox of all selected objects
        Fig f = targets.get(0);
        if (_bbox == null) {
            _bbox = f.getBounds();
            leftMostCenter = _bbox.x + _bbox.width / 2;
            rightMostCenter = _bbox.x + _bbox.width / 2;
            topMostCenter = _bbox.y + _bbox.height / 2;
            bottomMostCenter = _bbox.y + _bbox.height / 2;
            for (int i = 1; i < size; i++) {
                f = targets.get(i);
                Rectangle r = f.getBounds();
                _bbox.add(r);
                leftMostCenter = Math.min(leftMostCenter, r.x + r.width / 2);
                rightMostCenter = Math.max(rightMostCenter, r.x + r.width / 2);
                topMostCenter = Math.min(topMostCenter, r.y + r.height / 2);
                bottomMostCenter = Math.max(bottomMostCenter, r.y + r.height
                        / 2);
            }
        }

        // find the sum of the widths and heights of all selected objects
        int totalWidth = 0, totalHeight = 0;
        for (int i = 0; i < size; i++) {
            f = targets.get(i);
            totalWidth += f.getWidth();
            totalHeight += f.getHeight();
        }

        float gap = 0, oncenter = 0;
        float xNext = 0, yNext = 0;

        switch (_request) {
            case H_SPACING:
                xNext = _bbox.x;
                gap = (_bbox.width - totalWidth) / Math.max(size - 1, 1);
                break;
            case H_CENTERS:
                xNext = leftMostCenter;
                oncenter = (rightMostCenter - leftMostCenter)
                        / Math.max(size - 1, 1);
                break;
            case H_PACK:
                xNext = _bbox.x;
                gap = packGap;
                break;
            case V_SPACING:
                yNext = _bbox.y;
                gap = (_bbox.height - totalHeight) / Math.max(size - 1, 1);
                break;
            case V_CENTERS:
                yNext = topMostCenter;
                oncenter = (bottomMostCenter - topMostCenter)
                        / Math.max(size - 1, 1);
                break;
            case V_PACK:
                yNext = _bbox.y;
                gap = packGap;
                break;
        }

        // sort top-to-bottom or left-to-right, this maintains visual order
        // when we set the coordinates
        for (int i = 0; i < size; i++) {
            for (int j = i + 1; j < size; j++) {
                Fig fi = targets.get(i);
                Fig fj = targets.get(j);
                if (_request == H_SPACING || _request == H_CENTERS
                        || _request == H_PACK) {
                    if (fi.getX() > fj.getX()) {
                        swap(targets, i, j);
                    }
                } else if (fi.getY() > fj.getY()) {
                    swap(targets, i, j);
                }
            }
        }

        for (int i = 0; i < size; i++) {
            f = targets.get(i);
            switch (_request) {
                case H_SPACING:
                case H_PACK:
                    f.setLocation((int) xNext, f.getY());
                    xNext += f.getWidth() + gap;
                    break;
                case H_CENTERS:
                    f.setLocation((int) xNext - f.getWidth() / 2, f.getY());
                    xNext += oncenter;
                    break;
                case V_SPACING:
                case V_PACK:
                    f.setLocation(f.getX(), (int) yNext);
                    yNext += f.getHeight() + gap;
                    break;
                case V_CENTERS:
                    f.setLocation(f.getX(), (int) yNext - f.getHeight() / 2);
                    yNext += oncenter;
                    break;
            }
            f.endTrans();
        }
    }

    public void undoIt() {
    }

    protected void swap(List<Fig> v, int i, int j) {
        Fig temp = v.get(i);
        v.add(i, v.get(j));
        v.add(j, temp);
    }

    /**
     * @return the resulting bounding box after were done
     */
    public Rectangle getBoundingBox() {
        return _bbox;
    }
} /* end class CmdDistribute */
