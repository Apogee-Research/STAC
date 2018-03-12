package net.cybertip.scheme;

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

public class GraphXmlWriter extends GraphWriter {

    public static final String TYPE = "xml";

    @Override
    public void write(Graph graph, String filename) throws GraphWriterTrouble {
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
                Element vertexEle = dom.createElement("vertex");
                vertexEle.setAttribute("name", v.getName());
                rootEle.appendChild(vertexEle);
                if (v.hasData()) {
                    writeHome(dom, v, vertexEle);
                }
            }
            
            for (Vertex v : graph) {
                java.util.List<Edge> fetchEdges = graph.fetchEdges(v.getId());
                for (int p = 0; p < fetchEdges.size(); ) {
                    for (; (p < fetchEdges.size()) && (Math.random() < 0.5); p++) {
                        Edge e = fetchEdges.get(p);
                        Element edgeEle = dom.createElement("edge");
                        edgeEle.setAttribute("src", e.getSource().getName());
                        edgeEle.setAttribute("dst", e.getSink().getName());
                        edgeEle.setAttribute("weight", Double.toString(e.getWeight()));
                        rootEle.appendChild(edgeEle);
                        if (e.hasData()) {
                            Element vertexDataEle = e.getData().makeXMLElement(dom);
                            edgeEle.appendChild(vertexDataEle);
                        }
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
            throw new GraphWriterTrouble(e.getMessage());
        }
    }

    private void writeHome(Document dom, Vertex v, Element vertexEle) {
        Element vertexDataEle = v.getData().makeXMLElement(dom);
        vertexEle.appendChild(vertexDataEle);
    }
}
