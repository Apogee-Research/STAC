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
package org.tigris.gef.persistence.pgml;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import org.tigris.gef.base.Layer;
import org.tigris.gef.presentation.Fig;
import org.tigris.gef.presentation.FigEdge;
import org.tigris.gef.presentation.FigGroup;

/**
 * Utility methods referred to by PGML.tee
 *
 * @since 0.11.1 12-May-2005
 * @author Bob Tarling
 * @stereotype utility
 */
public class PgmlUtility {

    /**
     * Get the PGML description of a color. If possible this is a text
     * description otherwise it is in red green blue integer format seperated by
     * spaces.
     *
     * @param color The color to convert to PGML style
     * @return a string representing the color in pgml format
     */
    public static String getColor(Color color) {
        String colorDescr = getColorName(color);
        if (colorDescr != null) {
            return colorDescr;
        }
        return color.getRed() + " " + color.getGreen() + " " + color.getBlue();
    }

    /**
     * Get a color name for a color or null if this is some custom color.
     *
     * @param color
     * @return the color name or null.
     */
    private static String getColorName(Color color) {

        String colorName = null;

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

    /**
     * Translate the visibility flag of a Fig to the PGML "visibility" attribute
     * value. The PGML values are 0=hidden and 1=shown. If not specified then 1
     * is the default so we return null for this to prevent redundent data being
     * written to PGML.
     *
     * @param f The Fig
     * @return "0"=hidden, null=shown
     */
    public static String getVisibility(Fig f) {
        if (f.isVisible()) {
            return null;
        }
        return "0";
    }

    /**
     * Translate the dashed flag of a Fig to the PGML "dashed" attribute value.
     *
     * @param f The Fig
     * @return 0=not dashed, 1=dashed
     */
    public static int getDashed(Fig f) {
        return (f.getDashed()) ? 1 : 0;
    }

    /**
     * Translate the filled flag of a Fig to the PGML "filled" attribute value.
     *
     * @param f The Fig
     * @return 0=not filled, 1=filled
     */
    public static int getFilled(Fig f) {
        return (f.getFilled()) ? 1 : 0;
    }

    /**
     * Get all contents of a layer other than the edges.
     */
    public static List getContentsNoEdges(Layer lay) {
        List contents = lay.getContents();
        int size = contents.size();
        ArrayList list = new ArrayList(size);
        for (int i = 0; i < size; i++) {
            Object o = contents.get(i);
            if (!(o instanceof FigEdge)) {
                list.add(o);
            }
        }
        return list;
    }

    /**
     * Generate an identifier for this Fig which is unique within the diagram.
     *
     * @param f the Fig to generate the id for
     * @return a unique string
     */
    public static String getId(Fig f) {
        if (f == null) {
            throw new IllegalArgumentException("A fig must be supplied");
        }
        if (f.getGroup() != null) {
            String groupId = f.getGroup().getId();
            if (f.getGroup() instanceof FigGroup) {
                FigGroup group = (FigGroup) f.getGroup();
                return groupId + "." + ((List) group.getFigs()).indexOf(f);
            } else if (f.getGroup() instanceof FigEdge) {
                FigEdge edge = (FigEdge) f.getGroup();
                return groupId + "."
                        + (((List) edge.getPathItemFigs()).indexOf(f) + 1);
            } else {
                return groupId + ".0";
            }
        }

        Layer layer = f.getLayer();
        if (layer == null) {
            return "LAYER_NULL";
        }

        List c = (List) layer.getContents();
        int index = c.indexOf(f);
        return "Fig" + index;
    }

}
