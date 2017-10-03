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
package org.tigris.gef.persistence.svg;

import java.awt.Color;

/**
 * Utility methods referred to by SVG.tee
 *
 * @since 0.11.1 12-May-2005
 * @author Bob Tarling
 * @stereotype utility
 */
public class SvgUtility {

    /**
     * Get the SVG description of a color. If possible this is a text
     * description otherwise it is in hexadecimal red green blue format of
     * either #rrggbb or #rgb.
     *
     * @param color The color to convert to SVG style
     * @return a string representing the color in SVG format
     */
    public static String getColor(Color color) {
        String colorDescr = getColorName(color);
        if (colorDescr != null) {
            return colorDescr;
        }

        int red = color.getRed();
        int green = color.getGreen();
        int blue = color.getBlue();

        if (red < 15 && green < 15 && blue < 15) {
            return "#" + Integer.toHexString(color.getRed()) + " "
                    + Integer.toHexString(color.getGreen()) + " "
                    + Integer.toHexString(color.getBlue());
        }
        return "#" + colorToHex(color.getRed()) + " "
                + colorToHex(color.getGreen()) + " "
                + colorToHex(color.getBlue());
    }

    /**
     * Convert an integer representing a color scale to 2 hex digits.
     *
     * @param integer
     * @return A string of exactly 2 hexadecimal digits.
     */
    private static String colorToHex(int integer) {
        if (integer > 255) {
            throw new IllegalArgumentException(
                    "Color value cannot be great than 255");
        }
        if (integer > 15) {
            return Integer.toHexString(integer);
        }

        return "0" + Integer.toHexString(integer);
    }

    /**
     * Get a color name for a color or null if this is some custom color.
     *
     * @param color
     * @return the color name or null.
     */
    private static String getColorName(Color color) {

        String colorName = null;

        // TODO expand to full SVG color list.
        if (color.equals(Color.white)) {
            colorName = "white";
        } else if (color.equals(Color.black)) {
            colorName = "black";
        } else if (color.equals(Color.red)) {
            colorName = "red";
        } else if (color.equals(Color.green)) {
            colorName = "green";
        } else if (color.equals(Color.blue)) {
            colorName = "blue";
        }

        return colorName;
    }
}
