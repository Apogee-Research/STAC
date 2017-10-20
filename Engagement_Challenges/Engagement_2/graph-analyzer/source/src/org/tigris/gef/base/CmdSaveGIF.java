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
// File: CmdSaveGIF.java
// Classes: CmdSaveGIF
// Original Author: stevep@wrq.com
package org.tigris.gef.base;

import java.awt.*;
import java.awt.image.*;
import java.io.*;

import Acme.JPM.Encoders.GifEncoder;

/**
 * Cmd to save a diagram as a GIF image in a supplied OutputStream. Requires the
 * Acme.JPM.Encoders.GifEncoder class. Operates on the diagram in the current
 * editor.
 *
 * Code loosely adapted from CmdPrint.
 *
 * in 0.12.3 use SaveGIFAction
 *
 * @author Steve Poole, stevep@wrq.com
 */
public class CmdSaveGIF extends CmdSaveGraphics {

    private static final long serialVersionUID = 4044142753088912626L;

    /**
     * Used as background color in image and set transparent. Chosen because
     * it's unlikely to be selected by the user, and leaves the diagram readable
     * if viewed without transparency.
     */
    public static final int TRANSPARENT_BG_COLOR = 0x00efefef;

    public CmdSaveGIF() {
        super("SaveGIF");
    }

    /**
     * Write the diagram contained by the current editor into an OutputStream as
     * a GIF image.
     */
    protected void saveGraphics(OutputStream s, Editor ce, Rectangle drawingArea)
            throws IOException {

        // Create an offscreen image and render the diagram into it.
        Image i = ce.createImage(drawingArea.width * scale, drawingArea.height
                * scale);
        Graphics g = i.getGraphics();
        if (g instanceof Graphics2D) {
            ((Graphics2D) g).scale(scale, scale);
        }
        g.setColor(new Color(TRANSPARENT_BG_COLOR));
        g.fillRect(0, 0, drawingArea.width * scale, drawingArea.height * scale);
        // a little extra won't hurt
        g.translate(-drawingArea.x, -drawingArea.y);
        ce.print(g);

        // Tell the Acme GIF encoder to save the image as a GIF into the
        // output stream. Use the TransFilter to make the background
        // color transparent.
        try {
            FilteredImageSource fis = new FilteredImageSource(i.getSource(),
                    new TransFilter(TRANSPARENT_BG_COLOR));
            GifEncoder ge = new GifEncoder(fis, s);
            // GifEncoder ge = new GifEncoder( i, s );
            ge.encode();
        } catch (IOException e) {
            e.printStackTrace();
        }

        g.dispose();
        // force garbage collection, to prevent out of memory exceptions
        g = null;
        i = null;
    }

} /* end class CmdSaveGIF */
