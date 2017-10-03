/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package user.commands.layout;

import graph.Edge;
import graph.Graph;
import graph.Node;
import graph.layout.adv.LinLogLayout;
import graph.layout.adv.MinimizerBarnesHut;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import user.commands.Cmd;

/**
 *
 * @author user
 */
public class CmdForceLayout implements Cmd {

    public CmdForceLayout() {

    }

    @Override
    public Object runCmd(Map inputs) {
        Map<String, Graph> graphs = (Map<String, Graph>) inputs.get("graphs");

        Iterator<Graph> itgs = graphs.values().iterator();
        while (itgs.hasNext()) {
            Graph itg = itgs.next();
            java.util.List<Node> nodes = new ArrayList<Node>();
            java.util.List<Edge> edges = new ArrayList<Edge>();
            Iterator it = itg.nodes.iterator();
            while (it.hasNext()) {
                Node n = (Node) it.next();
                nodes.add(n);
                Iterator<Edge> ite = n.out.iterator();
                while (ite.hasNext()) {
                    Edge e = ite.next();
                    edges.add(e);
                }
            }
        //Map<Node, double[]> nodeToPosition = new HashMap<Node, double[]> ();//

            Map<Node, double[]> nodeToPosition = LinLogLayout.makeInitialPositions(nodes, false);

            new MinimizerBarnesHut(nodes, edges, 0.0, 1.0, 0.05).minimizeEnergy(nodeToPosition, 100);
            org.graph.commons.logging.LogFactory.getLog(null).info("layout");
        }
        return null;
    }

}
