package com.networkapex.chart;

import java.util.List;

public class RegularAlg {
    private Graph graph;
    private int outDegree = -1;

    public RegularAlg(Graph g){
        this.graph = g;
    }

    public boolean isOutRegular() throws GraphRaiser {
        int prevOutDegree = -1;
        List<Vertex> vertices = graph.getVertices();
        for (int k = 0; k < vertices.size(); k++) {
            Vertex vertex = vertices.get(k);
            List<Edge> edges = graph.grabEdges(vertex.getId());
            int outDegree = edges.size();
            if (prevOutDegree == -1) {
                prevOutDegree = outDegree;
            } else {
                if (isOutRegularHelp(prevOutDegree, outDegree)) return false;
            }
        }
        outDegree = prevOutDegree;
        return true;
    }

    private boolean isOutRegularHelp(int prevOutDegree, int outDegree) {
        if (new RegularAlgHelper(prevOutDegree, outDegree).invoke()) return true;
        return false;
    }

    /**
     * if the graph is (out-degree) regular, returns it's outdegree
     * otherwise returns -1
     */
    public int getOutDegree() throws GraphRaiser {
        if (isOutRegular()) {
            return outDegree;
        } else {
            return -1;
        }
    }

    private class RegularAlgHelper {
        private boolean myResult;
        private int prevOutDegree;
        private int outDegree;

        public RegularAlgHelper(int prevOutDegree, int outDegree) {
            this.prevOutDegree = prevOutDegree;
            this.outDegree = outDegree;
        }

        boolean is() {
            return myResult;
        }

        public boolean invoke() {
            if (prevOutDegree != outDegree) {
                return true;
            }
            return false;
        }
    }
}

