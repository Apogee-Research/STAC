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
// File: MutableGraphModel.java
// Interfaces: MutableGraphModel
// Original Author: jrobbins@ics.uci.edu
// $Id: MutableGraphModel.java 1153 2008-11-30 16:14:45Z bobtarling $
package org.tigris.gef.graph;

import java.util.Hashtable;
import java.util.Map;

/**
 * This interface provides a facade to a net-level representation. Similiar in
 * concept to the Swing class TreeModel.
 * <p>
 *
 * This interface goes beyond GraphModel in that it allows modifications to the
 * graph, instead of just access.
 */
public interface MutableGraphModel extends GraphModel {

    /**
     * Return true if the given object is present as a node in the graph
     */
    boolean containsNode(Object node);

    /**
     * Return true if the given object is a valid node in this graph
     */
    boolean canAddNode(Object node);

    /**
     * Return a valid node in this graph
     */
    Object createNode(String name, Hashtable args);

    /**
     * Return true if the given object is a valid edge in this graph
     */
    boolean canAddEdge(Object edge);

    /**
     * Return true if the type of the given object can be mapped to a supported
     * type.
     */
    boolean canDragNode(Object node);

    /**
     * Remove the given node from the graph. Sends a notification.
     */
    void removeNode(Object node);

    /**
     * Add the given node to the graph, if valid. Sends a notification.
     */
    void addNode(Object node);

    /**
     * Return true if the given object is present as a edge in the graph
     */
    boolean containsEdge(Object edge);

    /**
     * Add the given edge to the graph, if valid. Sends a notification.
     */
    void addEdge(Object edge);

    /**
     * Add existing edges that are related to the node.
     */
    void addNodeRelatedEdges(Object node);

    /**
     * Remove the given edge from the graph. Sends a notification.
     */
    void removeEdge(Object edge);

    /**
     * Create a new node based on the given node and add it to the graph
     */
    void dragNode(Object node);

    /**
     * Return true if the connection to the old node can be rerouted to the new
     * node.
     */
    boolean canChangeConnectedNode(Object newNode, Object oldNode, Object edge);

    /**
     * Return true if the two given ports can be connected by a kind of edge to
     * be determined by the ports.
     */
    boolean canConnect(Object fromP, Object toP);

    /**
     * Return true if the two given ports can be connected by the given kind of
     * edge.
     */
    boolean canConnect(Object fromP, Object toP, Object edgeType);

    /**
     * Reroutes the connection to the old node to be connected to the new node.
     */
    void changeConnectedNode(Object newNode, Object oldNode, Object edge,
            boolean isSource);

    /**
     * Contruct and add a new edge of a kind determined by the ports. Sends a
     * notification.
     */
    Object connect(Object fromPort, Object toPort);

    /**
     * Contruct and add a new edge with given attributes. Sends a notification.
     */
    Object connect(Object fromPort, Object toPort, Class edgeClass);

    /**
     * Contruct and add a new edge with given attributes. Sends a notification.
     */
    Object connect(Object fromPort, Object toPort, Object edgeType);

    /**
     * Contruct and add a new edge with given attributes. Sends a notification.
     */
    Object connect(Object fromPort, Object toPort, Object edgeType,
            Map attributes);

    /**
     * Returns true if handle can be enclosed into encloser.
     */
    boolean isEnclosable(Object handle, Object encloser);

} /* end interface MutableGraphModel */
