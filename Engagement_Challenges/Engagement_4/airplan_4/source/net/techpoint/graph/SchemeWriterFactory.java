package net.techpoint.graph;

public class SchemeWriterFactory {
    static public SchemeWriter obtainSchemeWriter(String type) throws SchemeWriterFailure {
        if (type.equals(SchemeXmlWriter.TYPE)) {
            return new SchemeXmlWriter();
        }
        else if (type.equals(SchemeTextWriter.TYPE)) {
            return new SchemeTextWriter();
        }
        
        throw new SchemeWriterFailure("File type " + type +" is not supported.");
    }
}
