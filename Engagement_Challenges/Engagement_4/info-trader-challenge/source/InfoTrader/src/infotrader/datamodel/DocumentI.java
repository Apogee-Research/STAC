/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package infotrader.datamodel;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.tree.DefaultMutableTreeNode;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;

/**
 *
 * @author user
 */
public class DocumentI extends NodeBase {

    List<HyperLink> link;
    SerializationPosition state;
    private final String catagory;

    public DocumentI(String cat) {
        super(cat);
        this.catagory = cat;
        link = new ArrayList<HyperLink>();
    }

    public String getCatagory() {
        return catagory;
    }

    //STAC:Function to serialize/write out doc type
    public Element serialize(Node node, SerializationPosition state) {

        Element doc = state.doc;
        DefaultMutableTreeNode treeNode = state.currPos;

        Object value = treeNode.getUserObject();

        Element parentElement = null;
        parentElement = doc.getOwnerDocument().createElement("doc");

        DocumentI book = (DocumentI) value;
        // Apply properties to root element...
        Attr attrName = doc.getOwnerDocument().createAttribute("name");

        attrName.setNodeValue(book.getCatagory());
        parentElement.getAttributes().setNamedItem(attrName);

        this.state = state;
        Iterator<HyperLink> it = link.iterator();

        //I believe this code no longer does anything, but unwilling to change anything at this point
        while (it.hasNext()) {
            HyperLink next = it.next();
            if (this.state.contains(next)) {
                state.add(next);
                next.serialize(next, state);
            }

        }

        return parentElement;
    }

    public void addLink(String linkstr) {

        HyperLink hlink = new HyperLink(linkstr);
        link.add(hlink);
    }

}
