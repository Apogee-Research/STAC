/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package graphviz;

import java.awt.*;
import java.util.*;

/**
 * A simple graph with round nodes and straight edges.
 */
public class SimpleGraph extends Graph {

    public Node[] getNodePrototypes() {
        Node[] nodeTypes
                = {
                    new CircleNode(Color.BLACK),
                    new CircleNode(Color.WHITE)
                };
        return nodeTypes;
    }

    public Edge[] getEdgePrototypes() {
        Edge[] edgeTypes
                = {
                    new LineEdge()
                };
        return edgeTypes;
    }
}
