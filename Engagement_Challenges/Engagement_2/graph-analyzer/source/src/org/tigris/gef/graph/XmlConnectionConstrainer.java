package org.tigris.gef.graph;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * See issue 105.
 *
 * @author Bob Tarling
 * @since 25-May-2004
 */
public class XmlConnectionConstrainer extends ConnectionConstrainer {

    public XmlConnectionConstrainer(Document doc) throws GraphModelException {
        try {
            Element root = doc.getDocumentElement();
            Element connectionsElement = (Element) root.getElementsByTagName(
                    "connections").item(0);
            NodeList connectionNodes = connectionsElement
                    .getElementsByTagName("connection");

            int connectionCount = connectionNodes.getLength();
            for (int i = 0; i < connectionCount; ++i) {
                Element connection = (Element) connectionNodes.item(i);
                Element edgeElement = (Element) connection
                        .getElementsByTagName("edge").item(0);
                String edgeClassName = edgeElement.getFirstChild()
                        .getNodeValue();
                NodeList portNodeList = connection.getElementsByTagName("port");
                Element port1Element = (Element) portNodeList.item(0);
                String portClassName1 = port1Element.getFirstChild()
                        .getNodeValue();
                if (portNodeList.getLength() == 1) {
                    addValidConnection(edgeClassName, portClassName1);
                } else {
                    Element port2Element = (Element) portNodeList.item(1);
                    String portClassName2 = port2Element.getFirstChild()
                            .getNodeValue();
                    addValidConnection(edgeClassName, portClassName1,
                            portClassName2);
                }
            }
        } catch (DOMException e) {
            throw new GraphModelException(e);
        } catch (ClassNotFoundException e) {
            throw new GraphModelException(e);
        }
    }

    protected void addValidConnection(String edgeClassName,
            String portClassName1, String portClassName2)
            throws ClassNotFoundException {
        Class edgeClass = Class.forName(edgeClassName);
        Class port1Class = Class.forName(portClassName1);
        Class port2Class = Class.forName(portClassName2);
        addValidConnection(edgeClass, port1Class, port2Class);
    }

    protected void addValidConnection(String edgeClassName, String portClassName)
            throws ClassNotFoundException {
        addValidConnection(Class.forName(edgeClassName), Class
                .forName(portClassName));
    }
}
