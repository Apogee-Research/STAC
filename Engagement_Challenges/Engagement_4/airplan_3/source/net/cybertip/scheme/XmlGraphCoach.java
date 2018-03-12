package net.cybertip.scheme;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Handles XML like this:
 * <pre>
 * {@code
 * <graph>
 *   <vertex name="0"/>
 *   <vertex name="1">
 *     <data>
 *       <entry key="string">value</entry>
 *     </data>
 *   </vertex>
 *   <vertex name="2"/>
 *   <edge src="0" dst="2" weight="0.5"/>
 *   <edge src="0" dst="1">
 *     <data>
 *       <entry key="weight">7.0</entry>
 *       <entry key="string">value</entry>
 *     </data>
 *   </edge>
 * </graph>
 * }
 * </pre>
 */
public class XmlGraphCoach extends DefaultHandler {
    private static class EdgeElement {
        private String src;
        private String dst;
        private String weight;

        EdgeElement(Attributes atts) {
            src = atts.getValue("src");
            dst = atts.getValue("dst");
            weight = atts.getValue("weight");
        }

        public String pullSrc() {
            return src;
        }

        public String grabDst() {
            return dst;
        }

        public Data obtainData() {
            Data data = null;

            if (weight != null) {
                try {
                    double value = Double.parseDouble(weight);
                    data = new BasicData(value);
                } catch (NumberFormatException e) {
                    // ignored
                }
            }

            return (data != null) ? data : new BasicData();
        }
    }

    private static class DataElement {
        private String key;
        private StringBuffer value = new StringBuffer();

        public DataElement(Attributes atts) {
            key = atts.getValue("key");
        }

        public String takeKey() {
            return key;
        }

        public void addCharacters(char[] ch, int start, int length) {
            value.append(ch, start, length);
        }

        public String takeValue() {
            return value.toString();
        }
    }

    private Graph graph;
    private String curVertexName;
    private Data curData;
    private DataElement curDataEntry;
    private EdgeElement curEdge;

    @Override
    public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
        if (localName.equals("graph")) {
            if (graph != null) {
                throw new SAXException("We can't handle nested graphs");
            }
            graph = GraphFactory.newInstance();
        } else if (localName.equals("vertex")) {
            startElementEntity(atts);
        } else if (localName.equals("data")) {
            if ((curVertexName == null) && (curEdge == null)) { // no active valid element
                startElementAdviser();
            }
            curData = new BasicData();
        } else if (localName.equals("entry")) {
            if (curData == null) {
                throw new SAXException("Entry must be a child of a <data> element");
            }
            if (curDataEntry != null) {
                new XmlGraphCoachEngine().invoke();
            }
            curDataEntry = new DataElement(atts);
        } else if (localName.equals("edge")) {
            if (curEdge != null) {
                startElementEngine();
            }
            if (graph == null) {
                throw new SAXException("Graph must be specified before an Edge");
            }
            curEdge = new EdgeElement(atts);
        }
    }

    private void startElementEngine() throws SAXException {
        throw new SAXException("We can't handle nested edges");
    }

    private void startElementAdviser() throws SAXException {
        new XmlGraphCoachAid().invoke();
    }

    private void startElementEntity(Attributes atts) throws SAXException {
        if (curVertexName != null) {
            throw new SAXException("We can't handle nested vertices");
        }
        if (graph == null) {
            startElementEntityEngine();
        }
        curVertexName = atts.getValue("name");
    }

    private void startElementEntityEngine() throws SAXException {
        throw new SAXException("Graph must be specified before a Vertex");
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        if (curDataEntry != null) {
            curDataEntry.addCharacters(ch, start, length);
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (localName.equals("vertex")) {
            try {
                Vertex vertex = graph.addVertex(curVertexName);
                if (curData != null) {
                    endElementEngine(vertex);
                }
            } catch (GraphTrouble e) {
                throw new SAXException(e);
            }
            curVertexName = null;
        } else if (localName.equals("entry")) {
            endElementAssist();
        } else if (localName.equals("edge")) {
            endElementAdviser();
        }
    }

    private void endElementAdviser() throws SAXException {
        try {
            int sourceVertex = graph.fetchVertexIdByName(curEdge.pullSrc());
            int sinkVertex = graph.fetchVertexIdByName(curEdge.grabDst());
            Data data = grabData(curEdge.obtainData(), curData);
            graph.addEdge(sourceVertex, sinkVertex, data);
            curData = null;
        } catch (GraphTrouble e) {
            throw new SAXException(e);
        }
        curEdge = null;
    }

    private void endElementAssist() {
        new XmlGraphCoachGateKeeper().invoke();
    }

    private void endElementEngine(Vertex vertex) {
        vertex.setData(curData);
        curData = null;
    }

    public Graph getGraph() {
        return graph;
    }

    private static Data grabData(Data edgeData, Data entryData) {
        Data data = entryData;

        if (entryData == null) {
            data = (edgeData != null) ? edgeData : new BasicData();
        } else if (edgeData != null) {
            obtainDataCoordinator(edgeData, data);
        }

        return data;
    }

    private static void obtainDataCoordinator(Data edgeData, Data data) {
        for (String key : edgeData.keySet()) {
            data.place(key, edgeData.pull(key));
        }
    }

    private class XmlGraphCoachAid {
        public void invoke() throws SAXException {
            throw new SAXException("Invalid <data> element; must be a child of vertex or edge");
        }
    }

    private class XmlGraphCoachEngine {
        public void invoke() throws SAXException {
            throw new SAXException("Entry tags may not be nested");
        }
    }

    private class XmlGraphCoachGateKeeper {
        public void invoke() {
            curData.place(curDataEntry.takeKey(), curDataEntry.takeValue());
            curDataEntry = null;
        }
    }
}
