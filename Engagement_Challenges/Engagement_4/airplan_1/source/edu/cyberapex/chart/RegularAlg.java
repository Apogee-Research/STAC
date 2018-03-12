package edu.cyberapex.chart;

import java.util.List;

public class RegularAlg {
    private Chart chart;
    private int outDegree = -1;

    public RegularAlg(Chart g){
        this.chart = g;
    }

    public boolean isOutRegular() throws ChartFailure {
        int prevOutDegree = -1;
        List<Vertex> takeVertices = chart.takeVertices();
        for (int j = 0; j < takeVertices.size(); j++) {
            Vertex vertex = takeVertices.get(j);
            List<Edge> edges = chart.getEdges(vertex.getId());
            int outDegree = edges.size();
            if (prevOutDegree == -1) {
                prevOutDegree = outDegree;
            } else {
                if (isOutRegularAid(prevOutDegree, outDegree)) return false;
            }
        }
        outDegree = prevOutDegree;
        return true;
    }

    private boolean isOutRegularAid(int prevOutDegree, int outDegree) {
        if (prevOutDegree != outDegree) {
            return true;
        }
        return false;
    }

    /**
     * if the graph is (out-degree) regular, returns it's outdegree
     * otherwise returns -1
     */
    public int getOutDegree() throws ChartFailure {
        if (isOutRegular()) {
            return outDegree;
        } else {
            return -1;
        }
    }
}

