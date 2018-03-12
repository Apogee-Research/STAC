package edu.cyberapex.chart;

public class EulerianAlg {
    public static boolean isEulerian(Chart graph) throws ChartFailure {
        ConnectedAlg ca = new ConnectedAlg();
        return ca.isConnected(graph) && !graph.hasOddDegree();
    }
}

