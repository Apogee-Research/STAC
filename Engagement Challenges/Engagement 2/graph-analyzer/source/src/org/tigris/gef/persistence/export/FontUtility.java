//%1032269233760:org.tigris.gef.xml.pgml%
//Copyright (c) 1996-99 The Regents of the University of California. All
//Rights Reserved. Permission to use, copy, modify, and distribute this
//software and its documentation without fee, and without a written
//agreement is hereby granted, provided that the above copyright notice
//and this paragraph appear in all copies.  This software program and
//documentation are copyrighted by The Regents of the University of
//California. The software program and documentation are supplied "AS
//IS", without any accompanying services from The Regents. The Regents
//does not warrant that the operation of the program will be
//uninterrupted or error-free. The end-user understands that the program
//was developed for research purposes and is advised not to rely
//exclusively on the program for any reason.  IN NO EVENT SHALL THE
//UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT,
//SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES, INCLUDING LOST PROFITS,
//ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
//THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
//SUCH DAMAGE. THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY
//WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
//MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
//PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
//CALIFORNIA HAS NO OBLIGATIONS TO PROVIDE MAINTENANCE, SUPPORT,
//UPDATES, ENHANCEMENTS, OR MODIFICATIONS.
package org.tigris.gef.persistence.export;

import java.awt.Font;
import java.awt.FontMetrics;

import javax.swing.JPanel;

/**
 * A utility class providing helper methods for fonts.
 *
 * @since 0.11.1 11-May-2005
 * @author Bob Tarling
 * @stereotype utility
 */
public class FontUtility {

    /**
     * A JPanel from which we can gain font metrics.
     */
    private static final JPanel DUMMY_PANEL = new JPanel();

    /**
     * The constructor is not accessible for a utility.
     */
    private FontUtility() {
    }

    /**
     * Gets the font metrics for the specified font.
     *
     * @param font The font for which font metrics is to be obtained.
     * @return the FontMetrics
     */
    public static FontMetrics getFontMetrics(Font font) {
        // return Toolkit.getDefaultToolkit().getFontMetrics(font);
        return DUMMY_PANEL.getFontMetrics(font);
    }
}
