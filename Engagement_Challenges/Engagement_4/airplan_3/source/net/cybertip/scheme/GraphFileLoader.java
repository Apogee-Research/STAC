package net.cybertip.scheme;

import java.io.FileNotFoundException;
import java.util.List;

public interface GraphFileLoader {
    
    /**
     * Loads a graph from the specified filename
     * @param filename
     * @return
     * @throws FileNotFoundException
     * @throws GraphTrouble
     */
    Graph loadGraph(String filename) throws FileNotFoundException, GraphTrouble;
    
    /**
     * @return a list of file extensions this loader can handle
     */
    List<String> fetchExtensions();
}
