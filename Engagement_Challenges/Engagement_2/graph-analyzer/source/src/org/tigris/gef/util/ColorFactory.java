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
package org.tigris.gef.util;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * A flyweight factory class used to get color instances. This only creates new
 * instances of a Color if required. Previous instances are cached.
 *
 * @since 0.11.1 10-May-2005
 * @author Bob Tarling
 * @stereotype utility
 */
public class ColorFactory {

    /**
     * A map of previously created colors mapped by an RGB string description in
     * the form "rrr ggg bbb" where rrr = red value int ggg = green value int
     * and bbb = blue value int.
     */
    private static final Map USED_COLORS_BY_RGB_STRING = new HashMap();
    private static final Map USED_COLORS_BY_RGB_INTEGER = new HashMap();

    static {
        cacheColor(Color.white);
        cacheColor(Color.black);
        cacheColor(Color.red);
        cacheColor(Color.green);
        cacheColor(Color.blue);
    }

    /**
     * A utility
     */
    private ColorFactory() {
    }

    /**
     * A flyweight factory method for reusing the same Color value multiple
     * times.
     *
     * @param colorDescr A string of RGB values seperated by space or a color
     * name recognised by PGML (later to include SVG)
     * @param defaultColor a color to return if the color description can't be
     * interpretted.
     * @return the equivilent Color
     */
    public static Color getColor(String colorDescr, Color defaultColor) {
        Color color = getColor(colorDescr);

        if (color != null) {
            return color;
        }

        return defaultColor;
    }

    /**
     * A flyweight factory method for reusing the same Color value multiple
     * times.
     *
     * @param colorDescr A string of RGB values seperated by space or a color
     * name recognised by PGML (later to include SVG)
     * @return the equivilent Color
     */
    public static Color getColor(String colorDescr) {
        Color color = null;
        if (colorDescr.equalsIgnoreCase("white")) {
            color = Color.white;
        } else if (colorDescr.equalsIgnoreCase("black")) {
            color = Color.black;
        } else if (colorDescr.equalsIgnoreCase("red")) {
            color = Color.red;
        } else if (colorDescr.equalsIgnoreCase("green")) {
            color = Color.green;
        } else if (colorDescr.equalsIgnoreCase("blue")) {
            color = Color.blue;
        } else if (colorDescr.indexOf(' ') > 0) {
            // If there any spaces we assume this is a space
            // seperated string of RGB values
            color = getColorByRgb(colorDescr);
        } else {
            // Otherwise we assume its a single integer value
            color = getColorByRgb(Integer.valueOf(colorDescr));
        }
        return color;
    }

    /**
     * Get a color based on a space seperated RGB string.
     *
     * @param colorDescr an RGB description of the color as integers seperated
     * by spaces.
     * @return the required Color object.
     */
    private static Color getColorByRgb(String colorDescr) {
        Color color = (Color) USED_COLORS_BY_RGB_STRING.get(colorDescr);
        if (color == null) {
            StringTokenizer st = new StringTokenizer(colorDescr, " ");
            int red = Integer.parseInt(st.nextToken());
            int green = Integer.parseInt(st.nextToken());
            int blue = Integer.parseInt(st.nextToken());
            color = new Color(red, green, blue);
            cacheColor(colorDescr, color);
        }

        return color;
    }

    /**
     * Get a color based on a single RGB integer.
     *
     * @param rgbInteger the integer value of the color.
     * @return the required Color object.
     */
    private static Color getColorByRgb(Integer rgbInteger) {
        Color color = (Color) USED_COLORS_BY_RGB_INTEGER.get(rgbInteger);
        if (color == null) {
            color = Color.decode(rgbInteger.toString());
            cacheColor(rgbInteger, color);
        }

        return color;
    }

    /**
     * Cache a Color the indexes will be deduced.
     *
     * @param color
     */
    private static void cacheColor(Color color) {
        cacheColor(colorToInteger(color), color);
    }

    /**
     * Cache a Color providing the RGB string by which it can be retrieved
     *
     * @param stringIndex
     * @param color
     */
    private static void cacheColor(String stringIndex, Color color) {
        cacheColor(stringIndex, colorToInteger(color), color);
    }

    /**
     * Convert a Color to an single Integer value.
     *
     * @param color The color
     * @return the single integer value representing the Color
     */
    private static Integer colorToInteger(Color color) {
        return new Integer(color.getRGB());
        // Integer.valueOf(color.getRGB()); - TODO when JRE1.4 support dropped
    }

    /**
     * Cache a Color providing the RGB integer by which it can be retrieved
     *
     * @param intIndex
     * @param color
     */
    private static void cacheColor(Integer intIndex, Color color) {
        cacheColor(color.getRed() + " " + color.getGreen() + " "
                + color.getBlue(), intIndex, color);
    }

    /**
     * Cache a Color providing all the indexes by which it can be retrieved
     *
     * @param stringIndex
     * @param intIndex
     * @param color
     */
    private static void cacheColor(String stringIndex, Integer intIndex,
            Color color) {
        USED_COLORS_BY_RGB_INTEGER.put(intIndex, color);
        USED_COLORS_BY_RGB_STRING.put(stringIndex, color);
    }
}
