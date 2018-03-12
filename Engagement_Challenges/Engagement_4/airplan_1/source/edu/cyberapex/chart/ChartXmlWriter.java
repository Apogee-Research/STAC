package edu.cyberapex.chart;

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

public class ChartXmlWriter extends ChartWriter {

    public static final String TYPE = "xml";

    @Override
    public void write(Chart chart, String filename) throws ChartWriterFailure {
        Document dom;

        // instance of a DocumentBuilderFactory
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        try {
            // use factory to get an instance of document builder
            DocumentBuilder db = dbf.newDocumentBuilder();
            // create instance of DOM
            dom = db.newDocument();
            Element rootEle = dom.createElement("graph");
            for (Vertex v : chart) {
                Element vertexEle = dom.createElement("vertex");
                vertexEle.setAttribute("name", v.getName());
                rootEle.appendChild(vertexEle);
                if (v.hasData()) {
                    writeHelp(dom, v, vertexEle);
                }
            }
            
            for (Vertex v : chart) {
                java.util.List<Edge> edges = chart.getEdges(v.getId());
                for (int a = 0; a < edges.size(); a++) {
                    Edge e = edges.get(a);
                    Element edgeEle = dom.createElement("edge");
                    edgeEle.setAttribute("src", e.getSource().getName());
                    edgeEle.setAttribute("dst", e.getSink().getName());
                    edgeEle.setAttribute("weight", Double.toString(e.getWeight()));
                    rootEle.appendChild(edgeEle);
                    if (e.hasData()) {
                        writeAid(dom, e, edgeEle);
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
            throw new ChartWriterFailure(e.getMessage());
        }
    }

    private void writeAid(Document dom, Edge e, Element edgeEle) {
        Element vertexDataEle = e.getData().generateXMLElement(dom);
        edgeEle.appendChild(vertexDataEle);
    }

    private void writeHelp(Document dom, Vertex v, Element vertexEle) {
        Element vertexDataEle = v.getData().generateXMLElement(dom);
        vertexEle.appendChild(vertexDataEle);
    }
}
