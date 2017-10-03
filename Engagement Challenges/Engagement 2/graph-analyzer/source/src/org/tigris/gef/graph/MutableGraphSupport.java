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
// File: DefaultGraphModel.java
// Interfaces: DefaultGraphModel
// Original Author: jrobbins@ics.uci.edu
// $Id: MutableGraphSupport.java 1153 2008-11-30 16:14:45Z bobtarling $
package org.tigris.gef.graph;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.Action;

import org.tigris.gef.di.GraphElement;
import org.tigris.gef.presentation.Fig;

/**
 * An abstract class that makes it easier to implement your own version of
 * MutableGraphModel. This class basically includes the code for event
 * notifications, so that you don't have to write that. It also provides a few
 * utility methods.
 */
public abstract class MutableGraphSupport implements MutableGraphModel,
        java.io.Serializable {

    private Vector _graphListeners;

    private ConnectionConstrainer connectionConstrainer;

    private static Action saveAction;

    public MutableGraphSupport() {
    }

    public MutableGraphSupport(ConnectionConstrainer cc) {
        connectionConstrainer = cc;
    }

    public List getGraphListeners() {
        return _graphListeners;
    }

    // //////////////////////////////////////////////////////////////
    // MutableGraphModel implementation
    /**
     * Return a valid node in this graph TODO Should throw a GraphModelException
     * or InvalidArgumentException
     */
    public Object createNode(String name, Hashtable args) {
        Object newNode;
        try {
            newNode = Class.forName(name).newInstance();
        } catch (java.lang.ClassNotFoundException ignore) {
            return null;
        } catch (java.lang.IllegalAccessException ignore) {
            return null;
        } catch (java.lang.InstantiationException ignore) {
            return null;
        }

        if (newNode instanceof GraphNodeHooks) {
            ((GraphNodeHooks) newNode).initialize(args);
        }
        return newNode;
    }

    protected ConnectionConstrainer getConnectionConstrainer() {
        return connectionConstrainer;
    }

    /**
     * Apply the object containing the ruleset for what edges and ports can
     * connect in the graph
     */
    public void setConnectionConstrainer(ConnectionConstrainer cc) {
        connectionConstrainer = cc;
    }

    /**
     * Return true if the type of the given node can be mapped to a type
     * supported by this type of diagram
     */
    public boolean canDragNode(Object node) {
        return false;
    }

    /**
     * Create a new node based on the given one and add it to the graph.
     */
    public void dragNode(Object node) {
    }

    /**
     * Return true if the connection to the old node can be rerouted to the new
     * node.
     */
    public boolean canChangeConnectedNode(Object newNode, Object oldNode,
            Object edge) {
        return false;
    }

    /**
     * Determine if the two given ports can be connected by the given kind of
     * edge. This delegates either to the registered ConnectionConstrainer or if
     * unregistered then ignores edgeClass and calls canConnect(port,port).
     *
     * @param fromPort the source port for which to test
     * @param toPort the destination port for which to test
     * @param edgeType An identifier indicating the type of edge to create
     */
    public boolean canConnect(Object fromPort, Object toPort, Object edgeType) {
        boolean canConnect = false;
        if (connectionConstrainer != null) {
            canConnect = connectionConstrainer.isConnectionValid(edgeType,
                    fromPort, toPort);
        } else {
            canConnect = canConnect(fromPort, toPort);
        }
        return canConnect;
    }

    /**
     * Determine if the two given ports can be connected by the given kind of
     * edge. This delegates either to the registered ConnectionConstrainer or if
     * unregistered then ignores edgeClass and calls canConnect(port,port).
     *
     * @param fromPort the source port for which to test
     * @param toPort the destination port for which to test
     * @param edgeClass The edge class for which test
     */
    public boolean canConnect(Object fromPort, Object toPort, Class edgeClass) {
        boolean canConnect = false;
        if (connectionConstrainer != null) {
            canConnect = connectionConstrainer.isConnectionValid(edgeClass,
                    fromPort, toPort);
        } else {
            canConnect = canConnect(fromPort, toPort);
        }
        return canConnect;
    }

    /**
     * Reroutes the connection to the old node to be connected to the new node.
     */
    public void changeConnectedNode(Object newNode, Object oldNode,
            Object edge, boolean isSource) {
    }

    /**
     * Contruct and add a new edge of the given kind. By default ignore
     * edgeClass and call connect(port,port).
     */
    public Object connect(Object fromPort, Object toPort, Object edgeClass) {
        return connect(fromPort, toPort);
    }

    /**
     * Construct and add a new edge of the given kind and connect the given
     * ports. By default ignore edgeClass and call connect(port,port).
     *
     * @param fromPort The originating port to connect
     *
     * @param toPort The destination port to connect
     *
     * @param edgeType Some indicator of the type of edge to create. This could
     * be a <code>Class</code> or some other value as input to a factory to
     * create the required edge model.
     *
     * @param styleAttributes key/value pairs from which to create the edge.
     *
     * @return The type of edge created (the same as <code>edgeClass</code> if
     * we succeeded, <code>null</code> otherwise)
     */
    public Object connect(Object fromPort, Object toPort, Object edgeType,
            Map styleAttributes) {
        return connect(fromPort, toPort);
    }

    // //////////////////////////////////////////////////////////////
    // utility methods
    public boolean containsNode(Object node) {
        List nodes = getNodes();
        return nodes.contains(node);
    }

    public boolean containsEdge(Object edge) {
        List edges = getEdges();
        return edges.contains(edge);
    }

    public boolean containsNodePort(Object port) {
        List nodes = getNodes();
        if (nodes == null) {
            return false;
        }
        for (int i = 0; i < nodes.size(); ++i) {
            List ports = getPorts(nodes.get(i));
            if (ports != null && ports.contains(port)) {
                return true;
            }
        }
        return false;
    }

    public boolean containsEdgePort(Object port) {
        List edges = getNodes();
        if (edges == null) {
            return false;
        }
        for (int i = 0; i < edges.size(); ++i) {
            List ports = getPorts(edges.get(i));
            if (ports != null && ports.contains(port)) {
                return true;
            }
        }
        return false;
    }

    public boolean containsPort(Object port) {
        return containsNodePort(port) || containsEdgePort(port);
    }

    // //////////////////////////////////////////////////////////////
    // listener registration
    public void addGraphEventListener(GraphListener listener) {
        if (_graphListeners == null) {
            _graphListeners = new Vector();
        }
        _graphListeners.addElement(listener);
    }

    public void removeGraphEventListener(GraphListener listener) {
        if (_graphListeners == null) {
            return;
        }
        _graphListeners.removeElement(listener);
    }

    // //////////////////////////////////////////////////////////////
    // event notifications
    public void fireNodeAdded(Object node) {
        if (saveAction != null && !saveAction.isEnabled()) {
            saveAction.setEnabled(true);
        }
        if (_graphListeners == null) {
            return;
        }
        GraphEvent ge = new GraphEvent(this, node);
        Enumeration listeners = _graphListeners.elements();
        while (listeners.hasMoreElements()) {
            GraphListener listen = (GraphListener) listeners.nextElement();
            listen.nodeAdded(ge);
        }
    }

    public void fireNodeRemoved(Object node) {
        if (saveAction != null && !saveAction.isEnabled()) {
            saveAction.setEnabled(true);
        }
        if (_graphListeners == null) {
            return;
        }
        GraphEvent ge = new GraphEvent(this, node);
        Enumeration listeners = _graphListeners.elements();
        while (listeners.hasMoreElements()) {
            GraphListener listen = (GraphListener) listeners.nextElement();
            listen.nodeRemoved(ge);
        }
    }

    public void fireEdgeAdded(Object edge) {
        if (saveAction != null && !saveAction.isEnabled()) {
            saveAction.setEnabled(true);
        }
        if (_graphListeners == null) {
            return;
        }
        GraphEvent ge = new GraphEvent(this, edge);
        Enumeration listeners = _graphListeners.elements();
        while (listeners.hasMoreElements()) {
            GraphListener listen = (GraphListener) listeners.nextElement();
            listen.edgeAdded(ge);
        }
    }

    public void fireEdgeRemoved(Object edge) {
        if (saveAction != null && !saveAction.isEnabled()) {
            saveAction.setEnabled(true);
        }
        if (_graphListeners == null) {
            return;
        }
        GraphEvent ge = new GraphEvent(this, edge);
        Enumeration listeners = _graphListeners.elements();
        while (listeners.hasMoreElements()) {
            GraphListener listen = (GraphListener) listeners.nextElement();
            listen.edgeRemoved(ge);
        }
    }

    public void fireGraphChanged() {
        if (saveAction != null && !saveAction.isEnabled()) {
            saveAction.setEnabled(true);
        }
        if (_graphListeners == null) {
            return;
        }
        GraphEvent ge = new GraphEvent(this, null);
        Enumeration listeners = _graphListeners.elements();
        while (listeners.hasMoreElements()) {
            GraphListener listen = (GraphListener) listeners.nextElement();
            listen.graphChanged(ge);
        }
    }

    public static void setSaveAction(Action action) {
        saveAction = action;
    }

    public static void enableSaveAction() {
        if (saveAction != null) {
            saveAction.setEnabled(true);
        }
    }

    public void removeNode(Object node) {
        fireNodeRemoved(node);
    }

    /**
     * Add the given node to the graph, if valid.
     */
    public void addNode(Object node) {
        fireNodeAdded(node);
    }

    /**
     * Add the given edge to the graph, if valid.
     */
    public void addEdge(Object edge) {
        fireEdgeAdded(edge);
    }

    /**
     * Remove the given edge from the graph.
     */
    public void removeEdge(Object edge) {
        fireEdgeRemoved(edge);
    }

    /**
     * Remove the given edge from the graph.
     */
    public void removeFig(Fig fig) {
        if (fig instanceof GraphElement) {
            throw new IllegalArgumentException(
                    "Use removeEdge or removeNode to remove a complex Fig");
        }
        fig.removeFromDiagram();
        fireGraphChanged();
    }

    /**
     * Remove all the nodes from the graph.
     */
    public void removeAllNodes() {
        fireGraphChanged();
    }

    /**
     * Remove all the edges from the graph.
     */
    public void removeAllEdges() {
        fireGraphChanged();
    }

    /**
     * Remove all nodes and edges to reset the graph.
     */
    public void removeAll() {
        fireGraphChanged();
    }

    /**
     * Returns true if handle can be enclosed into encloser.
     */
    public boolean isEnclosable(Object handle, Object encloser) {
        return true;
    }

} /* end class MutableGraphSupport */
