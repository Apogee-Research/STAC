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
package org.tigris.gef.base;

import org.graph.commons.logging.Log;
import org.graph.commons.logging.LogFactory;
import org.tigris.gef.presentation.Fig;
import org.tigris.gef.ui.PopupGenerator;

import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.util.Vector;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;

/**
 * A permanent Mode to catch right-mouse-button events and show a popup menu.
 * Needs-more-work: this is not fully implemented yet. It should ask the Fig
 * under the mouse what menu it should offer.
 */
public class ModePopup extends FigModifyingModeImpl {

    private static final long serialVersionUID = 288785293995576958L;
    private static final Log LOG = LogFactory.getLog(ModePopup.class);

    // //////////////////////////////////////////////////////////////
    // constructor
    public ModePopup(Editor par) {
        super(par);
    }

    // //////////////////////////////////////////////////////////////
    // accessors
    /**
     * Always false because I never want to get out of popup mode.
     */
    public boolean canExit() {
        return false;
    }

    public String instructions() {
        return " ";
    }

    public boolean showPopup(MouseEvent me) {
        int x = me.getX();
        int y = me.getY();
        Fig underMouse = editor.hit(x, y);

        me = editor.retranslateMouseEvent(me);

        // if no Fig is under the mouse, show the editor's popup menu
        if (underMouse == null) {
            JPopupMenu editorPopup = editor.getPopupMenu();
            if (editorPopup != null) {
                // if the editor has a popup menu, show it
                editorPopup.show(me.getComponent(), me.getX(), me.getY());
                me.consume();
                return true;
            }
        }

        if (!(underMouse instanceof PopupGenerator)) {
            return false;
        }

        SelectionManager selectionManager = editor.getSelectionManager();
        if (!selectionManager.containsFig(underMouse)) {
            selectionManager.select(underMouse);
        } else {
            Vector selection = selectionManager.getFigs();
            Vector reassertSelection = new Vector(selection);
            selectionManager.select(reassertSelection);
        }

        Class commonClass = selectionManager.findCommonSuperClass();
        if (commonClass != null) {
            Object commonInstance = selectionManager
                    .findFirstSelectionOfType(commonClass);

            if (commonInstance instanceof PopupGenerator) {
                PopupGenerator popupGenerator = (PopupGenerator) commonInstance;
                List actions = popupGenerator.getPopUpActions(me);

                JPopupMenu popup = new JPopupMenu();

                int size = actions.size();
                for (int i = 0; i < size; ++i) {
                    Object a = actions.get(i);
                    if (a instanceof AbstractAction) {
                        popup.add((AbstractAction) a);
                    } else if (a instanceof JMenu) {
                        popup.add((JMenu) a);
                    } else if (a instanceof JMenuItem) {
                        popup.add((JMenuItem) a);
                    } else if (a instanceof JSeparator) {
                        popup.add((JSeparator) a);
                    }
                }

                popup.show(editor.getJComponent(), me.getX(), me.getY());
                me.consume();
                return true;
            }
        }
        return false;
    }

    /**
     * Determine if a popup menu should be displayed by this mouse key being
     * released
     */
    public void mouseReleased(MouseEvent me) {
        boolean popUpDisplayed = false;
        if (me.isPopupTrigger() || me.getModifiers() == InputEvent.BUTTON3_MASK) {
            popUpDisplayed = showPopup(me);
            if (LOG.isDebugEnabled()) {
                if (popUpDisplayed) {
                    LOG
                            .debug("MouseReleased detected as a popup trigger and popup displayed and event consumed");
                } else {
                    LOG
                            .debug("MouseReleased detected as a popup trigger but no popup to display");
                }
            }
            return;
        }
        LOG.debug("MouseReleased is not a popup trigger");
    }

    /**
     * Determine if a popup menu should be displayed by this mouse key being
     * pressed
     */
    public void mousePressed(MouseEvent me) {
        boolean popUpDisplayed = false;
        if (me.isPopupTrigger() || me.getModifiers() == InputEvent.BUTTON3_MASK) {
            popUpDisplayed = showPopup(me);
            if (LOG.isDebugEnabled()) {
                if (popUpDisplayed) {
                    LOG
                            .debug("MousePressed detected as a popup and popup displayed and event consumed");
                } else {
                    LOG
                            .debug("MousePressed detected as a popup but no popup to display");
                }
            }
            return;
        }
    }

    /**
     * Determine if a popup menu should be displayed by this mouse key being
     * clicked
     */
    public void mouseClicked(MouseEvent me) {
        boolean popUpDisplayed = false;
        if (me.isPopupTrigger() || me.getModifiers() == InputEvent.BUTTON3_MASK) {
            popUpDisplayed = showPopup(me);
            if (LOG.isDebugEnabled()) {
                if (popUpDisplayed) {
                    LOG
                            .debug("MouseClicked detected as a popup and popup displayed and event consumed");
                } else {
                    LOG
                            .debug("MouseClicked detected as a popup but no popup to display");
                }
            }
        }
    }
}
