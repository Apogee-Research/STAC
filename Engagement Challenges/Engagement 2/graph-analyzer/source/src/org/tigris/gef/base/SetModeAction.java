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
// File: SetModeAction.java
// Classes: SetModeAction
// Original Author: andrea.nironi@gmail.com
package org.tigris.gef.base;

import java.awt.event.ActionEvent;
import java.util.Hashtable;
import java.util.Properties;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.tigris.gef.util.Localizer;

/**
 * Action that sets the next global editor mode. The global editor mode effects
 * the next editor that you move the mouse into. For example, in PaletteFig the
 * Line button sets the next global mode to ModeCreateFigLine.
 *
 */
public class SetModeAction extends AbstractAction {

    private static final long serialVersionUID = -5362721817833001571L;
    protected Hashtable modeArgs;
    protected Hashtable args;

    /**
     * Creates a new SetModeAction
     */
    public SetModeAction(Properties args) {
        this("SetEditorMode", args);
    }

    /**
     * Creates a new SetModeAction
     */
    public SetModeAction(String name, Properties args) {
        super(name);
        this.args = args;
    }

    /**
     * Creates a new SetModeAction
     *
     * @param name The name of the action
     */
    public SetModeAction(String name) {
        this(name, false);
    }

    /**
     * Creates a new SetModeAction
     *
     * @param name The name of the action
     * @param icon The icon of the action
     */
    public SetModeAction(String name, Icon icon) {
        this(name, icon, false);
    }

    /**
     * Creates a new SetModeAction
     *
     * @param name The name of the action
     * @param localize Whether to localize the name or not
     */
    public SetModeAction(String name, boolean localize) {
        super(localize ? Localizer.localize("GefBase", name) : name);
    }

    /**
     * Creates a new SetModeAction
     *
     * @param name The name of the action
     * @param icon The icon of the action
     * @param localize Whether to localize the name or not
     */
    public SetModeAction(String name, Icon icon, boolean localize) {
        super(localize ? Localizer.localize("GefBase", name) : name, icon);
    }

    /**
     * Set the next global mode to the named mode.
     */
    public SetModeAction(Class modeClass) {
        super("SetEditorMode");
        setArg("desiredModeClass", modeClass);
    }

    public SetModeAction(Class modeClass, String name) {
        super(name);
        setArg("desiredModeClass", modeClass);
    }

    /**
     * Set the next global mode to the named mode, and maybe make it sticky.
     */
    public SetModeAction(Class modeClass, boolean sticky) {
        super("SetEditorMode");
        setArg("desiredModeClass", modeClass);
        setArg("shouldBeSticky", sticky ? Boolean.TRUE : Boolean.FALSE);
    }

    /**
     * Set the next global mode to the named mode, and set all arguments.
     */
    public SetModeAction(Class modeClass, Hashtable modeArgs) {
        super("SetEditorMode");
        setArg("desiredModeClass", modeClass);
        this.modeArgs = modeArgs;
    }

    public SetModeAction(Class modeClass, String arg, Object value) {
        this(modeClass, arg, value, "SetEditorMode");
        modeArgs = new Hashtable(1);
        modeArgs.put(arg, value);
        setArg("desiredModeClass", modeClass);
    }

    public SetModeAction(Class modeClass, String arg, Object value, String name) {
        super(name);
        modeArgs = new Hashtable(1);
        modeArgs.put(arg, value);
        setArg("desiredModeClass", modeClass);
    }

    public SetModeAction(Class modeClass, String arg, Object value,
            String name, ImageIcon icon) {
        super(name, icon);
        modeArgs = new Hashtable(1);
        modeArgs.put(arg, value);
        setArg("desiredModeClass", modeClass);
    }

    public void actionPerformed(ActionEvent e) {
        Mode mode;
        Class desiredModeClass = (Class) getArg("desiredModeClass");
        // needs-more-work: if mode is not defined, prompt the user
        try {
            mode = (Mode) desiredModeClass.newInstance();
        } catch (java.lang.InstantiationException ignore) {
            return;
        } catch (java.lang.IllegalAccessException ignore) {
            return;
        }
        mode.init(modeArgs);
        Boolean shouldBeSticky = (Boolean) getArg("shouldBeSticky");
        if (shouldBeSticky == null) {
            Globals.mode(mode);
        } else {
            Globals.mode(mode, shouldBeSticky.booleanValue());
        }
    }

    /**
     * Store the given argument under the given name.
     */
    private void setArg(String key, Object value) {
        if (args == null) {
            args = new Hashtable();
        }
        args.put(key, value);
    }

    /**
     * Get the object stored as an argument under the given name.
     */
    private Object getArg(String key) {
        if (args == null) {
            return null;
        } else {
            return args.get(key);
        }
    }
}
