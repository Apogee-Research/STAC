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
// File: GraphEdgeRenderer.java
// Interfaces: GraphEdgeRenderer
// Original Author: jrobbins@ics.uci.edu
// $Id: GraphEdgeRenderer.java 1153 2008-11-30 16:14:45Z bobtarling $
package org.tigris.gef.graph;

import java.util.Map;

import org.tigris.gef.base.Layer;
import org.tigris.gef.presentation.FigEdge;

/**
 * An interface for FigEdge factories. Similiar in concept to the Swing class
 * TreeCellRenderer.
 */
public interface GraphEdgeRenderer extends java.io.Serializable {

    /**
     * Factory for a FigEdge that can be used to represent the given edge
     *
     * @param graphmodel the model in which to place the FigEdge
     * @param layer the layer in which to place the FigEdge
     * @param edge the model element from which to create the FigEdge
     * @param attributeMap an optional map of attributes to style the fig.
     * return the new FigEdge
     */
    FigEdge getFigEdgeFor(GraphModel graphmodel, Layer layer, Object edge,
            Map attributeMap);

    /**
     * Factory for a FigEdge that can be used to represent the given edge
     *
     * @param edge the model element from which to create the FigEdge
     * @param attributeMap an optional map of attributes to style the fig.
     * return the new FigEdge
     */
    FigEdge getFigEdgeFor(Object edge, Map attributeMap);
} /* end interface GraphEdgeRenderer */
