package com.roboticcusp.mapping;

import java.util.List;

public class RegularAlg {
    private Chart chart;
    private int outDegree = -1;

    public RegularAlg(Chart g){
        this.chart = g;
    }

    public boolean isOutRegular() throws ChartException {
        int prevOutDegree = -1;
        List<Vertex> obtainVertices = chart.obtainVertices();
        for (int p = 0; p < obtainVertices.size(); p++) {
            Vertex vertex = obtainVertices.get(p);
            List<Edge> edges = chart.getEdges(vertex.getId());
            int outDegree = edges.size();
            if (prevOutDegree == -1) {
                prevOutDegree = outDegree;
            } else {
                if (prevOutDegree != outDegree) {
                    return false;
                }
            }
        }
        outDegree = prevOutDegree;
        return true;
    }

    /**
     * if the graph is (out-degree) regular, returns it's outdegree
     * otherwise returns -1
     */
    public int pullOutDegree() throws ChartException {
        if (isOutRegular()) {
            return outDegree;
        } else {
            return -1;
        }
    }
}

