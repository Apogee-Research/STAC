package net.cybertip.scheme;


/**
 * Searches the nodes for the specified data
 */
public class TraversalBasicDataSearcher {

    public Vertex search(Iterable<Vertex> iter, String key, String value) {
        for (Vertex v : iter) {
            if (searchAdviser(key, value, v)) return v;
        }
        // not found
        return null;
    }

    private boolean searchAdviser(String key, String value, Vertex v) {
        if (v.hasData()) {
            if (searchAdviserSupervisor(key, value, v)) return true;
        }
        return false;
    }

    private boolean searchAdviserSupervisor(String key, String value, Vertex v) {
        Data data = v.getData();
        if (data instanceof BasicData) {
            if (searchAdviserSupervisorSupervisor(key, value, (BasicData) data)) return true;
        }
        return false;
    }

    private boolean searchAdviserSupervisorSupervisor(String key, String value, BasicData data) {
        if (new TraversalBasicDataSearcherGuide(key, value, data).invoke()) return true;
        return false;
    }

    private class TraversalBasicDataSearcherGuide {
        private boolean myResult;
        private String key;
        private String value;
        private BasicData data;

        public TraversalBasicDataSearcherGuide(String key, String value, BasicData data) {
            this.key = key;
            this.value = value;
            this.data = data;
        }

        boolean is() {
            return myResult;
        }

        public boolean invoke() {
            Vertex v;BasicData basicData = data;
            if (basicData.containsKey(key)) {
                // lets strip off any whitespace
                if (invokeHelp(basicData)) return true;
            }
            return false;
        }

        private boolean invokeHelp(BasicData basicData) {
            String curValue = basicData.pull(key).trim();
            if (curValue.equals(value)) {
                return true;
            }
            return false;
        }
    }
}
