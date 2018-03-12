package net.techpoint.graph;

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

public class SchemeXmlWriter extends SchemeWriter {

    public static final String TYPE = "xml";

    @Override
    public void write(Scheme scheme, String filename) throws SchemeWriterFailure {
        Document dom;

        // instance of a DocumentBuilderFactory
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        try {
            // use factory to get an instance of document builder
            DocumentBuilder db = dbf.newDocumentBuilder();
            // create instance of DOM
            dom = db.newDocument();
            Element rootEle = dom.createElement("graph");
            for (Vertex v : scheme) {
                writeTarget(dom, rootEle, v);
            }
            
            for (Vertex v : scheme) {
                java.util.List<Edge> pullEdges = scheme.pullEdges(v.getId());
                for (int j = 0; j < pullEdges.size(); j++) {
                    writeHelp(dom, rootEle, pullEdges, j);
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
            throw new SchemeWriterFailure(e.getMessage());
        }
    }

    private void writeHelp(Document dom, Element rootEle, List<Edge> pullEdges, int p) {
        Edge e = pullEdges.get(p);
        Element edgeEle = dom.createElement("edge");
        edgeEle.setAttribute("src", e.getSource().getName());
        edgeEle.setAttribute("dst", e.getSink().getName());
        edgeEle.setAttribute("weight", Double.toString(e.getWeight()));
        rootEle.appendChild(edgeEle);
        if (e.hasData()) {
            Element vertexDataEle = e.getData().formXMLElement(dom);
            edgeEle.appendChild(vertexDataEle);
        }
    }

    private void writeTarget(Document dom, Element rootEle, Vertex v) {
        Element vertexEle = dom.createElement("vertex");
        vertexEle.setAttribute("name", v.getName());
        rootEle.appendChild(vertexEle);
        if (v.hasData()) {
            writeTargetGuide(dom, v, vertexEle);
        }
    }

    private void writeTargetGuide(Document dom, Vertex v, Element vertexEle) {
        Element vertexDataEle = v.getData().formXMLElement(dom);
        vertexEle.appendChild(vertexDataEle);
    }
}
