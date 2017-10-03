/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package user.commands.tomanager;

import graph.Graph;
import graph.Node;
import java.util.Iterator;
import java.util.Map;
import org.tigris.gef.base.Globals;
import org.tigris.gef.base.LayerDiagram;
import org.tigris.gef.presentation.FigCircle;
import user.commands.Cmd;

/**
 *
 * @author user
 */
public class CmdToLayerDiagram implements Cmd {

    @Override
    public Object runCmd(Map inputs) {
        Map<String, Graph> graphs = (Map<String, Graph>) inputs.get("graphs");

        Iterator<Graph> it = graphs.values().iterator();
        while (it.hasNext()) {
            Graph itg = it.next();
            LayerDiagram layerDiagram1 = new LayerDiagram(itg.name);

            Iterator itnodes = itg.nodes.iterator();
            while (itnodes.hasNext()) {
                Node next = (Node) itnodes.next();

                FigCircle fig1 = new FigCircle((int) next.x, (int) next.y, 10, 10);
                fig1.setOwner(next);
                layerDiagram1.add(fig1);
            }
            if (itg.name.equals("main")) {
                Globals.curEditor().getLayerManager().addLayer(layerDiagram1, true);
                Globals.curEditor().getLayerManager().setActiveLayer(layerDiagram1);
            } else {
                Globals.curEditor().getLayerManager().addLayer(layerDiagram1, false);
            }
        }
        return null;
    }

    /*

     */
}
