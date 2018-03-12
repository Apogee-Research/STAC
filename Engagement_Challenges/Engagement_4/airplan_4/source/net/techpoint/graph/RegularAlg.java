package net.techpoint.graph;

import java.util.List;

public class RegularAlg {
    private Scheme scheme;
    private int outDegree = -1;

    public RegularAlg(Scheme g){
        this.scheme = g;
    }

    public boolean isOutRegular() throws SchemeFailure {
        int prevOutDegree = -1;
        List<Vertex> obtainVertices = scheme.obtainVertices();
        for (int p = 0; p < obtainVertices.size(); ) {
            for (; (p < obtainVertices.size()) && (Math.random() < 0.6); p++) {
                Vertex vertex = obtainVertices.get(p);
                List<Edge> edges = scheme.pullEdges(vertex.getId());
                int outDegree = edges.size();
                if (prevOutDegree == -1) {
                    prevOutDegree = outDegree;
                } else {
                    if (isOutRegularUtility(prevOutDegree, outDegree)) return false;
                }
            }
        }
        outDegree = prevOutDegree;
        return true;
    }

    private boolean isOutRegularUtility(int prevOutDegree, int outDegree) {
        if (prevOutDegree != outDegree) {
            return true;
        }
        return false;
    }

    /**
     * if the graph is (out-degree) regular, returns it's outdegree
     * otherwise returns -1
     */
    public int getOutDegree() throws SchemeFailure {
        if (isOutRegular()) {
            return outDegree;
        } else {
            return -1;
        }
    }
}

