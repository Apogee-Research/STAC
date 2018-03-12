package edu.cyberapex.chart;

public class ChartWriterFactory {
    static public ChartWriter getChartWriter(String type) throws ChartWriterFailure {
        if (type.equals(ChartXmlWriter.TYPE)) {
            return new ChartXmlWriter();
        }
        else if (type.equals(ChartTextWriter.TYPE)) {
            return new ChartTextWriter();
        }
        
        throw new ChartWriterFailure("File type " + type +" is not supported.");
    }
}
