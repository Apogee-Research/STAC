package com.roboticcusp.mapping;


/**
 * Searches the nodes for the specified data
 */
public class TraversalBasicDataSearcher {

    public Vertex search(Iterable<Vertex> iter, String key, String value) {
        for (Vertex v : iter) {
            if (v.hasData()) {
                Data data = v.getData();
                if (data instanceof BasicData) {
                    BasicData basicData = (BasicData) data;
                    if (basicData.containsKey(key)) {
                        // lets strip off any whitespace
                        String curValue = basicData.grab(key).trim();
                        if (curValue.equals(value)) {
                            return v;
                        }
                    }
                }
            }
        }
        // not found
        return null;
    }
}
