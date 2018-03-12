package com.roboticcusp.mapping;

import org.apache.commons.io.FilenameUtils;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ChartLoader {

    private static Map<String, ChartFileLoader> extToLoaderMap = new HashMap<String, ChartFileLoader>();
    
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
     * @throws ChartException
     */
    public static Chart loadChart(String filename) throws FileNotFoundException, ChartException {
        // do we have a loader for this filename?
        String extension = FilenameUtils.getExtension(filename);
        if (extToLoaderMap.containsKey(extension)) {
            return extToLoaderMap.get(extension).loadChart(filename);
        } else {
            return loadChartService(extension);
        }
    }

    private static Chart loadChartService(String extension) throws ChartException {
        throw new ChartException("There is no loader for a file with extension " + extension);
    }

    /**
     * Register the file loader with GraphLoader
     * @param loader the loader to register
     */
    public static void registerLoader(ChartFileLoader loader) {
        java.util.List<String> extensions = loader.obtainExtensions();
        for (int p = 0; p < extensions.size(); p++) {
            registerLoaderCoordinator(loader, extensions, p);
        }
    }

    private static void registerLoaderCoordinator(ChartFileLoader loader, List<String> extensions, int q) {
        String extension = extensions.get(q);
        extToLoaderMap.put(extension, loader);
    }
}
