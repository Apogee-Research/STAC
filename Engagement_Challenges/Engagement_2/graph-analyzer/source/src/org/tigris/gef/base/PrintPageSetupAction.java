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
// File: PrintPageSetupAction.java
// Classes: PrintPageSetupAction
// Original Author: andrea.nironi@gmail.com
package org.tigris.gef.base;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Icon;

import org.tigris.gef.util.Localizer;

/**
 * Cmd to setup a page for printing.
 *
 * @author Eugenio Alvarez
 * @see PrintAction
 */
public class PrintPageSetupAction extends AbstractAction {

    private static final long serialVersionUID = 9121062856431140477L;
    private PrintAction printAction;

    /**
     * Creates a new PrintPageSetupAction
     *
     * @param name The name of the action
     * @param printAction The action to be printed
     */
    public PrintPageSetupAction(String name, PrintAction printAction) {
        this(name, printAction, false);
    }

    /**
     * Creates a new PrintPageSetupAction
     *
     * @param printAction The action to be printed
     */
    public PrintPageSetupAction(PrintAction printAction) {
        this("Page Setup", printAction, false);
    }

    /**
     * Creates a new PrintPageSetupAction
     *
     * @param name The name of the action
     * @param icon The icon of the action
     * @param printAction The action to be printed
     */
    public PrintPageSetupAction(String name, Icon icon, PrintAction printAction) {
        this(name, icon, printAction, false);
    }

    /**
     * Creates a new PrintPageSetupAction
     *
     * @param name The name of the action
     * @param printAction The action to be printed
     * @param localize Whether to localize the name or not
     */
    public PrintPageSetupAction(String name, PrintAction printAction,
            boolean localize) {
        super(localize ? Localizer.localize("GefBase", name) : name);
        this.printAction = printAction;
    }

    /**
     * Creates a new PrintPageSetupAction
     *
     * @param name The name of the action
     * @param icon The icon of the action
     * @param printAction The action to be printed
     * @param localize Whether to localize the name or not
     */
    public PrintPageSetupAction(String name, Icon icon,
            PrintAction printAction, boolean localize) {
        super(localize ? Localizer.localize("GefBase", name) : name, icon);
        this.printAction = printAction;
    }

    public void actionPerformed(ActionEvent e) {
        printAction.doPageSetup();
    }
}
