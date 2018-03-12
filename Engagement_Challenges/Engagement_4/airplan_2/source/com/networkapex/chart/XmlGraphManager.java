package com.networkapex.chart;

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
public class XmlGraphManager extends DefaultHandler {
    private static class EdgeElement {
        private String src;
        private String dst;
        private String weight;

        EdgeElement(Attributes atts) {
            src = atts.getValue("src");
            dst = atts.getValue("dst");
            weight = atts.getValue("weight");
        }

        public String takeSrc() {
            return src;
        }

        public String getDst() {
            return dst;
        }

        public Data grabData() {
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

        public String grabKey() {
            return key;
        }

        public void addCharacters(char[] ch, int start, int length) {
            value.append(ch, start, length);
        }

        public String getValue() {
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
                startElementHelp();
            }
            graph = GraphFactory.newInstance();
        } else if (localName.equals("vertex")) {
            if (curVertexName != null) {
                startElementHerder();
            }
            if (graph == null) {
                throw new SAXException("Graph must be specified before a Vertex");
            }
            curVertexName = atts.getValue("name");
        } else if (localName.equals("data")) {
            startElementGuide();
        } else if (localName.equals("entry")) {
            startElementEntity(atts);
        } else if (localName.equals("edge")) {
            startElementService(atts);
        }
    }

    private void startElementService(Attributes atts) throws SAXException {
        if (curEdge != null) {
            startElementServiceHerder();
        }
        if (graph == null) {
            throw new SAXException("Graph must be specified before an Edge");
        }
        curEdge = new EdgeElement(atts);
    }

    private void startElementServiceHerder() throws SAXException {
        new XmlGraphManagerHelper().invoke();
    }

    private void startElementEntity(Attributes atts) throws SAXException {
        if (curData == null) {
            startElementEntityGuide();
        }
        if (curDataEntry != null) {
            startElementEntityHome();
        }
        curDataEntry = new DataElement(atts);
    }

    private void startElementEntityHome() throws SAXException {
        throw new SAXException("Entry tags may not be nested");
    }

    private void startElementEntityGuide() throws SAXException {
        throw new SAXException("Entry must be a child of a <data> element");
    }

    private void startElementGuide() throws SAXException {
        if ((curVertexName == null) && (curEdge == null)) { // no active valid element
            startElementGuideSupervisor();
        }
        curData = new BasicData();
    }

    private void startElementGuideSupervisor() throws SAXException {
        throw new SAXException("Invalid <data> element; must be a child of vertex or edge");
    }

    private void startElementHerder() throws SAXException {
        throw new SAXException("We can't handle nested vertices");
    }

    private void startElementHelp() throws SAXException {
        throw new SAXException("We can't handle nested graphs");
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
            endElementExecutor();
        } else if (localName.equals("entry")) {
            endElementHome();
        } else if (localName.equals("edge")) {
            try {
                int sourceVertex = graph.takeVertexIdByName(curEdge.takeSrc());
                int sinkVertex = graph.takeVertexIdByName(curEdge.getDst());
                Data data = pullData(curEdge.grabData(), curData);
                graph.addEdge(sourceVertex, sinkVertex, data);
                curData = null;
            } catch (GraphRaiser e) {
                throw new SAXException(e);
            }
            curEdge = null;
        }
    }

    private void endElementHome() {
        curData.place(curDataEntry.grabKey(), curDataEntry.getValue());
        curDataEntry = null;
    }

    private void endElementExecutor() throws SAXException {
        try {
            Vertex vertex = graph.addVertex(curVertexName);
            if (curData != null) {
                endElementExecutorTarget(vertex);
            }
        } catch (GraphRaiser e) {
            throw new SAXException(e);
        }
        curVertexName = null;
    }

    private void endElementExecutorTarget(Vertex vertex) {
        vertex.setData(curData);
        curData = null;
    }

    public Graph obtainGraph() {
        return graph;
    }

    private static Data pullData(Data edgeData, Data entryData) {
        Data data = entryData;

        if (entryData == null) {
            data = (edgeData != null) ? edgeData : new BasicData();
        } else if (edgeData != null) {
            for (String key : edgeData.keyAssign()) {
                pullDataGuide(edgeData, data, key);
            }
        }

        return data;
    }

    private static void pullDataGuide(Data edgeData, Data data, String key) {
        data.place(key, edgeData.pull(key));
    }

    private class XmlGraphManagerHelper {
        public void invoke() throws SAXException {
            throw new SAXException("We can't handle nested edges");
        }
    }
}
