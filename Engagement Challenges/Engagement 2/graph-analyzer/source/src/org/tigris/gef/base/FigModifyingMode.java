// $Id: FigModifyingMode.java 1265 2009-08-19 05:57:56Z mvw $
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
package org.tigris.gef.base;

import java.awt.Cursor;
import java.awt.Graphics;

import org.tigris.gef.presentation.Fig;

/**
 * This is the basic interface for all Modes that are manipulating the visual
 * representation of the underlying model. It is a subclass of Mode.
 *
 * @see Mode
 * @see FigModifyingModeImpl
 * @see Editor
 */
public interface FigModifyingMode extends Mode {
    // //////////////////////////////////////////////////////////////
    // accessors

    /**
     * Set the parent Editor of this Mode
     */
    public void setEditor(Editor w);

    /**
     * Get the parent Editor of this Mode
     */
    public Editor getEditor();

    /**
     * Returns the cursor that should be shown when this Mode starts.
     */
    public Cursor getInitialCursor();

    // //////////////////////////////////////////////////////////////
    // feedback to the user
    /**
     * Reply a string of instructions that should be shown in the statusbar when
     * this mode starts.
     */
    public String instructions();

    /**
     * Set the mouse cursor to some appropriate for this mode.
     */
    public void setCursor(Cursor c);

    // //////////////////////////////////////////////////////////////
    // painting methods
    /**
     * Modes can paint themselves to give the user feedback. For example,
     * ModePlace paints the object being placed. Mode's are drawn on top of
     * (after) the Editor's current view and on top of any selections.
     */
    public void paint(Graphics g);

    /**
     * Just calls paint(g) by default.
     */
    public void print(Graphics g);

    /**
     * Tests, if the actually handled fig is contained in the one given as
     * parameter.
     */
    public boolean isFigEnclosedIn(Fig testedFig, Fig enclosingFig);

} /* end interface FigModifyingMode */
