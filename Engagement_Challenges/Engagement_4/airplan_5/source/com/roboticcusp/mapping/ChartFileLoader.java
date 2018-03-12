package com.roboticcusp.mapping;

import java.io.FileNotFoundException;
import java.util.List;

public interface ChartFileLoader {
    
    /**
     * Loads a graph from the specified filename
     * @param filename
     * @return
     * @throws FileNotFoundException
     * @throws ChartException
     */
    Chart loadChart(String filename) throws FileNotFoundException, ChartException;
    
    /**
     * @return a list of file extensions this loader can handle
     */
    List<String> obtainExtensions();
}
