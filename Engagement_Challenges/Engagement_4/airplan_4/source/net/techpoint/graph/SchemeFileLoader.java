package net.techpoint.graph;

import java.io.FileNotFoundException;
import java.util.List;

public interface SchemeFileLoader {
    
    /**
     * Loads a graph from the specified filename
     * @param filename
     * @return
     * @throws FileNotFoundException
     * @throws SchemeFailure
     */
    Scheme loadScheme(String filename) throws FileNotFoundException, SchemeFailure;
    
    /**
     * @return a list of file extensions this loader can handle
     */
    List<String> obtainExtensions();
}
