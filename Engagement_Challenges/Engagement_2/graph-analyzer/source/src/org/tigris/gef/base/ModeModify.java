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
// File: ModeModify.java
// Classes: ModeModify
// Original Author: ics125 spring 1996
// $Id: ModeModify.java 1328 2011-05-21 14:52:00Z bobtarling $
package org.tigris.gef.base;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.tigris.gef.graph.GraphModel;
import org.tigris.gef.graph.MutableGraphSupport;
import org.tigris.gef.presentation.Fig;
import org.tigris.gef.presentation.FigEdge;
import org.tigris.gef.presentation.FigNode;
import org.tigris.gef.presentation.Handle;
import org.tigris.gef.undo.UndoManager;
import org.tigris.gef.util.Localizer;

/**
 * A Mode to process events from the Editor when the user is modifying a Fig.
 * Right now users can drag one or more Figs around the drawing area, or they
 * can move a handle on a single Fig.
 *
 * @see Fig
 * @see Selection
 */
public class ModeModify extends FigModifyingModeImpl {

    private static final long serialVersionUID = -914125238898272775L;

    /**
     * Minimum amount that the user must move the mouse to indicate that she
     * really wants to modify something.
     */
    private static final int MIN_DELTA = 4;
    private double degrees45 = Math.PI / 4;

    /**
     * drag in process
     */
    private boolean _dragInProcess = false;

    /**
     * The current position of the mouse during a drag operation.
     */
    private Point newMousePosition = new Point(0, 0);

    /**
     * The point at which the mouse started a drag operation.
     */
    private Point dragStartMousePosition = new Point(0, 0);

    /**
     * The location of the selection when the drag was started.
     */
    private Point dragStartSelectionPosition = null;

    /**
     * The index of the handle that the user is dragging
     */
    private Handle _curHandle = new Handle(-1);
    private Rectangle _highlightTrap = null;
    private int _deltaMouseX;
    private int _deltaMouseY;

    private GraphModel graphModel;

    // private ModifyCommand modifyCommand;
    //    
    /**
     * Construct a new ModeModify with the given parent, and set the Anchor
     * point to a default location (the _anchor's proper position will be
     * determined on mouse down).
     */
    public ModeModify(Editor par) {
        super(par);
    }

    // //////////////////////////////////////////////////////////////
    // user feedback
    /**
     * Reply a string of instructions that should be shown in the statusbar when
     * this mode starts.
     */
    public String instructions() {
        return Localizer.localize("GefBase", "ModeModifyInstructions");
    }

    // //////////////////////////////////////////////////////////////
    // event handlers
    /**
     * When the user drags the mouse two things can happen: (1) if the user is
     * dragging the body of one or more Figs then they are all moved around the
     * drawing area, or (2) if the user started dragging on a handle of one Fig
     * then the user can drag the handle around the drawing area and the Fig
     * reacts to that.
     */
    public void mouseDragged(MouseEvent mouseEvent) {
        if (mouseEvent.isConsumed()) {
            return;
        }

        mouseEvent.consume();
        Point p = mouseEvent.getPoint();
        getEditor().snap(p); // only allow movement on snap positions
        newMousePosition.x = p.x;
        newMousePosition.y = p.y;
        _deltaMouseX = p.x - dragStartMousePosition.x;
        _deltaMouseY = p.y - dragStartMousePosition.y;
        if (!_dragInProcess && Math.abs(_deltaMouseX) < MIN_DELTA
                && Math.abs(_deltaMouseY) < MIN_DELTA) {
            return;
        }

        if (!_dragInProcess) {
            _dragInProcess = true;
            UndoManager.getInstance().startChain();
            graphModel = editor.getGraphModel();
            if (graphModel instanceof MutableGraphSupport) {
                ((MutableGraphSupport) graphModel).fireGraphChanged();
            }
        }

        boolean restrict45 = mouseEvent.isControlDown();
        handleMouseDragged(restrict45);
    }

    /**
     * Check if a drag operation is in progress and if the key event changes the
     * restriction of horizontal/vertical movement. If so, update the
     * selection's position.
     *
     * @param keyEvent
     */
    private void updateMouseDrag(KeyEvent keyEvent) {
        if (_dragInProcess) {
            boolean restrict45 = keyEvent.isControlDown();
            handleMouseDragged(restrict45);
        }
    }

    public void keyPressed(KeyEvent keyEvent) {
        super.keyPressed(keyEvent);
        updateMouseDrag(keyEvent);
    }

    public void keyReleased(KeyEvent keyEvent) {
        super.keyReleased(keyEvent);
        updateMouseDrag(keyEvent);
    }

    /**
     * Like handleMouseDragged(MouseEvent) but takes only delta mouse position
     * as arguments. Is also called when control is pressed or released during
     * the drag.
     */
    private void handleMouseDragged(boolean restrict45) {
        int deltaMouseX = _deltaMouseX;
        int deltaMouseY = _deltaMouseY;
        if (restrict45 && deltaMouseY != 0) {
            double degrees = Math.atan2(deltaMouseY, deltaMouseX);
            degrees = degrees45 * Math.round(degrees / degrees45);
            double r = Math.sqrt(deltaMouseX * deltaMouseX + deltaMouseY
                    * deltaMouseY);
            deltaMouseX = (int) (r * Math.cos(degrees));
            deltaMouseY = (int) (r * Math.sin(degrees));
        }

        SelectionManager selectionManager = getEditor().getSelectionManager();
        if (selectionManager.getLocked()) {
            Globals.showStatus("Cannot Modify Locked Objects");
            return;
        }

        if (dragStartSelectionPosition == null) {
            selectionManager.startDrag();
        }

        Point selectionCurrentPosition = null;
        if (selectionManager.size() == 1
                && ((selectionManager.getFigs().get(0) instanceof FigEdge) || _curHandle.index > 0)) {
            selectionCurrentPosition = new Point(dragStartMousePosition);
        } else {
            selectionCurrentPosition = selectionManager.getDragLocation();
        }

        if (dragStartSelectionPosition == null) {
            dragStartSelectionPosition = selectionCurrentPosition;
        }

        Point selectionNewPosition = new Point(dragStartSelectionPosition);
        selectionNewPosition.translate(deltaMouseX, deltaMouseY);
        getEditor().snap(selectionNewPosition);
        selectionNewPosition.x = Math.max(0, selectionNewPosition.x);
        selectionNewPosition.y = Math.max(0, selectionNewPosition.y);

        int deltaSelectionX = selectionNewPosition.x
                - selectionCurrentPosition.x;
        int deltaSelectionY = selectionNewPosition.y
                - selectionCurrentPosition.y;
        if (deltaSelectionX != 0 || deltaSelectionY != 0) {
            if (_curHandle.index == -1) {
                setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                if (legal(deltaSelectionX, deltaSelectionY, selectionManager)) {
                    selectionManager.drag(deltaSelectionX, deltaSelectionY);
                }
            } else {
                if (_curHandle.index >= 0) {
                    setCursor(Cursor
                            .getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
                    selectionManager.dragHandle(newMousePosition.x,
                            newMousePosition.y, dragStartMousePosition.x,
                            dragStartMousePosition.y, _curHandle);
                    selectionManager.endTrans();
                }
            }
            // Note: if _curHandle.index == -2 then do nothing
        }
    }

    /**
     * When the user presses the mouse button on a Fig, this Mode starts
     * preparing for future drag events by finding if a handle was clicked on.
     * This event is passed from ModeSelect.
     */
    public void mousePressed(MouseEvent me) {
        if (me.isConsumed()) {
            return;
        }

        int x = me.getX();
        int y = me.getY();
        start();
        SelectionManager selectionManager = getEditor().getSelectionManager();
        if (selectionManager.size() == 0) {
            done();
        }

        if (selectionManager.getLocked()) {
            Globals.showStatus("Cannot Modify Locked Objects");
            me.consume();
            return;
        }

        dragStartMousePosition = me.getPoint();
        dragStartSelectionPosition = null;
        selectionManager.hitHandle(new Rectangle(x - 4, y - 4, 8, 8),
                _curHandle);
        Globals.showStatus(_curHandle.instructions);
        selectionManager.endTrans();

    }

    /**
     * On mouse up the modification interaction is done.
     */
    public void mouseReleased(MouseEvent me) {
        _dragInProcess = false;
        if (me.isConsumed()) {
            return;
        }

        done();
        me.consume();
        SelectionManager sm = editor.getSelectionManager();
        sm.stopDrag();
        List figs = sm.getFigs();
        int figCount = figs.size();
        for (int figIndex = 0; figIndex < figCount; ++figIndex) {
            Fig selectedFig = (Fig) figs.get(figIndex);
            if ((selectedFig instanceof FigNode)) {
                Rectangle bbox = selectedFig.getBounds();
                Layer lay = selectedFig.getLayer();
                List otherFigs = lay.getContents();
                Fig encloser = null;
                Iterator it = otherFigs.iterator();
                while (it.hasNext()) {
                    Fig otherFig = (Fig) it.next();
                    if (!(otherFig instanceof FigNode)) {
                        continue;
                    }

                    if (!(otherFig.getUseTrapRect())) {
                        continue;
                    }

                    // if (figs.contains(otherFig)) continue;
                    Rectangle trap = otherFig.getTrapRect();
                    if (trap == null) {
                        continue;
                    }

                    // now bbox is where the fig _will_ be
                    if ((trap.contains(bbox.x, bbox.y) && trap.contains(bbox.x
                            + bbox.width, bbox.y + bbox.height))) {
                        encloser = otherFig;
                    }
                }

                selectedFig.setEnclosingFig(encloser);

            } else if (selectedFig instanceof FigEdge) {
                ((FigEdge) selectedFig).computeRoute();
                selectedFig.endTrans();
            }

            selectedFig.endTrans();

            // if (modifyCommand != null) {
            // modifyCommand.execute();
            // }
        }
    }

    public void done() {
        super.done();
        SelectionManager sm = getEditor().getSelectionManager();
        sm.cleanUp();
        if (_highlightTrap != null) {
            editor.damaged(_highlightTrap);
            _highlightTrap = null;
        }
    }

    public void paint(Graphics g) {
        super.paint(g);
        if (_highlightTrap != null) {
            // Graphics g = (Graphics)graphicsContext;
            Color selectRectColor = Globals.getPrefs().getRubberbandColor();
            g.setColor(selectRectColor);
            g.drawRect(_highlightTrap.x - 1, _highlightTrap.y - 1,
                    _highlightTrap.width + 1, _highlightTrap.height + 1);
            g.drawRect(_highlightTrap.x - 2, _highlightTrap.y - 2,
                    _highlightTrap.width + 3, _highlightTrap.height + 3);
        }
    }

    private void damageHighlightTrap() {
        if (_highlightTrap == null) {
            return;
        }
        Rectangle r = new Rectangle(_highlightTrap);
        r.x -= 2;
        r.y -= 2;
        r.width += 4;
        r.height += 4;
        editor.damaged(r);
    }

    /**
     * Tests if the drag is legal regarding overlap with the bounds of any
     * enclosers.
     *
     * @param dx
     * @param dy
     * @param selectionManager
     * @return
     */
    private boolean legal(int dx, int dy, SelectionManager selectionManager) {
        damageHighlightTrap();

        _highlightTrap = null;
        List<FigNode> draggingFigNodes = getNodes(selectionManager.getDraggableFigs());
        int figCount = draggingFigNodes.size();
        Rectangle figBounds = new Rectangle();
        boolean draggedOntoCanvas = true;
        Fig encloser = null;
        Fig draggedFig = null;
        for (int figIndex = 0; figIndex < figCount; ++figIndex) {
            draggedFig = (Fig) draggingFigNodes.get(figIndex);

            draggedFig.getBounds(figBounds);
            figBounds.x += dx;
            figBounds.y += dy;

            Layer lay = draggedFig.getLayer();
            List<FigNode> figsInLayer = getNodes(lay.getContents());
            for (Fig otherFig : figsInLayer) {

                if (!draggedFig.getUseTrapRect() && !otherFig.getUseTrapRect()) {
                    // If neither the item being dragged or the item testing for
                    // overlap is an encloser then ignore and check next one
                    continue;
                }

                if (draggingFigNodes.contains(otherFig)) {
                    // If the item being tested for overlap is also being
                    // dragged then ignore as nothing has changed for this.
                    continue;
                }

                if (otherFig.getEnclosingFig() == draggedFig) {
                    // If the item being tested for overlap already encloses
                    // the dragged Fig then ignore.
                    // TODO: DO we really want to do this? What if we're bing
                    // dragged out of the encloser?
                    continue;
                }

                if (!otherFig.isVisible()) {
                    continue;
                }

                for (Rectangle trap : otherFig.getTrapRects(draggedFig)) {
                    if (trap == null) {
                        continue;
                    }

                    if (!trap.intersects(figBounds)) {
                        continue;
                    }

                    if ((trap.contains(figBounds.x, figBounds.y) && trap.contains(
                            figBounds.x + figBounds.width, figBounds.y
                            + figBounds.height))) {
                        draggedOntoCanvas = false;
                        encloser = otherFig;
                        continue;
                    }

                    if ((figBounds.contains(trap.x, trap.y) && figBounds.contains(
                            trap.x + trap.width, trap.y + trap.height))) {
                        continue;
                    }

                    _highlightTrap = trap;
                    damageHighlightTrap();
                    return false;
                }
            }

            if (draggedOntoCanvas) {
                // If it isn't dragged into any fig but into diagram canvas (null
                // encloser).
                if (!((MutableGraphSupport) graphModel).isEnclosable(
                        ((FigNode) draggedFig).getOwner(), null)) {
                    return false;
                }
            } else {
                // If it is dragged into any fig.
                if (!((MutableGraphSupport) graphModel).isEnclosable(
                        ((FigNode) draggedFig).getOwner(), ((FigNode) encloser)
                        .getOwner())) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Given a list of Figs returns only those that are FigNode instances
     *
     * @param figs
     * @return
     */
    private List<FigNode> getNodes(List<Fig> figs) {
        ArrayList<FigNode> results = new ArrayList<FigNode>(figs.size());
        for (Fig f : figs) {
            if (f instanceof FigNode) {
                results.add((FigNode) f);
            }
        }
        return results;
    }
}

// /**
// * This only exists to wrap the DragMemento.
// * The command is created when a drag start and executed when the drag ends.
// * @author Bob Tarling
// */
// class ModifyCommand implements Command {
//    
// private Map boundsByFigs = new HashMap();
// private int xOffset;
// private int yOffset;
//    
// ModifyCommand(List figs) {
// Iterator it = figs.iterator();
// while (it.hasNext()) {
// Fig f = (Fig)it.next();
// boundsByFigs.put(f, f.getBounds());
// }
// }
//
// public void execute() {
// ModifyMemento memento = new ModifyMemento();
// UndoManager.getInstance().startChain();
// UndoManager.getInstance().addMemento(memento);
// }
//
// private class ModifyMemento extends Memento {
//        
// ModifyMemento() {
// }
//        
// public void undo() {
// Iterator it = boundsByFigs.keySet().iterator();
// while (it.hasNext()) {
// Fig f = (Fig)it.next();
// f.damage();
// Rectangle rect = (Rectangle)boundsByFigs.get(f);
// f.setBounds(rect);
// f.calcBounds();
// f.damage();
// }
// }
// public void redo() {
// }
// public void dispose() {
// }
// }
// }
