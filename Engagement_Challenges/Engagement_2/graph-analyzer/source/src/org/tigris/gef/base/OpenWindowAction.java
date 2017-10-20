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
// File: OpenWindowAction.java
// Classes: OpenWindowAction
// Original Author: andrea.nironi@gmail.com
package org.tigris.gef.base;

import java.awt.Frame;
import java.awt.event.ActionEvent;

import javax.swing.Icon;

import org.tigris.gef.undo.UndoableAction;
import org.tigris.gef.util.Localizer;

/**
 * Action to open a user interface dialog window. Given the name of a subclass
 * of Frame, this Action makes a new instance and calls show(). For example,
 * used to open a list of some availible commands.
 *
 * @see org.tigris.gef.graph.presentation.JGraphFrame
 */
public class OpenWindowAction extends UndoableAction {

    private static final long serialVersionUID = 8660792603517868506L;
    private String className;

    /**
     * Creates a new OpenWindowAction
     *
     * @param name The name of the action
     */
    public OpenWindowAction(String name, String className) {
        this(name, className, false);
    }

    /**
     * Creates a new OpenWindowAction
     *
     * @param name The name of the action
     * @param icon The icon of the action
     */
    public OpenWindowAction(String name, String className, Icon icon) {
        this(name, icon, className, false);
    }

    /**
     * Creates a new OpenWindowAction
     *
     * @param name The name of the action
     * @param localize Whether to localize the name or not
     */
    public OpenWindowAction(String name, String className, boolean localize) {
        super(localize ? Localizer.localize("GefBase", name) : name);
        this.className = className;
    }

    /**
     * Creates a new OpenWindowAction
     *
     * @param name The name of the action
     * @param icon The icon of the action
     * @param localize Whether to localize the name or not
     */
    public OpenWindowAction(String name, Icon icon, String className,
            boolean localize) {
        super(localize ? Localizer.localize("GefBase", name) : name, icon);
        this.className = className;
    }

    public void actionPerformed(ActionEvent e) {
        // String className = (String) getArg("className");
        Frame window;
        Class clazz;
        if (className != null) {
            Globals.showStatus("Opening window for " + className);
            try {
                clazz = Class.forName(className);
            } catch (java.lang.ClassNotFoundException ignore) {
                return;
            }

            try {
                window = (Frame) clazz.newInstance();
            } catch (java.lang.IllegalAccessException ignore) {
                return;
            } catch (java.lang.InstantiationException ignore) {
                return;
            }
            window.setVisible(true);
            return;
        }
        org.graph.commons.logging.LogFactory.getLog(null).info("invalid window name");
    }
}
