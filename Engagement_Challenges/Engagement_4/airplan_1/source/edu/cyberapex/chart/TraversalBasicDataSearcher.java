package edu.cyberapex.chart;


/**
 * Searches the nodes for the specified data
 */
public class TraversalBasicDataSearcher {

    public Vertex search(Iterable<Vertex> iter, String key, String value) {
        for (Vertex v : iter) {
            if (v.hasData()) {
                if (searchService(key, value, v)) return v;
            }
        }
        // not found
        return null;
    }

    private boolean searchService(String key, String value, Vertex v) {
        if (new TraversalBasicDataSearcherGuide(key, value, v).invoke()) return true;
        return false;
    }

    private class TraversalBasicDataSearcherGuide {
        private boolean myResult;
        private String key;
        private String value;
        private Vertex v;

        public TraversalBasicDataSearcherGuide(String key, String value, Vertex v) {
            this.key = key;
            this.value = value;
            this.v = v;
        }

        boolean is() {
            return myResult;
        }

        public boolean invoke() {
            Data data = v.getData();
            if (data instanceof BasicData) {
                BasicData basicData = (BasicData) data;
                if (basicData.containsKey(key)) {
                    // lets strip off any whitespace
                    String curValue = basicData.fetch(key).trim();
                    if (curValue.equals(value)) {
                        return true;
                    }
                }
            }
            return false;
        }
    }
}
