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
// File: SelectionResize.java
// Classes: SelectionResize
// Original Author: jrobbins@ics.uci.edu
// $Id: SelectionResize.java 1334 2011-05-23 10:30:22Z bobtarling $
package org.tigris.gef.base;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;

import org.graph.commons.logging.Log;
import org.graph.commons.logging.LogFactory;

import org.tigris.gef.presentation.Connector;
import org.tigris.gef.presentation.Fig;
import org.tigris.gef.presentation.FigNode;
import org.tigris.gef.presentation.Handle;

/**
 * A Selection class to represent selections on Figs that present resize
 * handles. The selected Fig can be moved or resized. Figrect, FigRRect,
 * FigCircle, and FigGroup are some of the Figs that normally use this
 * Selection. The selected Fig is told it's new bounding box, and some Figs
 * (like FigGroup or FigPoly) do calculations to scale themselves.
 */
public class SelectionResize extends Selection {

    private static final long serialVersionUID = 1996301098909656022L;

    private int cx;
    private int cy;
    private int cw;
    private int ch;

    private static Log log = LogFactory.getLog(SelectionResize.class);

    // //////////////////////////////////////////////////////////////
    // constructors
    /**
     * Construct a new SelectionResize for the given Fig
     */
    public SelectionResize(Fig f) {
        super(f);
    }

    /**
     * Return a handle ID for the handle under the mouse, or -1 if none.
     * Needs-More-Work: in the future, return a Handle instance or null.
     * <p>
     *
     * <pre>
     *   0-------1-------2
     *   |               |
     *   3               4
     *   |               |
     *   5-------6-------7
     * </pre>
     */
    public void hitHandle(Rectangle r, Handle h) {
        if (getContent().isResizable()) {

            updateHandleBox();
            Rectangle testRect = new Rectangle(0, 0, 0, 0);
            testRect.setBounds(cx - HAND_SIZE / 2, cy - HAND_SIZE / 2,
                    HAND_SIZE, ch + HAND_SIZE / 2);
            boolean leftEdge = r.intersects(testRect);
            testRect.setBounds(cx + cw - HAND_SIZE / 2, cy - HAND_SIZE / 2,
                    HAND_SIZE, ch + HAND_SIZE / 2);
            boolean rightEdge = r.intersects(testRect);
            testRect.setBounds(cx - HAND_SIZE / 2, cy - HAND_SIZE / 2, cw
                    + HAND_SIZE / 2, HAND_SIZE);
            boolean topEdge = r.intersects(testRect);
            testRect.setBounds(cx - HAND_SIZE / 2, cy + ch - HAND_SIZE / 2, cw
                    + HAND_SIZE / 2, HAND_SIZE);
            boolean bottomEdge = r.intersects(testRect);
            // needs-more-work: midpoints for side handles
            if (leftEdge && topEdge) {
                h.index = Handle.NORTHWEST;
                h.instructions = "Resize top left";
            } else if (rightEdge && topEdge) {
                h.index = Handle.NORTHEAST;
                h.instructions = "Resize top right";
            } else if (leftEdge && bottomEdge) {
                h.index = Handle.SOUTHWEST;
                h.instructions = "Resize bottom left";
            } else if (rightEdge && bottomEdge) {
                h.index = Handle.SOUTHEAST;
                h.instructions = "Resize bottom right";
            } // needs-more-work: side handles
            else {
                h.index = -1;
                h.instructions = "Move object(s)";
            }
        } else {
            h.index = -1;
            h.instructions = "Move object(s)";
        }

    }

    /**
     * Update the private variables cx etc. that represent the rectangle on
     * whose corners handles are to be drawn.
     */
    private void updateHandleBox() {
        final Rectangle cRect = getContent().getHandleBox();
        cx = cRect.x;
        cy = cRect.y;
        cw = cRect.width;
        ch = cRect.height;
    }

    /**
     * Paint the handles at the four corners and midway along each edge of the
     * bounding box.
     */
    public void paint(Graphics g) {
        final Fig fig = getContent();
        if (getContent().isResizable()) {

            updateHandleBox();
            g.setColor(Globals.getPrefs().handleColorFor(fig));
            g.fillRect(cx - HAND_SIZE / 2, cy - HAND_SIZE / 2, HAND_SIZE,
                    HAND_SIZE);
            g.fillRect(cx + cw - HAND_SIZE / 2, cy - HAND_SIZE / 2, HAND_SIZE,
                    HAND_SIZE);
            g.fillRect(cx - HAND_SIZE / 2, cy + ch - HAND_SIZE / 2, HAND_SIZE,
                    HAND_SIZE);
            g.fillRect(cx + cw - HAND_SIZE / 2, cy + ch - HAND_SIZE / 2,
                    HAND_SIZE, HAND_SIZE);
        } else {
            final int x = fig.getX();
            final int y = fig.getY();
            final int w = fig.getWidth();
            final int h = fig.getHeight();
            g.setColor(Globals.getPrefs().handleColorFor(fig));
            g.drawRect(x - BORDER_WIDTH, y - BORDER_WIDTH, w + BORDER_WIDTH * 2
                    - 1, h + BORDER_WIDTH * 2 - 1);
            g.drawRect(x - BORDER_WIDTH - 1, y - BORDER_WIDTH - 1, w
                    + BORDER_WIDTH * 2 + 2 - 1, h + BORDER_WIDTH * 2 + 2 - 1);
            g.fillRect(x - HAND_SIZE, y - HAND_SIZE, HAND_SIZE, HAND_SIZE);
            g.fillRect(x + w, y - HAND_SIZE, HAND_SIZE, HAND_SIZE);
            g.fillRect(x - HAND_SIZE, y + h, HAND_SIZE, HAND_SIZE);
            g.fillRect(x + w, y + h, HAND_SIZE, HAND_SIZE);
        }
    }

    /**
     * Change some attribute of the selected Fig when the user drags one of its
     * handles. Needs-More-Work: someday I might implement resizing that
     * maintains the aspect ratio.
     */
    public void dragHandle(int mX, int mY, int anX, int anY, Handle hand) {
        final Fig fig = getContent();
        if (!fig.isResizable()) {
            if (log.isDebugEnabled()) {
                log.debug("Handle " + hand
                        + " dragged but no action as fig is not resizable");
            }
            return;
        }

        updateHandleBox();

        final int x = cx;
        final int y = cy;
        final int w = cw;
        final int h = ch;
        int newX = x, newY = y, newWidth = w, newHeight = h;
        Dimension minSize = fig.getMinimumSize();
        int minWidth = minSize.width, minHeight = minSize.height;
        switch (hand.index) {
            case -1:
                fig.translate(anX - mX, anY - mY);
                return;
            case Handle.NORTHWEST:
                newWidth = x + w - mX;
                newWidth = (newWidth < minWidth) ? minWidth : newWidth;
                newHeight = y + h - mY;
                newHeight = (newHeight < minHeight) ? minHeight : newHeight;
                newX = x + w - newWidth;
                newY = y + h - newHeight;
                fig.setHandleBox(newX, newY, newWidth, newHeight);
                if ((newX + newWidth) != (x + w)) {
                    newX += (newX + newWidth) - (x + w);
                }
                if ((newY + newHeight) != (y + h)) {
                    newY += (newY + newHeight) - (y + h);
                }
                fig.setHandleBox(newX, newY, newWidth, newHeight);
                return;
            case Handle.NORTH:
                break;
            case Handle.NORTHEAST:
                newWidth = mX - x;
                newWidth = (newWidth < minWidth) ? minWidth : newWidth;
                newHeight = y + h - mY;
                newHeight = (newHeight < minHeight) ? minHeight : newHeight;
                newY = y + h - newHeight;
                fig.setHandleBox(newX, newY, newWidth, newHeight);
                if ((newY + newHeight) != (y + h)) {
                    newY += (newY + newHeight) - (y + h);
                }
                fig.setHandleBox(newX, newY, newWidth, newHeight);
                break;
            case Handle.WEST:
                break;
            case Handle.EAST:
                break;
            case Handle.SOUTHWEST:
                newWidth = x + w - mX;
                newWidth = (newWidth < minWidth) ? minWidth : newWidth;
                newHeight = mY - y;
                newHeight = (newHeight < minHeight) ? minHeight : newHeight;
                newX = x + w - newWidth;
                fig.setHandleBox(newX, newY, newWidth, newHeight);
                if ((newX + newWidth) != (x + w)) {
                    newX += (newX + newWidth) - (x + w);
                }
                fig.setHandleBox(newX, newY, newWidth, newHeight);
                break;
            case Handle.SOUTH:
                break;
            case Handle.SOUTHEAST:
                newWidth = mX - x;
                newWidth = (newWidth < minWidth) ? minWidth : newWidth;
                newHeight = mY - y;
                newHeight = (newHeight < minHeight) ? minHeight : newHeight;
                fig.setHandleBox(newX, newY, newWidth, newHeight);
                break;
            default:
                log.error("invalid handle number for resizing fig");
                return;
        }
        if (fig instanceof FigNode) {
            FigNode figNode = (FigNode) fig;
            for (Connector connector : figNode.getConnectors()) {

                Fig connectorFig = (Fig) connector;
                Point centre = new Point(
                        connectorFig.getX() + connectorFig.getHalfWidth(),
                        connectorFig.getY() + connectorFig.getHalfHeight());
                Point attachPoint = figNode.getClosestPoint(centre);
                connectorFig.setLocation(
                        attachPoint.x - connectorFig.getHalfWidth(),
                        attachPoint.y - connectorFig.getHalfHeight());
            }
        }
    }

} /* end class SelectionResize */
