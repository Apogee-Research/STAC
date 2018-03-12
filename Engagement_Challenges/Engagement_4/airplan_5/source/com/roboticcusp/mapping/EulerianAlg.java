package com.roboticcusp.mapping;

public class EulerianAlg {
    public static boolean isEulerian(Chart graph) throws ChartException {
        ConnectedAlg ca = new ConnectedAlg();
        return ca.isConnected(graph) && !graph.hasOddDegree();
    }
}

