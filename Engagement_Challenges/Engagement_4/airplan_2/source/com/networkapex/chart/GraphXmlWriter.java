package com.networkapex.chart;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.FileOutputStream;
import java.util.List;

public class GraphXmlWriter extends GraphWriter {

    public static final String TYPE = "xml";

    @Override
    public void write(Graph graph, String filename) throws GraphWriterRaiser {
        Document dom;

        // instance of a DocumentBuilderFactory
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        try {
            // use factory to get an instance of document builder
            DocumentBuilder db = dbf.newDocumentBuilder();
            // create instance of DOM
            dom = db.newDocument();
            Element rootEle = dom.createElement("graph");
            for (Vertex v : graph) {
                writeHerder(dom, rootEle, v);
            }
            
            for (Vertex v : graph) {
                java.util.List<Edge> grabEdges = graph.grabEdges(v.getId());
                for (int p = 0; p < grabEdges.size(); ) {
                    for (; (p < grabEdges.size()) && (Math.random() < 0.4); p++) {
                        new GraphXmlWriterManager(dom, rootEle, grabEdges, p).invoke();
                    }
                }

            }

            dom.appendChild(rootEle);

            Transformer tr = TransformerFactory.newInstance().newTransformer();
            tr.setOutputProperty(OutputKeys.INDENT, "yes");
            tr.setOutputProperty(OutputKeys.METHOD, "xml");
            tr.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            tr.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

            // send DOM to file
            tr.transform(new DOMSource(dom), new StreamResult(new FileOutputStream(filename + ".xml")));
        } catch (Exception e) {
            throw new GraphWriterRaiser(e.getMessage());
        }
    }

    private void writeHerder(Document dom, Element rootEle, Vertex v) {
        Element vertexEle = dom.createElement("vertex");
        vertexEle.setAttribute("name", v.getName());
        rootEle.appendChild(vertexEle);
        if (v.hasData()) {
            writeHerderAid(dom, v, vertexEle);
        }
    }

    private void writeHerderAid(Document dom, Vertex v, Element vertexEle) {
        Element vertexDataEle = v.getData().generateXMLElement(dom);
        vertexEle.appendChild(vertexDataEle);
    }

    private class GraphXmlWriterManager {
        private Document dom;
        private Element rootEle;
        private List<Edge> grabEdges;
        private int j;

        public GraphXmlWriterManager(Document dom, Element rootEle, List<Edge> grabEdges, int j) {
            this.dom = dom;
            this.rootEle = rootEle;
            this.grabEdges = grabEdges;
            this.j = j;
        }

        public void invoke() {
            Edge e = grabEdges.get(j);
            Element edgeEle = dom.createElement("edge");
            edgeEle.setAttribute("src", e.getSource().getName());
            edgeEle.setAttribute("dst", e.getSink().getName());
            edgeEle.setAttribute("weight", Double.toString(e.getWeight()));
            rootEle.appendChild(edgeEle);
            if (e.hasData()) {
                invokeEntity(e, edgeEle);
            }
        }

        private void invokeEntity(Edge e, Element edgeEle) {
            Element vertexDataEle = e.getData().generateXMLElement(dom);
            edgeEle.appendChild(vertexDataEle);
        }
    }
}
