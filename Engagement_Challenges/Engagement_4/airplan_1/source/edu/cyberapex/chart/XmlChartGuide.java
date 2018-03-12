package edu.cyberapex.chart;

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
public class XmlChartGuide extends DefaultHandler {
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

        public String fetchDst() {
            return dst;
        }

        public Data pullData() {
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

        public String getKey() {
            return key;
        }

        public void addCharacters(char[] ch, int start, int length) {
            value.append(ch, start, length);
        }

        public String fetchValue() {
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
                startElementHerder();
            }
            chart = ChartFactory.newInstance();
        } else if (localName.equals("vertex")) {
            startElementService(atts);
        } else if (localName.equals("data")) {
            new XmlChartGuideEngine().invoke();
        } else if (localName.equals("entry")) {
            new XmlChartGuideWorker(atts).invoke();
        } else if (localName.equals("edge")) {
            startElementFunction(atts);
        }
    }

    private void startElementFunction(Attributes atts) throws SAXException {
        if (curEdge != null) {
            startElementFunctionGuide();
        }
        if (chart == null) {
            startElementFunctionHandler();
        }
        curEdge = new EdgeElement(atts);
    }

    private void startElementFunctionHandler() throws SAXException {
        throw new SAXException("Graph must be specified before an Edge");
    }

    private void startElementFunctionGuide() throws SAXException {
        throw new SAXException("We can't handle nested edges");
    }

    private void startElementService(Attributes atts) throws SAXException {
        if (curVertexName != null) {
            throw new SAXException("We can't handle nested vertices");
        }
        if (chart == null) {
            throw new SAXException("Graph must be specified before a Vertex");
        }
        curVertexName = atts.getValue("name");
    }

    private void startElementHerder() throws SAXException {
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
            endElementSupervisor();
        } else if (localName.equals("entry")) {
            curData.put(curDataEntry.getKey(), curDataEntry.fetchValue());
            curDataEntry = null;
        } else if (localName.equals("edge")) {
            endElementHelp();
        }
    }

    private void endElementHelp() throws SAXException {
        try {
            int sourceVertex = chart.getVertexIdByName(curEdge.takeSrc());
            int sinkVertex = chart.getVertexIdByName(curEdge.fetchDst());
            Data data = obtainData(curEdge.pullData(), curData);
            chart.addEdge(sourceVertex, sinkVertex, data);
            curData = null;
        } catch (ChartFailure e) {
            throw new SAXException(e);
        }
        curEdge = null;
    }

    private void endElementSupervisor() throws SAXException {
        try {
            Vertex vertex = chart.addVertex(curVertexName);
            if (curData != null) {
                vertex.setData(curData);
                curData = null;
            }
        } catch (ChartFailure e) {
            throw new SAXException(e);
        }
        curVertexName = null;
    }

    public Chart getChart() {
        return chart;
    }

    private static Data obtainData(Data edgeData, Data entryData) {
        Data data = entryData;

        if (entryData == null) {
            data = (edgeData != null) ? edgeData : new BasicData();
        } else if (edgeData != null) {
            for (String key : edgeData.keySet()) {
                data.put(key, edgeData.fetch(key));
            }
        }

        return data;
    }

    private class XmlChartGuideEngine {
        public void invoke() throws SAXException {
            if ((curVertexName == null) && (curEdge == null)) { // no active valid element
                throw new SAXException("Invalid <data> element; must be a child of vertex or edge");
            }
            curData = new BasicData();
        }
    }

    private class XmlChartGuideWorker {
        private Attributes atts;

        public XmlChartGuideWorker(Attributes atts) {
            this.atts = atts;
        }

        public void invoke() throws SAXException {
            if (curData == null) {
                invokeExecutor();
            }
            if (curDataEntry != null) {
                throw new SAXException("Entry tags may not be nested");
            }
            curDataEntry = new DataElement(atts);
        }

        private void invokeExecutor() throws SAXException {
            throw new SAXException("Entry must be a child of a <data> element");
        }
    }
}
