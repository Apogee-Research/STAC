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
// File: ModeImpl.java
// Classes: ModeImpl
// Original Author: thorsten Jun 2000
// $Id: ModeImpl.java 1153 2008-11-30 16:14:45Z bobtarling $
package org.tigris.gef.base;

import java.awt.event.*;
import java.io.Serializable;
import java.util.Hashtable;

/**
 * This is the default implementation for the basic interface of all modes. It
 * provides basic functionality for initializing the mode and handling its
 * parameters. All of the methods can be overwritten, but this is not always
 * necessary.
 *
 * @see Mode
 * @see FigModifyingModeImpl
 */
public class ModeImpl implements Mode, Serializable, KeyListener,
        MouseListener, MouseMotionListener {

    private static final long serialVersionUID = -3707221186028816573L;

    /**
     * Arguments to this mode. These are usually set just after the mode is
     * created via the init method and used later.
     */
    protected Hashtable _args = new Hashtable();

    // //////////////////////////////////////////////////////////////
    // constructors
    /**
     * Construct a new Mode instance with the given parameters as its initial
     * parameters
     */
    public ModeImpl(Hashtable parameters) {
        setArgs(parameters);
    }

    /**
     * Construct a new ModeImpl instance without any parameters. This
     * constructor is needed because some Cmd-Classes can only call
     * Class.newInstance which does not pass constructor arguments.
     *
     * @see CmdSetMode
     */
    public ModeImpl() {
    }

    // //////////////////////////////////////////////////////////////
    // Arguments
    public void setArgs(Hashtable args) {
        _args = args;
    }

    public void setArg(String key, Object value) {
        if (_args == null) {
            _args = new Hashtable();
        }
        _args.put(key, value);
    }

    public Hashtable getArgs() {
        return _args;
    }

    public Object getArg(String s) {
        if (_args == null) {
            return null;
        }
        return _args.get(s);
    }

    // //////////////////////////////////////////////////////////////
    // methods related to transitions among modes
    /**
     * When a Mode handles a certain event that indicates that the user wants to
     * exit that Mode (e.g., a mouse up event after a drag in ModeCreateEdge)
     * the Mode calls done to make switching to another Mode possible.
     */
    public void done() {
    }

    /**
     * When the user performs the first AWT Event that indicate that they want
     * to do some work in this mode, then change the global next mode.
     */
    public void start() {
        Globals.nextMode();
    }

    /**
     * Some Mode's should never be exited, but by default any Mode can exit.
     * Mode's which return false for canExit() will not be popped from a
     * ModeManager.
     *
     * @see ModeManager
     */
    public boolean canExit() {
        return true;
    }

    /**
     * Modes may need some parameters in order to work properly. With this
     * method, a Mode can be inititalized with a unspecified number of
     * parameters. Call this method first, before using a Mode.
     */
    public void init(Hashtable parameters) {
        setArgs(parameters);
    }

    /**
     * Modes can be finished before completed for some reasons. This method lets
     * the mode be finished from any state it is in.
     */
    public void leave() {
        Globals.setSticky(false);
        done();
        Globals.nextMode();
        Editor editor = Globals.curEditor();
        if (editor != null) {
            editor.finishMode();
        }
    }

    // //////////////////////////////////////////////////////////////
    // event handlers
    public void keyPressed(KeyEvent ke) {
    }

    public void keyReleased(KeyEvent ke) {
    }

    public void keyTyped(KeyEvent ke) {
    }

    public void mouseMoved(MouseEvent me) {
    }

    public void mouseDragged(MouseEvent me) {
    }

    public void mouseClicked(MouseEvent me) {
    }

    public void mousePressed(MouseEvent me) {
    }

    public void mouseReleased(MouseEvent me) {
    }

    public void mouseExited(MouseEvent me) {
    }

    public void mouseEntered(MouseEvent me) {
    }

} /* end class Mode */
