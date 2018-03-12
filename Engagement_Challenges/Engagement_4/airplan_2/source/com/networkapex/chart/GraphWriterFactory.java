package com.networkapex.chart;

public class GraphWriterFactory {
    static public GraphWriter pullGraphWriter(String type) throws GraphWriterRaiser {
        if (type.equals(GraphXmlWriter.TYPE)) {
            return new GraphXmlWriter();
        }
        else if (type.equals(GraphTextWriter.TYPE)) {
            return new GraphTextWriter();
        }
        
        throw new GraphWriterRaiser("File type " + type +" is not supported.");
    }
}
