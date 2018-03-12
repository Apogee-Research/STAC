package edu.cyberapex.chart;

import java.io.FileNotFoundException;
import java.util.List;

public interface ChartFileLoader {
    
    /**
     * Loads a graph from the specified filename
     * @param filename
     * @return
     * @throws FileNotFoundException
     * @throws ChartFailure
     */
    Chart loadChart(String filename) throws FileNotFoundException, ChartFailure;
    
    /**
     * @return a list of file extensions this loader can handle
     */
    List<String> fetchExtensions();
}
