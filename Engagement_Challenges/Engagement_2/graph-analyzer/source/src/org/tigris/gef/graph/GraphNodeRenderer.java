// Copyright (c) 1996-2008 The Regents of the University of California. All
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
// File: GraphNodeRenderer.java
// Interfaces: GraphNodeRenderer
// Original Author: jrobbins@ics.uci.edu
// $Id: GraphNodeRenderer.java 1200 2008-12-17 15:31:19Z mvw $
package org.tigris.gef.graph;

import java.util.Map;

import org.tigris.gef.base.Layer;
import org.tigris.gef.presentation.FigNode;

/**
 * An interface for FigNode factories. Similiar in concept to the Swing class
 * TreeCellRenderer.
 */
public interface GraphNodeRenderer extends java.io.Serializable {

    /**
     * Factory for a FigNode that can be used to represent the given node
     *
     * @param graphmodel the model in which to place the FigNode
     * @param layer the layer in which to place the FigNode
     * @param node the model element from which to create the FigNode
     * @param attributeMap an optional map of attributes to style the fig.
     * @return the new FigNode
     */
    FigNode getFigNodeFor(GraphModel graphmodel, Layer layer, Object node,
            Map attributeMap);

    /**
     * Factory for a FigNode that can be used to represent the given node
     *
     * @param node the model element from which to create the FigNode
     * @param attributeMap an optional map of attributes to style the fig.
     * return the new FigNode
     */
    FigNode getFigNodeFor(Object node, int x, int y, Map attributeMap);
} /* end interface GraphNodeRenderer */
