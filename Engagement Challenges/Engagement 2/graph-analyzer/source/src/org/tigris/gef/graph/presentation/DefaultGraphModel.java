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
// $Id: DefaultGraphModel.java 1153 2008-11-30 16:14:45Z bobtarling $
package org.tigris.gef.graph.presentation;

import java.util.Collection;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import org.graph.commons.logging.Log;
import org.graph.commons.logging.LogFactory;
import org.tigris.gef.graph.ConnectionConstrainer;
import org.tigris.gef.graph.GraphNodeHooks;
import org.tigris.gef.graph.MutableGraphSupport;

/**
 * This interface provides a facade to a net-level representation. Similiar in
 * concept to the Swing class TreeModel. This implementation of GraphModel uses
 * the GEF classes NetList, NetNode, NetPort, and NetEdge. If you implement your
 * own GraphModel, you can use your own application-specific classes.
 *
 * @see NetList
 * @see NetNode
 * @see NetPort
 * @see NetEdge
 * @see AdjacencyListGraphModel
 */
public class DefaultGraphModel extends MutableGraphSupport implements
        java.io.Serializable {

    private static final long serialVersionUID = 8098329898758384131L;

    private NetList netList;

    private static Log LOG = LogFactory.getLog(DefaultGraphModel.class);

    // //////////////////////////////////////////////////////////////
    // constructors
    public DefaultGraphModel() {
        netList = new NetList();
    }

    public DefaultGraphModel(ConnectionConstrainer cc) {
        super(cc);
        netList = new NetList();
    }

    public DefaultGraphModel(NetList nl) {
        netList = nl;
    }

    /**
     * Return all nodes in the graph
     */
    public List getNodes() {
        return netList.getNodes();
    }

    /**
     * Return all edges in the graph
     */
    public List getEdges() {
        return netList.getEdges();
    }

    /**
     * Return all ports on node or edge
     */
    public List getPorts(Object nodeOrEdge) {
        if (nodeOrEdge instanceof NetNode) {
            return ((NetNode) nodeOrEdge).getPorts();
        }
        if (nodeOrEdge instanceof NetEdge) {
            return ((NetEdge) nodeOrEdge).getPorts();
        }
        return null; // raise exception
    }

    /**
     * Return all nodes in the graph
     */
    public Collection getNodes(Collection c) {
        return netList.getNodes(c);
    }

    /**
     * Return all nodes in the graph
     */
    public Collection getEdges(Collection c) {
        return netList.getEdges(c);
    }

    /**
     * Return all ports on node or edge
     */
    public Collection getPorts(Collection c, Object nodeOrEdge) {
        if (nodeOrEdge instanceof NetNode) {
            return ((NetNode) nodeOrEdge).getPorts();
        }
        if (nodeOrEdge instanceof NetEdge) {
            return ((NetEdge) nodeOrEdge).getPorts();
        }
        return null; // raise exception
    }

    /**
     * Return the node or edge that owns the given port
     */
    public Object getOwner(Object port) {
        if (port instanceof NetPort) {
            return ((NetPort) port).getParent();
        }
        return null; // raise exception
    }

    /**
     * Return all edges going to given port
     */
    public List getInEdges(Object port) {
        Vector res = new Vector();
        Vector edge = ((NetPort) port).getEdges();
        for (int i = 0; i < edge.size(); i++) {
            NetEdge ne = (NetEdge) edge.elementAt(i);
            if (ne.getDestPort() == port) {
                res.add(ne);
            }
        }
        return res;
    }

    /**
     * Return all edges going from given port
     */
    public List getOutEdges(Object port) {
        Vector res = new Vector();
        Vector edge = ((NetPort) port).getEdges();
        for (int i = 0; i < edge.size(); i++) {
            NetEdge ne = (NetEdge) edge.elementAt(i);
            if (ne.getSourcePort() == port) {
                res.add(ne);
            }
        }
        return res;
    }

    /**
     * Return one end of an edge
     */
    public Object getSourcePort(Object edge) {
        if (edge instanceof NetEdge) {
            return ((NetEdge) edge).getSourcePort();
        }
        return null; // raise exception
    }

    /**
     * Return the other end of an edge
     */
    public Object getDestPort(Object edge) {
        if (edge instanceof NetEdge) {
            return ((NetEdge) edge).getDestPort();
        }
        return null; // raise exception
    }

    // //////////////////////////////////////////////////////////////
    // interface MutableGraphListener
    /**
     * Return a valid node in this graph
     */
    public Object createNode(String name, Hashtable args) {
        Object newNode;
        // Class nodeClass = (Class) getArg("className", DEFAULT_NODE_CLASS);
        // assert _nodeClass != null
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

    /**
     * Return true if the given object is a valid node in this graph
     */
    public boolean canAddNode(Object node) {
        return (node instanceof NetNode);
    }

    /**
     * Return true if the given object is a valid edge in this graph
     */
    public boolean canAddEdge(Object edge) {
        return (edge instanceof NetEdge);
    }

    /**
     * Remove the given node from the graph.
     */
    public void removeNode(Object node) {
        NetNode n = (NetNode) node;
        netList.removeNode(n);
        LOG.debug("Removed node from graph model");
        super.removeNode(node);
    }

    /**
     * Return true if dragging the given object is a valid in this graph
     */
    public boolean canDragNode(Object node) {
        return (node instanceof NetNode);
    }

    /**
     * Add the given node to the graph, if valid.
     */
    public void addNode(Object node) {
        NetNode n = (NetNode) node;
        netList.addNode(n);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Added a node. There are now "
                    + netList.getNodes(null).size() + " edges");
        }
        super.addNode(node);
    }

    /**
     * Add the given edge to the graph, if valid.
     */
    public void addEdge(Object edge) {
        NetEdge e = (NetEdge) edge;
        netList.addEdge(e);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Added an edge. There are now "
                    + netList.getEdges(null).size() + " edges");
        }
        super.addEdge(e);
    }

    public void addNodeRelatedEdges(Object node) {
    }

    /**
     * Remove the given edge from the graph.
     */
    public void removeEdge(Object edge) {
        LOG.debug("DefaultGraphModel::removeEdge");
        NetEdge e = (NetEdge) edge;
        netList.removeEdge(e);
        super.removeEdge(e);
    }

    /**
     * Remove all the nodes from the graph.
     */
    public void removeAllNodes() {
        LOG.debug("Removing all the nodes from the graph.");
        netList.removeAllNodes();
        super.removeAllNodes();
    }

    /**
     * Remove all the edges from the graph.
     */
    public void removeAllEdges() {
        LOG.debug("Removing all the edges from the graph.");
        netList.removeAllEdges();
        super.removeAllEdges();
    }

    /**
     * Remove all nodes and edges to reset the graph.
     */
    public void removeAll() {
        netList.removeAllEdges();
        netList.removeAllNodes();
        super.removeAll();
    }

    public void dragNode(Object node) {
        addNode(node);
    }

    /**
     * Return true if the two given ports can be connected by a kind of edge to
     * be determined by the ports.
     */
    public boolean canConnect(Object srcPort, Object destPort) {
        if (srcPort instanceof NetPort && destPort instanceof NetPort) {
            NetPort s = (NetPort) srcPort;
            NetPort d = (NetPort) destPort;
            if (LOG.isDebugEnabled()) {
                LOG.debug("Checking with ports to see if connection valid");
            }
            return s.canConnectTo(this, d) && d.canConnectTo(this, s);
        } else {
            if (LOG.isDebugEnabled()) {
                LOG.debug("By default, cannot connect non-NetPort objects");
            }
            return false;
        }
    }

    /**
     * Contruct and add a new edge of a kind determined by the ports
     */
    public Object connect(Object srcPort, Object destPort) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Attempting to connect " + srcPort + " to " + destPort);
        }
        if (!canConnect(srcPort, destPort)) {
            LOG.warn("Connection not allowed");
            return null;
        }
        if (srcPort instanceof NetPort && destPort instanceof NetPort) {
            NetPort s = (NetPort) srcPort;
            NetPort d = (NetPort) destPort;
            // org.graph.commons.logging.LogFactory.getLog(null).info("calling makeEdgeFor:" +
            // s.getClass().getName());
            NetEdge e = s.makeEdgeFor(d);
            return connectInternal(s, d, e);
        } else {
            return null;
        }
    }

    /**
     * Contruct and add a new edge of the given kind The default is to assume
     * the edge type is a Class.
     */
    public Object connect(Object srcPort, Object destPort, Object edgeType) {
        return connect(srcPort, destPort, (Class) edgeType);
    }

    /**
     * Contruct and add a new edge of the given kind
     */
    public Object connect(Object srcPort, Object destPort, Class edgeClass) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Attempting to connect " + srcPort + " to " + destPort
                    + " with " + edgeClass);
        }
        if (!canConnect(srcPort, destPort, edgeClass)) {
            LOG.warn("Connection not allowed");
            return null;
        }
        if (srcPort instanceof NetPort && destPort instanceof NetPort) {
            NetPort s = (NetPort) srcPort;
            NetPort d = (NetPort) destPort;
            try {
                NetEdge e = (NetEdge) edgeClass.newInstance();
                return connectInternal(s, d, e);
            } catch (java.lang.InstantiationException e) {
            } catch (java.lang.IllegalAccessException e) {
            }
        }
        return null;
    }

    /**
     * Asks the given edge to attempt to connect itself to the given ports.
     *
     * @param s source port
     * @param d destination port
     * @param e edge
     * @return the edge or null if the edge rejects the connection.
     */
    protected Object connectInternal(NetPort s, NetPort d, NetEdge e) {
        // org.graph.commons.logging.LogFactory.getLog(null).info("connectInternal");
        if (e.connect(this, s, d)) {
            addEdge(e);
            return e;
        } else {
            return null;
        }
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
     * Reroutes the connection to the old node to be connected to the new node.
     */
    public void changeConnectedNode(Object newNode, Object oldNode,
            Object edge, boolean isSource) {
    }
} /* end class DefaultGraphModel */
