package net.cybertip.scheme;

import java.util.List;

public class RegularAlg {
    private Graph graph;
    private int outDegree = -1;

    public RegularAlg(Graph g){
        this.graph = g;
    }

    public boolean isOutRegular() throws GraphTrouble {
        int prevOutDegree = -1;
        List<Vertex> grabVertices = graph.grabVertices();
        for (int j = 0; j < grabVertices.size(); j++) {
            Vertex vertex = grabVertices.get(j);
            List<Edge> edges = graph.fetchEdges(vertex.getId());
            int outDegree = edges.size();
            if (prevOutDegree == -1) {
                prevOutDegree = outDegree;
            } else {
                if (isOutRegularEntity(prevOutDegree, outDegree)) return false;
            }
        }
        outDegree = prevOutDegree;
        return true;
    }

    private boolean isOutRegularEntity(int prevOutDegree, int outDegree) {
        if (prevOutDegree != outDegree) {
            return true;
        }
        return false;
    }

    /**
     * if the graph is (out-degree) regular, returns it's outdegree
     * otherwise returns -1
     */
    public int grabOutDegree() throws GraphTrouble {
        if (isOutRegular()) {
            return outDegree;
        } else {
            return -1;
        }
    }
}

