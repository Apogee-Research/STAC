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
// File: CmdSetMode.java
// Classes: CmdSetMode
// Original Author: jrobbins@ics.uci.edu
// $Id: CmdSetMode.java 1153 2008-11-30 16:14:45Z bobtarling $
package org.tigris.gef.base;

import java.util.*;
import javax.swing.ImageIcon;

/**
 * Cmd that sets the next global editor mode. The global editor mode effects the
 * next editor that you move the mouse into. For example, in PaletteFig the Line
 * button sets the next global mode to ModeCreateFigLine.
 *
 * in 0.12.3 use SetModeAction
 */
public class CmdSetMode extends Cmd {

    private static final long serialVersionUID = 6891964133574261999L;

    /**
     * Arguments to pass to the new mode instance after creation.
     */
    protected Hashtable _modeArgs;

    public CmdSetMode(Properties args) {
        super(args, "SetEditorMode");
    }

    /**
     * Set the next global mode to the named mode.
     */
    public CmdSetMode(Class modeClass) {
        super("SetEditorMode");
        setArg("desiredModeClass", modeClass);
    }

    public CmdSetMode(Class modeClass, String name) {
        super(name);
        setArg("desiredModeClass", modeClass);
    }

    /**
     * Set the next global mode to the named mode, and maybe make it sticky.
     */
    public CmdSetMode(Class modeClass, boolean sticky) {
        super("SetEditorMode");
        setArg("desiredModeClass", modeClass);
        setArg("shouldBeSticky", sticky ? Boolean.TRUE : Boolean.FALSE);
    }

    /**
     * Set the next global mode to the named mode, and set all arguments.
     */
    public CmdSetMode(Class modeClass, Hashtable modeArgs) {
        super("SetEditorMode");
        setArg("desiredModeClass", modeClass);
        _modeArgs = modeArgs;
    }

    public CmdSetMode(Class modeClass, String arg, Object value) {
        super("SetEditorMode");
        _modeArgs = new Hashtable(1);
        _modeArgs.put(arg, value);
        setArg("desiredModeClass", modeClass);
    }

    public CmdSetMode(Class modeClass, String arg, Object value, String name) {
        super(name);
        _modeArgs = new Hashtable(1);
        _modeArgs.put(arg, value);
        setArg("desiredModeClass", modeClass);
    }

    public CmdSetMode(Class modeClass, String arg, Object value, String name,
            ImageIcon icon) {
        super(null, name, icon);
        _modeArgs = new Hashtable(1);
        _modeArgs.put(arg, value);
        setArg("desiredModeClass", modeClass);
    }

    public void doIt() {
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
        mode.init(_modeArgs);
        Boolean shouldBeSticky = (Boolean) getArg("shouldBeSticky");
        if (shouldBeSticky == null) {
            Globals.mode(mode);
        } else {
            Globals.mode(mode, shouldBeSticky.booleanValue());
        }
    }

    public void undoIt() {
        org.graph.commons.logging.LogFactory.getLog(null).info("undo does not make sense for setting modes");
    }
} /* end class CmdSetMode */
