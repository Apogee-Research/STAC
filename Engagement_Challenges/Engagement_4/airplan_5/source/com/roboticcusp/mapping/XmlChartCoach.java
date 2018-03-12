package com.roboticcusp.mapping;

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
public class XmlChartCoach extends DefaultHandler {
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

        public String fetchDst() {
            return dst;
        }

        public Data getData() {
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

        public String grabValue() {
            return value.toString();
        }
    }

    private Chart chart;
    private String curVertexName;
    private Data curData;
    private DataElement curDataEntry;
    private EdgeElement curEdge;

    @Override
    public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
        if (localName.equals("graph")) {
            if (chart != null) {
                startElementCoordinator();
            }
            chart = ChartFactory.newInstance();
        } else if (localName.equals("vertex")) {
            if (curVertexName != null) {
                throw new SAXException("We can't handle nested vertices");
            }
            if (chart == null) {
                startElementGateKeeper();
            }
            curVertexName = atts.getValue("name");
        } else if (localName.equals("data")) {
            if ((curVertexName == null) && (curEdge == null)) { // no active valid element
                throw new SAXException("Invalid <data> element; must be a child of vertex or edge");
            }
            curData = new BasicData();
        } else if (localName.equals("entry")) {
            if (curData == null) {
                startElementAdviser();
            }
            if (curDataEntry != null) {
                startElementUtility();
            }
            curDataEntry = new DataElement(atts);
        } else if (localName.equals("edge")) {
            startElementExecutor(atts);
        }
    }

    private void startElementExecutor(Attributes atts) throws SAXException {
        if (curEdge != null) {
            throw new SAXException("We can't handle nested edges");
        }
        if (chart == null) {
            throw new SAXException("Graph must be specified before an Edge");
        }
        curEdge = new EdgeElement(atts);
    }

    private void startElementUtility() throws SAXException {
        throw new SAXException("Entry tags may not be nested");
    }

    private void startElementAdviser() throws SAXException {
        throw new SAXException("Entry must be a child of a <data> element");
    }

    private void startElementGateKeeper() throws SAXException {
        throw new SAXException("Graph must be specified before a Vertex");
    }

    private void startElementCoordinator() throws SAXException {
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
            endElementEngine();
        } else if (localName.equals("entry")) {
            curData.put(curDataEntry.takeKey(), curDataEntry.grabValue());
            curDataEntry = null;
        } else if (localName.equals("edge")) {
            try {
                int sourceVertex = chart.obtainVertexIdByName(curEdge.pullSrc());
                int sinkVertex = chart.obtainVertexIdByName(curEdge.fetchDst());
                Data data = obtainData(curEdge.getData(), curData);
                chart.addEdge(sourceVertex, sinkVertex, data);
                curData = null;
            } catch (ChartException e) {
                throw new SAXException(e);
            }
            curEdge = null;
        }
    }

    private void endElementEngine() throws SAXException {
        try {
            Vertex vertex = chart.addVertex(curVertexName);
            if (curData != null) {
                endElementEngineAid(vertex);
            }
        } catch (ChartException e) {
            throw new SAXException(e);
        }
        curVertexName = null;
    }

    private void endElementEngineAid(Vertex vertex) {
        vertex.setData(curData);
        curData = null;
    }

    public Chart takeChart() {
        return chart;
    }

    private static Data obtainData(Data edgeData, Data entryData) {
        Data data = entryData;

        if (entryData == null) {
            data = (edgeData != null) ? edgeData : new BasicData();
        } else if (edgeData != null) {
            fetchDataHelp(edgeData, data);
        }

        return data;
    }

    private static void fetchDataHelp(Data edgeData, Data data) {
        for (String key : edgeData.keyDefine()) {
            data.put(key, edgeData.grab(key));
        }
    }
}
