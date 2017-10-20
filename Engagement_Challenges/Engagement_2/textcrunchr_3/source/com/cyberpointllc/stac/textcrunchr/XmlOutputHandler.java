package com.cyberpointllc.stac.textcrunchr;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import com.cyberpointllc.stac.textcrunchr.OutputHandlerException;

public class XmlOutputHandler extends OutputHandler {

    private final String xmlFileName = "TextCrunchr.xml";

    public void do_conclude() throws OutputHandlerException {
        Document dom;
        // instance of a DocumentBuilderFactory
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            // use factory to get an instance of document builder
            DocumentBuilder db = dbf.newDocumentBuilder();
            // create instance of DOM
            dom = db.newDocument();
            Element rootEle = dom.createElement("textcrunchr");
            for (String filename : sortedFiles) {
                do_concludeHelper(rootEle, dom, filename);
            }
            dom.appendChild(rootEle);
            try {
                Transformer tr = TransformerFactory.newInstance().newTransformer();
                tr.setOutputProperty(OutputKeys.INDENT, "yes");
                tr.setOutputProperty(OutputKeys.METHOD, "xml");
                tr.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
                tr.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
                // send DOM to file
                tr.transform(new  DOMSource(dom), new  StreamResult(new  FileOutputStream(xmlFileName)));
                System.out.println("Results sent to " + xmlFileName);
            } catch (TransformerException te) {
                throw new  OutputHandlerException(te.getMessage());
            } catch (IOException ioe) {
                throw new  OutputHandlerException(ioe.getMessage());
            }
        } catch (ParserConfigurationException pce) {
            throw new  OutputHandlerException("UsersXML: Error trying to instantiate DocumentBuilder " + pce);
        }
    }

    private void do_concludeHelper(Element rootEle, Document dom, String filename) throws OutputHandlerException, ParserConfigurationException {
        Element filenameEle = dom.createElement("file");
        filenameEle.setAttribute("name", filename);
        rootEle.appendChild(filenameEle);
        String path = namesToPaths.get(filename);
        List<TCResult> sampleResults = results.get(path);
        for (TCResult result : sampleResults) {
            Element name = dom.createElement("result");
            name.setAttribute("name", result.getName());
            name.appendChild(dom.createCDATASection(result.getValue()));
            filenameEle.appendChild(name);
        }
    }
}
