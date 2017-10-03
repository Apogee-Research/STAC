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
// File: SpawnAction.java
// Classes: SpawnAction
// Original Author: andrea.nironi@gmail.com
package org.tigris.gef.base;

import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Icon;

import org.tigris.gef.graph.presentation.JGraphFrame;
import org.tigris.gef.util.Localizer;

/**
 * Action to open a new editor on the same document as in the current editor.
 * Works by making a new JGraphFrame with a clone of the current editor. The
 * argument "dimension" may be set to th desired size of the new window.
 *
 * @see Editor
 * @see JGraphFrame
 */
public class SpawnAction extends AbstractAction {

    private static final long serialVersionUID = -7549484751631085721L;

    private String title;

    private Dimension dimension;

    public SpawnAction() {
        super();
    }

    /**
     * Creates a new SpawnAction
     *
     * @param name The name of the action
     */
    public SpawnAction(String name) {
        this(name, null, null, false);
    }

    /**
     * Creates a new SpawnAction
     *
     * @param name The name of the action
     */
    public SpawnAction(String name, String title, Dimension dimension) {
        this(name, title, dimension, false);
    }

    /**
     * Creates a new SpawnAction
     *
     * @param name The name of the action
     * @param icon The icon of the action
     */
    public SpawnAction(String name, Icon icon) {
        this(name, icon, null, null, false);
    }

    /**
     * Creates a new SpawnAction
     *
     * @param name The name of the action
     * @param icon The icon of the action
     */
    public SpawnAction(String name, Icon icon, String title, Dimension dimension) {
        this(name, icon, title, dimension, false);
    }

    /**
     * Creates a new SpawnAction
     *
     * @param name The name of the action
     * @param localize Whether to localize the name or not
     */
    public SpawnAction(String name, String title, Dimension dimension,
            boolean localize) {
        super(localize ? Localizer.localize("GefBase", name) : name);
        this.title = title;
        this.dimension = dimension;
    }

    /**
     * Creates a new SpawnAction
     *
     * @param name The name of the action
     * @param icon The icon of the action
     * @param localize Whether to localize the name or not
     */
    public SpawnAction(String name, Icon icon, String title,
            Dimension dimension, boolean localize) {
        super(localize ? Localizer.localize("GefBase", name) : name, icon);
        this.title = title;
        this.dimension = dimension;
    }

    public void actionPerformed(ActionEvent e) {
        Editor ce = Globals.curEditor();
        Editor ed = (Editor) ce.clone();
        // String title = (String) getArg("title", "new window");
        JGraphFrame jgf = new JGraphFrame(title, ed);
        // use clone because ce may be of a subclass of Editor
        // Object d = getArg("dimension");
        // if (d instanceof Dimension) jgf.setSize((Dimension)d);
        if (dimension != null) {
            jgf.setSize(dimension);
        }
        jgf.setVisible(true);
    }

}
