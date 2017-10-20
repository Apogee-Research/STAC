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
// File: OpenSVGAction.java
// Classes: OpenSVGAction
// Original Author: andrea.nironi@gmail.com
package org.tigris.gef.base;

import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.AbstractAction;
import javax.swing.Icon;

import org.tigris.gef.graph.presentation.JGraphFrame;
import org.tigris.gef.persistence.svg.SvgParser;
import org.tigris.gef.util.Localizer;
import org.tigris.gef.util.Util;

/**
 * Action to Load a previously saved document document. The loaded editor is
 * displayed in a new JGraphFrame.
 *
 * @see ActionSave
 */
public class OpenSVGAction extends AbstractAction implements FilenameFilter {

    private static final long serialVersionUID = 596114767338320391L;

    private Dimension dimension;

    /**
     * Creates a new OpenSVGAction
     *
     * @param name The name of the action
     */
    public OpenSVGAction(String name) {
        this(name, null, false);
    }

    /**
     * Creates a new OpenSVGAction
     *
     * @param name The name of the action
     * @param dimension The dimension of the graph
     */
    public OpenSVGAction(String name, Dimension dimension) {
        this(name, dimension, false);
    }

    /**
     * Creates a new OpenSVGAction
     *
     * @param name The name of the action
     * @param icon The icon of the action
     */
    public OpenSVGAction(String name, Icon icon) {
        this(name, icon, null, false);
    }

    /**
     * Creates a new OpenSVGAction
     *
     * @param name The name of the action
     * @param icon The icon of the action
     * @param dimension The dimension of the graph
     */
    public OpenSVGAction(String name, Icon icon, Dimension dimension) {
        this(name, icon, dimension, false);
    }

    /**
     * Creates a new OpenSVGAction
     *
     * @param name The name of the action
     * @param dimension The dimension of the graph
     * @param localize Whether to localize the name or not
     */
    public OpenSVGAction(String name, Dimension dimension, boolean localize) {
        super(localize ? Localizer.localize("GefBase", name) : name);
        this.dimension = dimension;
    }

    /**
     * Creates a new OpenSVGAction
     *
     * @param name The name of the action
     * @param icon The icon of the action
     * @param dimension The dimension of the graph
     * @param localize Whether to localize the name or not
     */
    public OpenSVGAction(String name, Icon icon, Dimension dimension,
            boolean localize) {
        super(localize ? Localizer.localize("GefBase", name) : name, icon);
        this.dimension = dimension;
    }

    /**
     * Only let the user select files that match the filter. This does not seem
     * to be called under JDK 1.0.2 on solaris. I have not finished this method,
     * it currently accepts all filenames.
     * <p>
     *
     * Needs-More-Work: The source code for this function is duplicated in
     * CmdSave#accept.
     *
     * this method always returns true
     */
    public boolean accept(File dir, String name) {
        return true;
    }

    public void actionPerformed(ActionEvent event) {
        Editor ce = Globals.curEditor();
        FileDialog fd = new FileDialog(ce.findFrame(), "Open...",
                FileDialog.LOAD);
        fd.setFilenameFilter(this);
        fd.setDirectory(Globals.getLastDirectory());
        fd.setVisible(true);
        String filename = fd.getFile(); // blocking
        String path = fd.getDirectory(); // blocking
        Globals.setLastDirectory(path);

        if (filename != null) {
            try {
                Globals.showStatus("Reading " + path + filename + "...");
                URL url = Util.fileToURL(new File(path + filename));
                Diagram diag = SvgParser.SINGLETON.readDiagram(url);
                Editor ed = new Editor(diag);
                Globals.showStatus("Read " + path + filename);
                JGraphFrame jgf = new JGraphFrame(path + filename, ed);
                // Object d = getArg("dimension");
                // if (d instanceof Dimension) {
                // jgf.setSize((Dimension) d);
                // }
                if (dimension != null) {
                    jgf.setSize(dimension);
                }
                jgf.setVisible(true);
            } catch (MalformedURLException murle) {
                org.graph.commons.logging.LogFactory.getLog(null).info("bad URL");
            } catch (IOException e) {
                org.graph.commons.logging.LogFactory.getLog(null).info("IOExcept in opensvg");
            }
        }
    }
}
