package net.techpoint.graph;

public class EulerianAlg {
    public static boolean isEulerian(Scheme scheme) throws SchemeFailure {
        ConnectedAlg ca = new ConnectedAlg();
        return ca.isConnected(scheme) && !scheme.hasOddDegree();
    }
}

