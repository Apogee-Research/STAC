package com.networkapex.chart;

import org.apache.commons.io.FilenameUtils;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class GraphLoader {

    private static Map<String, GraphFileLoader> extToLoaderMap = new HashMap<String, GraphFileLoader>();
    
    static {
        TextFileLoader.register();
        XmlFileLoader.register();
        ParserFileLoader.register();
    }
    /**
     * Loads a graph from the specified filename, if the file extension is recognized.
     * @param filename filename of the graph file
     * @return the graph
     * @throws FileNotFoundException
     * @throws GraphRaiser
     */
    public static Graph loadGraph(String filename) throws FileNotFoundException, GraphRaiser {
        // do we have a loader for this filename?
        String extension = FilenameUtils.getExtension(filename);
        if (extToLoaderMap.containsKey(extension)) {
            return extToLoaderMap.get(extension).loadGraph(filename);
        } else {
            throw new GraphRaiser("There is no loader for a file with extension " + extension);
        }
    }
    
    /**
     * Register the file loader with GraphLoader
     * @param loader the loader to register
     */
    public static void registerLoader(GraphFileLoader loader) {
        java.util.List<String> extensions = loader.getExtensions();
        for (int j = 0; j < extensions.size(); j++) {
            registerLoaderAid(loader, extensions, j);
        }
    }

    private static void registerLoaderAid(GraphFileLoader loader, List<String> extensions, int p) {
        String extension = extensions.get(p);
        extToLoaderMap.put(extension, loader);
    }
}
