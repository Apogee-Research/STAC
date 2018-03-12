package net.techpoint.graph;

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
public class XmlSchemeGuide extends DefaultHandler {
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

        public String fetchKey() {
            return key;
        }

        public void addCharacters(char[] ch, int start, int length) {
            value.append(ch, start, length);
        }

        public String pullValue() {
            return value.toString();
        }
    }

    private Scheme scheme;
    private String curVertexName;
    private Data curData;
    private DataElement curDataEntry;
    private EdgeElement curEdge;

    @Override
    public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
        if (localName.equals("graph")) {
            if (scheme != null) {
                throw new SAXException("We can't handle nested graphs");
            }
            scheme = SchemeFactory.newInstance();
        } else if (localName.equals("vertex")) {
            if (curVertexName != null) {
                throw new SAXException("We can't handle nested vertices");
            }
            if (scheme == null) {
                startElementHerder();
            }
            curVertexName = atts.getValue("name");
        } else if (localName.equals("data")) {
            startElementGateKeeper();
        } else if (localName.equals("entry")) {
            startElementEngine(atts);
        } else if (localName.equals("edge")) {
            if (curEdge != null) {
                startElementUtility();
            }
            if (scheme == null) {
                startElementGuide();
            }
            curEdge = new EdgeElement(atts);
        }
    }

    private void startElementGuide() throws SAXException {
        throw new SAXException("Graph must be specified before an Edge");
    }

    private void startElementUtility() throws SAXException {
        throw new SAXException("We can't handle nested edges");
    }

    private void startElementEngine(Attributes atts) throws SAXException {
        if (curData == null) {
            throw new SAXException("Entry must be a child of a <data> element");
        }
        if (curDataEntry != null) {
            startElementEngineFunction();
        }
        curDataEntry = new DataElement(atts);
    }

    private void startElementEngineFunction() throws SAXException {
        new XmlSchemeGuideHome().invoke();
    }

    private void startElementGateKeeper() throws SAXException {
        new XmlSchemeGuideUtility().invoke();
    }

    private void startElementHerder() throws SAXException {
        throw new SAXException("Graph must be specified before a Vertex");
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        if (curDataEntry != null) {
            charactersExecutor(ch, start, length);
        }
    }

    private void charactersExecutor(char[] ch, int start, int length) {
        curDataEntry.addCharacters(ch, start, length);
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (localName.equals("vertex")) {
            endElementEngine();
        } else if (localName.equals("entry")) {
            endElementHome();
        } else if (localName.equals("edge")) {
            try {
                int sourceVertex = scheme.getVertexIdByName(curEdge.takeSrc());
                int sinkVertex = scheme.getVertexIdByName(curEdge.getDst());
                Data data = fetchData(curEdge.grabData(), curData);
                scheme.addEdge(sourceVertex, sinkVertex, data);
                curData = null;
            } catch (SchemeFailure e) {
                throw new SAXException(e);
            }
            curEdge = null;
        }
    }

    private void endElementHome() {
        curData.place(curDataEntry.fetchKey(), curDataEntry.pullValue());
        curDataEntry = null;
    }

    private void endElementEngine() throws SAXException {
        new XmlSchemeGuideHelper().invoke();
    }

    public Scheme getScheme() {
        return scheme;
    }

    private static Data fetchData(Data edgeData, Data entryData) {
        Data data = entryData;

        if (entryData == null) {
            data = (edgeData != null) ? edgeData : new BasicData();
        } else if (edgeData != null) {
            fetchDataHelp(edgeData, data);
        }

        return data;
    }

    private static void fetchDataHelp(Data edgeData, Data data) {
        for (String key : edgeData.keyAssign()) {
            grabDataHelpTarget(edgeData, data, key);
        }
    }

    private static void grabDataHelpTarget(Data edgeData, Data data, String key) {
        data.place(key, edgeData.obtain(key));
    }

    private class XmlSchemeGuideUtility {
        public void invoke() throws SAXException {
            if ((curVertexName == null) && (curEdge == null)) { // no active valid element
                throw new SAXException("Invalid <data> element; must be a child of vertex or edge");
            }
            curData = new BasicData();
        }
    }

    private class XmlSchemeGuideHome {
        public void invoke() throws SAXException {
            throw new SAXException("Entry tags may not be nested");
        }
    }

    private class XmlSchemeGuideHelper {
        public void invoke() throws SAXException {
            try {
                Vertex vertex = scheme.addVertex(curVertexName);
                if (curData != null) {
                    vertex.setData(curData);
                    curData = null;
                }
            } catch (SchemeFailure e) {
                throw new SAXException(e);
            }
            curVertexName = null;
        }
    }
}
