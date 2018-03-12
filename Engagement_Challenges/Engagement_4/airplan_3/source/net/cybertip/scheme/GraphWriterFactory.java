package net.cybertip.scheme;

public class GraphWriterFactory {
    static public GraphWriter fetchGraphWriter(String type) throws GraphWriterTrouble {
        if (type.equals(GraphXmlWriter.TYPE)) {
            return new GraphXmlWriter();
        }
        else if (type.equals(GraphTextWriter.TYPE)) {
            return new GraphTextWriter();
        }
        
        throw new GraphWriterTrouble("File type " + type +" is not supported.");
    }
}
