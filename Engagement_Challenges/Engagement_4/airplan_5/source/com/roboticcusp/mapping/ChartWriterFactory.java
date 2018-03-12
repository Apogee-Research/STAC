package com.roboticcusp.mapping;

public class ChartWriterFactory {
    static public ChartWriter fetchChartWriter(String type) throws ChartWriterException {
        if (type.equals(ChartXmlWriter.TYPE)) {
            return new ChartXmlWriter();
        }
        else if (type.equals(ChartTextWriter.TYPE)) {
            return new ChartTextWriter();
        }
        
        throw new ChartWriterException("File type " + type +" is not supported.");
    }
}
