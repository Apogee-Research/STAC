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
// File: SelectionReshape.java
// Classes: SelectionReshape
// Original Author: jrobbins@ics.uci.edu
// $Id: SelectionReshape.java 1153 2008-11-30 16:14:45Z bobtarling $
package org.tigris.gef.base;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import org.tigris.gef.presentation.Fig;
import org.tigris.gef.presentation.FigEdge;
import org.tigris.gef.presentation.FigEdgePoly;
import org.tigris.gef.presentation.FigPoly;
import org.tigris.gef.presentation.Handle;
import org.tigris.gef.undo.Memento;
import org.tigris.gef.undo.UndoManager;

/**
 * A Selection that allows the user to reshape the selected Fig. This is used
 * with FigPoly, FigLine, and FigInk. One handle is drawn over each point on the
 * Fig.
 *
 * @see FigLine
 * @see FigPoly
 * @see FigInk
 */
public class SelectionReshape extends Selection implements KeyListener {

    private static final long serialVersionUID = 2204649413528863935L;

    private int selectedHandle = -1;

    /**
     * Construct a new SelectionReshape for the given Fig
     */
    public SelectionReshape(Fig f) {
        super(f);
    }

    /**
     * Return a handle ID for the handle under the mouse, or -1 if none.
     */
    public void hitHandle(Rectangle r, Handle h) {
        Fig fig = getContent();
        int npoints = fig.getNumPoints();
        int[] xs = fig.getXs();
        int[] ys = fig.getYs();
        for (int i = 0; i < npoints; ++i) {
            if (r.contains(xs[i], ys[i])) {
                selectedHandle = i;
                h.index = i;
                h.instructions = "Move point";
                return;
            }
        }
        if (fig instanceof FigEdgePoly) {
            for (int i = 0; i < npoints - 1; ++i) {
                if (Geometry.intersects(r, xs[i], ys[i], xs[i + 1], ys[i + 1])) {
                    h.index = fig.getNumPoints();
                    h.instructions = "Add a point";
                    return;
                }
            }
        }
        selectedHandle = -1;
        h.index = -1;
        h.instructions = "Move object(s)";
    }

    /**
     * Paint the handles at the four corners and midway along each edge of the
     * bounding box.
     */
    public void paint(Graphics g) {
        Fig fig = getContent();
        int npoints = fig.getNumPoints();
        int[] xs = fig.getXs();
        int[] ys = fig.getYs();
        g.setColor(Globals.getPrefs().handleColorFor(fig));
        for (int i = 0; i < npoints; ++i) {
            g.fillRect(xs[i] - HAND_SIZE / 2, ys[i] - HAND_SIZE / 2, HAND_SIZE,
                    HAND_SIZE);
        }
        if (selectedHandle != -1) {
            g.drawRect(xs[selectedHandle] - HAND_SIZE / 2 - 2,
                    ys[selectedHandle] - HAND_SIZE / 2 - 2, HAND_SIZE + 3,
                    HAND_SIZE + 3);
        }
    }

    /**
     * Change some attribute of the selected Fig when the user drags one of its
     * handles.
     */
    public void dragHandle(int mX, int mY, int anX, int anY, Handle h) {
        final Fig selectedFig = getContent();

        // check assertions
        if (selectedFig instanceof FigEdgePoly) {
            final FigEdgePoly figEdgePoly = ((FigEdgePoly) selectedFig);

            class FigEdgeReshapeMemento extends Memento {

                Polygon oldPolygon;

                FigEdgeReshapeMemento(final Polygon poly) {
                    oldPolygon = new Polygon(poly.xpoints, poly.ypoints,
                            poly.npoints);
                }

                public void undo() {
                    UndoManager.getInstance().addMementoLock(this);
                    Polygon poly = figEdgePoly.getPolygon();
                    Polygon curPoly = new Polygon(poly.xpoints, poly.ypoints,
                            poly.npoints);
                    figEdgePoly.setPolygon(oldPolygon);
                    oldPolygon = curPoly;
                    figEdgePoly.damage();
                    UndoManager.getInstance().removeMementoLock(this);
                }

                public void redo() {
                    undo();
                }

                public String toString() {
                    return (isStartChain() ? "*" : " ") + "ReshapeMemento "
                            + oldPolygon;
                }
            }

            if (UndoManager.getInstance().isGenerateMementos()) {
                Memento memento = new FigEdgeReshapeMemento(figEdgePoly
                        .getPolygon());
                UndoManager.getInstance().startChain();
                UndoManager.getInstance().addMemento(memento);
            }

            int npoints = selectedFig.getNumPoints();
            int[] xs = selectedFig.getXs();
            int[] ys = selectedFig.getYs();
            Rectangle r = new Rectangle(anX - 4, anY - 4, 8, 8);
            if (h.index == figEdgePoly.getNumPoints()) {
                // If we're dragging the perimeter of a FigEdgePoly then
                // create a handle at that point.
                for (int i = 0; i < npoints - 1; ++i) {
                    if (Geometry.intersects(r, xs[i], ys[i], xs[i + 1],
                            ys[i + 1])) {
                        figEdgePoly.insertPoint(i, r.x, r.y);
                        h.index = i + 1;
                        break;
                    }
                }
            }
            // We should now be guaranteed to have a handle
            if (h.index < 0 || h.index >= figEdgePoly.getNumPoints()) {
                org.graph.commons.logging.LogFactory.getLog(null).info("mistake " + h.index);
            }
            // If we're dragging the first or last end then maybe we need
            // to do something special in some sub class.
            if ((h.index == 0) || (h.index == figEdgePoly.getNumPoints() - 1)) {
                updateEdgeEnds(figEdgePoly, h, mX, mY);
            } // end if
        }

        if (selectedFig instanceof FigPoly) {
            FigPoly poly = (FigPoly) selectedFig;
            if (h.index == 0 || h.index == (poly.getNumPoints() - 1)) {
                Point moveTo = new Point(mX, mY);
                poly.setEndPoints(moveTo, moveTo);
            } else {
                poly.moveVertex(h, mX, mY, false);
            }
        } else {
            selectedFig.setPoint(h, mX, mY);
        }
    }

    public void updateEdgeEnds(FigEdge poly, Handle handle, int x, int y) {
    }

    // //////////////////////////////////////////////////////////////
    // event handlers
    public void keyPressed(KeyEvent ke) {
        if (ke.isConsumed()) {
            return;
        }
        if (getContent() instanceof KeyListener) {
            ((KeyListener) getContent()).keyPressed(ke);
        }
    }

    public void keyReleased(KeyEvent ke) {
        if (ke.isConsumed()) {
            return;
        }
        if (getContent() instanceof KeyListener) {
            ((KeyListener) getContent()).keyReleased(ke);
        }
    }
} /* end class SelectionReshape */
