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
/**
 * LayerPerspectiveMutable.java
 *
 * A LayerPerspective with an underlying MutableGraphModel. As figures are added
 * and removed the underlying MutableGraphModel is updated.
 *
 * @author Eugenio Alvarez Data Access Technologies
 *
 */
package org.tigris.gef.base;

import org.tigris.gef.graph.*;
import org.tigris.gef.presentation.*;

public class LayerPerspectiveMutable extends LayerPerspective {

    private static final long serialVersionUID = 4692683431762315041L;

    /**
     * The underlying connected graph to be visualized.
     */
    private MutableGraphModel mutableGraphModel;

    // //////////////////////////////////////////////////////////////
    // constructors
    public LayerPerspectiveMutable(String name, MutableGraphModel mgm) {
        super(name, (GraphModel) mgm);
        mutableGraphModel = mgm;
    }

    // //////////////////////////////////////////////////////////////
    // accessors
    public GraphModel getGraphModel() {
        return (GraphModel) getMutableGraphModel();
    }

    public void setGraphModel(GraphModel gm) {
        setMutableGraphModel((MutableGraphModel) gm);
    }

    public MutableGraphModel getMutableGraphModel() {
        return mutableGraphModel;
    }

    public void setMutableGraphModel(MutableGraphModel mgm) {
        super.setGraphModel((GraphModel) mgm);
        mutableGraphModel = mgm;
    }

    // //////////////////////////////////////////////////////////////
    // Layer API
    public void add(Fig fig) {
        Object owner = fig.getOwner();
        // prevents duplicate nodes.
        // To allow multiple views in one diagram, remove the following two
        // lines.
        if (fig instanceof FigNode && contains(fig)
                && mutableGraphModel.containsNode(owner)
                && fig.getLayer() == this) {
            // When a new node is created (using
            // GraphModelEvents), the node is first
            // added to the MutableGraphModel, then
            // added to the layer (here). Thus at
            // this point, _mgm.contains(owner),
            // but !_contents.contains(f), so we
            // have to add the Fig f to the layer.
            // Only if both the model and the graph
            // contain the node/the fig, we can
            // return without doing anything.
            // Added by oliver@freiheit.com
            return;
        }
        super.add(fig);
        // if ( owner != null && _mgm.canAddNode(owner))
        // _mgm.addNode(owner);
        // FigEdges are added by the underlying MutableGraphModel.
    }

    public void remove(Fig f) {
        super.remove(f);
        Object owner = f.getOwner();
        if (owner != null) {
            if (f instanceof FigEdge && mutableGraphModel.containsEdge(owner)) {
                mutableGraphModel.removeEdge(owner);
            } else if (f instanceof FigNode
                    && mutableGraphModel.containsNode(owner)) {
                mutableGraphModel.removeNode(owner);
            }
        }
    }
} /* end class LayerPerspectiveMutable */
