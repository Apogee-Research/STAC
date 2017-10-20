// Copyright (c) 1996-06 The Regents of the University of California. All
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
// File: CopyAction.java
// Classes: CopyAction
// Original Author: andrea.nironi@gmail.com
package org.tigris.gef.base;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.Icon;

import org.tigris.gef.presentation.Fig;
import org.tigris.gef.presentation.FigEdge;
import org.tigris.gef.util.Localizer;

/**
 * An Action to align 2 or more objects relative to each other.
 */
public class CopyAction extends AbstractAction {

    private static final long serialVersionUID = 8686090256848797031L;

    /**
     * Creates a new CopyAction
     */
    public CopyAction() {
        super();
    }

    /**
     * Creates a new CopyAction
     *
     * @param name The name of the action
     */
    public CopyAction(String name) {
        this(name, false);
    }

    /**
     * Creates a new CopyAction
     *
     * @param name The name of the action
     * @param icon The icon of the action
     */
    public CopyAction(String name, Icon icon) {
        this(name, icon, false);
    }

    /**
     * Creates a new CopyAction
     *
     * @param name The name of the action
     * @param localize Whether to localize the name or not
     */
    public CopyAction(String name, boolean localize) {
        super(localize ? Localizer.localize("GefBase", name) : name);
    }

    /**
     * Creates a new CopyAction
     *
     * @param name The name of the action
     * @param icon The icon of the action
     * @param localize Whether to localize the name or not
     */
    public CopyAction(String name, Icon icon, boolean localize) {
        super(localize ? Localizer.localize("GefBase", name) : name, icon);
    }

    public void actionPerformed(ActionEvent event) {
        Editor ce = Globals.curEditor();
        Vector copiedElements = ce.getSelectionManager().selections();
        Vector figs = new Vector();
        Enumeration copies = copiedElements.elements();
        while (copies.hasMoreElements()) {
            Selection s = (Selection) copies.nextElement();
            Fig f = s.getContent();
            if (f instanceof FigEdge) {
                continue;
            }
            // needs-more-work: add support for cut-and-paste of edges
            f = (Fig) f.clone();
            figs.addElement(f);
        }
        Globals.clipBoard = figs;
    }

    public void undoIt() {
        org.graph.commons.logging.LogFactory.getLog(null).info("Undo does not make sense for CmdCopy");
    }

    // Awaiting jdk 1.2
    /**
     * The DataFlavor used for our particular type of cut-and-paste data. This
     * one will transfer data in the form of a serialized Vector object. Note
     * that in Java 1.1.1, this works intra-application, but not between
     * applications. Java 1.1.1 inter-application data transfer is limited to
     * the pre-defined string and text data flavors.
     */
    // public static final DataFlavor dataFlavor =
    // new DataFlavor(Fig.class, "Fig");
    // protected Vector figs = new Vector(256,256); // Store the Figs.
    /**
     * Copy the current scribble and store it in a SimpleSelection object
     * (defined below). Then put that object on the clipboard for pasting.
     */
    // Going to have to wait for jdk 1.2 for this code to work.
    // public void copy(Fig fig) {
    // Get system clipboard
    // Clipboard c =
    // ProjectBrowser.TheInstance.getToolkit().getSystemClipboard();
    // Copy and save the scribble in a Transferable object
    // SimpleSelection f = new SimpleSelection(fig, dataFlavor);
    // Put that object on the clipboard
    // c.setContents(f, f);
    // Transferable t = c.getContents(ProjectBrowser.TheInstance);
    // if (t instanceof Transferable)
    // org.graph.commons.logging.LogFactory.getLog(null).info("Copy, success!");
    // org.graph.commons.logging.LogFactory.getLog(null).info("copy has been executed" + " t = " + t);
    // }
    /**
     * This nested class implements the Transferable and ClipboardOwner
     * interfaces used in data transfer. It is a simple class that remembers a
     * selected object and makes it available in only one specified flavor.
     */
    // Awaiting jdk 1.2
    static class SimpleSelection implements Transferable, ClipboardOwner {

        protected Fig selection; // The data to be transferred.
        protected DataFlavor flavor; // The one data flavor supported.

        public SimpleSelection(Fig selection, DataFlavor flavor) {
            this.selection = selection; // Specify data.
            this.flavor = flavor; // Specify flavor.
        }

        /**
         * Return the list of supported flavors. Just one in this case
         */
        public DataFlavor[] getTransferDataFlavors() {
            return new DataFlavor[]{flavor};
        }

        /**
         * Check whether we support a specified flavor
         */
        public boolean isDataFlavorSupported(DataFlavor f) {
            return f.equals(flavor);
        }

        /**
         * If the flavor is right, transfer the data (i.e. return it)
         */
        public Object getTransferData(DataFlavor f)
                throws UnsupportedFlavorException {
            if (f.equals(flavor)) {
                return selection;
            } else {
                throw new UnsupportedFlavorException(f);
            }
        }

        /**
         * This is the ClipboardOwner method. Called when the data is no longer
         * on the clipboard. In this case, we don't need to do much.
         */
        public void lostOwnership(Clipboard c, Transferable t) {
            selection = null;
        }
    }

} /* end class CmdCopy */
