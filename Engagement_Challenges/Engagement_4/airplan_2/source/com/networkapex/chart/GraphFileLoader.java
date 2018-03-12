package com.networkapex.chart;

import java.io.FileNotFoundException;
import java.util.List;

public interface GraphFileLoader {
    
    /**
     * Loads a graph from the specified filename
     * @param filename
     * @return
     * @throws FileNotFoundException
     * @throws GraphRaiser
     */
    Graph loadGraph(String filename) throws FileNotFoundException, GraphRaiser;
    
    /**
     * @return a list of file extensions this loader can handle
     */
    List<String> getExtensions();
}
