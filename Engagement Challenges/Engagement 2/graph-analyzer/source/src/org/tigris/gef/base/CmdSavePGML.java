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
// File: CmdSave.java
// Classes: CmdSave
// Original Author: jrobbins@ics.uci.edu
// $Id: CmdSavePGML.java 1153 2008-11-30 16:14:45Z bobtarling $
package org.tigris.gef.base;

import java.awt.*;
import java.io.*;

import org.tigris.gef.ocl.ExpansionException;
import org.tigris.gef.ocl.OCLExpander;
import org.tigris.gef.ocl.TemplateReader;

/**
 * Cmd to save the current document to a binary file using Sun's
 * ObjectSerialization library. The written file contains the Editor object and
 * all objects reachable through instance variables of the Editor (e.g., the
 * selections, the views, the contents of the views, the net-level description
 * of the graph, etc.). UI objects such as Windows, Frames, Panels, and Images
 * are not stored because I have marked those instance variables as transient in
 * the source code.
 * <p>
 *
 * One advantage of this approach to saving and loading is that developers using
 * GEF can add subclasses (e.g., to NetNode) which introduce new instance
 * variables, and those will be saved and loaded without the developers having
 * to special load and save methods. However, make sure that you do not point to
 * any AWT objects unless those instance variables are transient because those
 * cannot be saved.
 * <p>
 *
 * Needs-More-Work: the files produced by a save are not really good for
 * anything other than reloading into this tool, or another Java program that
 * uses ObjectSerialization. At this time GEF provides no support for saving or
 * loading textual representations of documents that could be used in other
 * tools.
 * <p>
 *
 * in 0.12.3 use SavePGMLAction
 *
 * @see CmdOpen
 */
public class CmdSavePGML extends Cmd implements FilenameFilter {

    private static final long serialVersionUID = 5595884484082379585L;

    private static OCLExpander _expander = null;

    public CmdSavePGML() {
        super("SaveAsPGML");
    }

    /**
     * Only allow the user to select files that match the fiven filename
     * pattern. Needs-More-Work: this is not used yet.
     */
    public CmdSavePGML(String filterPattern) {
        this();
        setArg("filterPattern", filterPattern);
    }

    public void doIt() {
        // @@@ just for easy debugging
        try {
            _expander = new OCLExpander(TemplateReader.getInstance().read(
                    "/org/tigris/gef/xml/dtd/PGML.tee"));

            Editor ce = Globals.curEditor();
            Diagram d = new Diagram("junk", ce.getGraphModel(),
                    (LayerPerspective) ce.getLayerManager().getActiveLayer());
            FileDialog fd = new FileDialog(ce.findFrame(),
                    "Save Diagram in PGML format", FileDialog.SAVE);
            fd.setFilenameFilter(this);
            fd.setDirectory(Globals.getLastDirectory());
            fd.setVisible(true);
            String filename = fd.getFile(); // blocking
            String path = fd.getDirectory(); // blocking
            Globals.setLastDirectory(path);
            if (filename != null) {
                Globals.showStatus("Writing " + path + filename + "...");
                FileWriter fw = new FileWriter(path + filename);
                org.graph.commons.logging.LogFactory.getLog(null).info("Cmd save in PGML...");

                _expander.expand(fw, d);
                org.graph.commons.logging.LogFactory.getLog(null).info("save done");
                Globals.showStatus("Wrote " + path + filename);
                fw.close();
                // ce.setTitle(filename);
            }
        } catch (FileNotFoundException ignore) {
            org.graph.commons.logging.LogFactory.getLog(null).info("got an FileNotFoundException");
        } catch (IOException ignore) {
            org.graph.commons.logging.LogFactory.getLog(null).info("got an IOException");
            ignore.printStackTrace();
        } catch (ExpansionException e) {
            org.graph.commons.logging.LogFactory.getLog(null).info("got an Exception");
            e.printStackTrace();
        }
    }

    /**
     * Only let the user select files that match the filter. This does not seem
     * to be called under JDK 1.0.2 on solaris. I have not finished this method,
     * it currently accepts all filenames.
     * <p>
     *
     * Needs-More-Work: the source code for this method is duplicated in
     * CmdOpen#accept.
     */
    public boolean accept(File dir, String name) {
        org.graph.commons.logging.LogFactory.getLog(null).info("checking: " + dir + " " + name);
        if (containsArg("filterPattern")) {
            // if pattern dosen't match, return false
            return true;
        }
        return true; // no pattern was specified
    }

    public void undoIt() {
        org.graph.commons.logging.LogFactory.getLog(null).info("Undo does not make sense for CmdSave");
    }

    // protected void initTemplates() {
    // t.put(Diagram.class,
    // "<pgml>\n"+
    // " ($self.contents$)\n"+
    // "</pgml>");
    // t.put(Rectangle.class,
    // "x='($self.x$)' y='($self.y$)' "+
    // "w='($self.width$)' h='($self.height$)'");
    // t.put(FigRect.class,
    // "<rectangle ($self.bounds$) "+
    // " fill='($self.filled)'\n"+
    // " fillcolor='($self.fillColor)'\n"+
    // " stroke='($self.lineWidth)'\n"+
    // " strokecolor='($self.lineColor)'\n"+
    // "/>");
    // t.put(FigRRect.class,
    // "<rectangle ($self.bounds$)\n"+
    // " fill='($self.filled)'\n"+
    // " fillcolor='($self.fillColor)'\n"+
    // " stroke='($self.lineWidth)'\n"+
    // " strokecolor='($self.lineColor)'\n"+
    // " rounding='($self.cornerRadius$)'\n"+
    // "/>");
    // t.put(FigCircle.class,
    // "<ellipse ($self.bounds$)\n"+
    // " fill='($self.filled)'\n"+
    // " fillcolor='($self.fillColor)'\n"+
    // " stroke='($self.lineWidth)'\n"+
    // " strokecolor='($self.lineColor)'\n"+
    // "/>");
    // t.put(FigText.class,
    // "<text ($self.bounds$) \n"+
    // " fill='($self.filled)'\n"+
    // " fillcolor='($self.fillColor)'\n"+
    // " stroke='($self.lineWidth)'\n"+
    // " strokecolor='($self.lineColor)'\n"+
    // " textsize='($self.fontSize$)'\n"+
    // ">($self.text$)</text>");
    // t.put(FigLine.class,
    // "<path\n"+
    // " fill='($self.filled)'\n"+
    // " fillcolor='($self.fillColor)'\n"+
    // " stroke='($self.lineWidth)'\n"+
    // " strokecolor='($self.lineColor)'\n"+
    // ">\n"+
    // " <moveto x='($self.x1$)' y='($self.y1$)'>\n"+
    // " <lineto x='($self.x2$)' y='($self.y2$)'>\n"+
    // "</path>");
    // t.put(FigPoly.class,
    // "<path>\n"+
    // " <moveto x='($self.x1$)' y='($self.y1$)'>\n"+
    // " <lineto ($self.points$)>\n"+
    // "</path>");
    // //spline?
    // //image?
    // t.put(FigGroup.class,
    // "<group description='($self.class$)'\n"+
    // " href='($ref.owner$)\n"+
    // ">\n"+
    // " ($self.figs$)\n"+
    // "</group>");
    // t.put(FigEdge.class,
    // "<group description='($self.class$)'\n"+
    // " href='($ref.owner$)'\n"+
    // ">\n"+
    // " ($self.fig$)\n"+
    // "</group>");
    // }
} /* end class CmdSavePGML */
