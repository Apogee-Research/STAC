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
// File: FigModifyingMode.java
// Classes: FigModifyingMode
// Original Author: thorsten Jun 2000
// $Id: FigModifyingModeImpl.java 1265 2009-08-19 05:57:56Z mvw $
package org.tigris.gef.base;

import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.Hashtable;

import org.tigris.gef.presentation.Fig;
import org.tigris.gef.undo.UndoManager;

/**
 * This is the default implementation for all Modes that are manipulating the
 * visual representation of the underlying model. It is a subclass of ModeImpl
 * and implements FigModifyingMode. The provide simple functionality and some
 * instance variables only. Although this class can be instantiated, it is not
 * designed to be used as an independent Mode. Any FigModifyingMode that is tend
 * to be used by the system should be designed as a subclass of this class and
 * overwrite the methods if necessary.
 *
 * @see Editor
 * @see FigModifyingMode
 * @see ModeImpl
 */
public class FigModifyingModeImpl extends ModeImpl implements FigModifyingMode {
    // //////////////////////////////////////////////////////////////
    // instance variables

    /**
     * The Editor that is in this mode. Each Mode instance belongs to exactly
     * one Editor instance.
     */
    protected Editor editor;

    // //////////////////////////////////////////////////////////////
    // constructors
    /**
     * Construct a new Mode instance with the given Editor as its editor
     */
    public FigModifyingModeImpl(Editor par) {
        setEditor(par);
    }

    /**
     * Constructs a new Mode instance with some initial parameters. At least a
     * parameter representing the the Editor, this Mode belongs to, should be
     * provided.
     */
    public FigModifyingModeImpl(Hashtable parameters) {
        init(parameters);
    }

    /**
     * Construct a new Mode instance without any Editor as its parent, the
     * parent must be filled in before the instance is actually used. This
     * constructor is needed because CmdSetMode can only call Class.newInstance
     * which does not pass constructor arguments. A call to init is necessary in
     * order to let this instance work properly.
     *
     */
    public FigModifyingModeImpl() {
    }

    // //////////////////////////////////////////////////////////////
    // methods related to transitions among modes
    public void done() {
        setCursor(Cursor.getDefaultCursor());
        editor.finishMode();
        UndoManager.getInstance().removeMementoLock(this);
    }

    // //////////////////////////////////////////////////////////////
    // accessors
    /**
     * Set the parent Editor of this Mode
     */
    public void setEditor(Editor w) {
        editor = w;
        setCursor(getInitialCursor());
    }

    /**
     * Get the parent Editor of this Mode
     */
    public Editor getEditor() {
        return editor;
    }

    /**
     * Returns the cursor that should be shown when this Mode starts.
     */
    public Cursor getInitialCursor() {
        return Cursor.getDefaultCursor();
    }

    // //////////////////////////////////////////////////////////////
    // feedback to the user
    /**
     * Reply a string of instructions that should be shown in the statusbar when
     * this mode starts.
     */
    public String instructions() {
        return "FigModifyingMode: " + getClass().getName();
    }

    /**
     * Set the mouse cursor to some appropriate for this mode.
     */
    public void setCursor(Cursor c) {
        if (editor != null) {
            editor.setCursor(c);
        }
    }

    // //////////////////////////////////////////////////////////////
    // painting methods
    /**
     * Modes can paint themselves to give the user feedback. For example,
     * ModePlace paints the object being placed. Mode's are drawn on top of
     * (after) the Editor's current view and on top of any selections.
     */
    public void paint(Graphics g) {
        paint((Object) g);
    }

    /**
     * Just calls paint(g) by default.
     */
    public void print(Graphics g) {
        print((Object) g);
    }

    /**
     * Modes can paint themselves to give the user feedback. For example,
     * ModePlace paints the object being placed. Mode's are drawn on top of
     * (after) the Editor's current view and on top of any selections.
     */
    final public void paint(Object graphicsContext) {
    }

    /**
     * Just calls paint(g) by default.
     */
    final public void print(Object graphicsContext) {
        paint(graphicsContext);
    }

    public boolean isFigEnclosedIn(Fig testedFig, Fig enclosingFig) {
        Rectangle bbox = testedFig.getBounds();
        Rectangle trap = enclosingFig.getTrapRect();
        if (trap != null
                && (trap.contains(bbox.x, bbox.y) && trap.contains(bbox.x
                        + bbox.width, bbox.y + bbox.height))) {
            return true;
        }
        return false;
    }

    static final long serialVersionUID = 7960954871341784898L;
} /* end class FigModifyingModeImpl */
