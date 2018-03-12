package com.networkapex.chart;


/**
 * Searches the nodes for the specified data
 */
public class TraversalBasicDataSearcher {

    public Vertex search(Iterable<Vertex> iter, String key, String value) {
        for (Vertex v : iter) {
            if (searchEntity(key, value, v)) return v;
        }
        // not found
        return null;
    }

    private boolean searchEntity(String key, String value, Vertex v) {
        if (v.hasData()) {
            if (searchEntityEntity(key, value, v)) return true;
        }
        return false;
    }

    private boolean searchEntityEntity(String key, String value, Vertex v) {
        Data data = v.getData();
        if (data instanceof BasicData) {
            BasicData basicData = (BasicData) data;
            if (basicData.containsKey(key)) {
                // lets strip off any whitespace
                String curValue = basicData.pull(key).trim();
                if (curValue.equals(value)) {
                    return true;
                }
            }
        }
        return false;
    }
}
