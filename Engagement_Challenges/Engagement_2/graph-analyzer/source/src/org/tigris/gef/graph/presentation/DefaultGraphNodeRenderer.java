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
// File: DefaultGraphNodeRenderer.java
// Classes: DefaultGraphNodeRenderer
// Original Author: jrobbins@ics.uci.edu
// $Id: DefaultGraphNodeRenderer.java 1153 2008-11-30 16:14:45Z bobtarling $
package org.tigris.gef.graph.presentation;

import java.util.Map;

import org.tigris.gef.base.Layer;
import org.tigris.gef.presentation.FigNode;
import org.tigris.gef.graph.*;

/**
 * An interface for FigNode factories. Similiar in concept to the Swing class
 * TreeCellRenderer. This Default class asks the NetNode to make its own
 * FigNode.
 *
 * @see org.tigris.gef.graph.presentation.NetNode#presentationFor
 */
public class DefaultGraphNodeRenderer implements GraphNodeRenderer,
        java.io.Serializable {

    private static final long serialVersionUID = -8396231710414093663L;

    /**
     * Return a Fig that can be used to represent the given node
     */
    public FigNode getFigNodeFor(GraphModel graph, Layer lay, Object node,
            Map styleAttributes) {
        if (node instanceof NetNode) {
            return ((NetNode) node).presentationFor(lay);
        }
        return null;
    }

    /**
     * Return a Fig that can be used to represent the given node
     */
    public FigNode getFigNodeFor(Object node, int x, int y, Map styleAttributes) {
        FigNode figNode = null;
        if (node instanceof NetNode) {
            figNode = ((NetNode) node).presentationFor(null);
            figNode.setLocation(x, y);
        }
        return null;
    }
} /* end class DefaultGraphNodeRenderer */
