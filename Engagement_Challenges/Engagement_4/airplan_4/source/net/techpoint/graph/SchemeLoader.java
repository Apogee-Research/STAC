package net.techpoint.graph;

import org.apache.commons.io.FilenameUtils;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;


public class SchemeLoader {

    private static Map<String, SchemeFileLoader> extToLoaderMap = new HashMap<String, SchemeFileLoader>();
    
    static {
        TextFileLoader.register();
        XmlFileLoader.register();
        PartFileLoader.register();
    }
    /**
     * Loads a graph from the specified filename, if the file extension is recognized.
     * @param filename filename of the graph file
     * @return the graph
     * @throws FileNotFoundException
     * @throws SchemeFailure
     */
    public static Scheme loadScheme(String filename) throws FileNotFoundException, SchemeFailure {
        // do we have a loader for this filename?
        String extension = FilenameUtils.getExtension(filename);
        if (extToLoaderMap.containsKey(extension)) {
            return extToLoaderMap.get(extension).loadScheme(filename);
        } else {
            throw new SchemeFailure("There is no loader for a file with extension " + extension);
        }
    }
    
    /**
     * Register the file loader with GraphLoader
     * @param loader the loader to register
     */
    public static void registerLoader(SchemeFileLoader loader) {
        java.util.List<String> extensions = loader.obtainExtensions();
        for (int b = 0; b < extensions.size(); b++) {
            String extension = extensions.get(b);
            extToLoaderMap.put(extension, loader);
        }
    }
}
