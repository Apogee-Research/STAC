/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package infotrader.datamodel;

import infotrader.dataprocessing.SiteMapGenerator;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;

/**
 *
 * @author user
 */
public class HyperLink extends Directory {

    static int id = 0;
    public static boolean links_to_directory() {
//serialize_hyperlink(link, state):
//	write out link    
        return false;
    }

    public static boolean links_to_existing() {
        //STUB, NOT NEEDED ANYMORE -- WAS MORE EFFICIENT TO IMPLEMENT UPFRONT WHEN CHECKING THE LINEAR LIST OF FILES
        return true;
    }

    public HyperLink(String linkstr) {
        //super(linkstr);
        String idstr = "-"+id;
        this.name = linkstr+idstr;
        id++;
    }

    @Override
    public Element serialize(Node node, SerializationPosition state) {

        String name1 = node.getName();
        //System.out.println("level:"+state.level+" name:" +name1);
        int lastIndexOf = name1.lastIndexOf('-');
        String namenorm = name1.substring(0, lastIndexOf);
        Node get = NodeBase.nodes.get(namenorm);
        if(get==null){
            return null;
        }

        Element doc = state.doc;

        //STAC:This logic finds the node the hyperlink points to in order to allow checks on that node.
        //It will not omit the empty string root node, which does not have a proper Directory type.
        TreeNode thenode = SiteMapGenerator.visitAllNodes(get.getName());
        DefaultMutableTreeNode noded = (DefaultMutableTreeNode)thenode;
        Object v = noded.getUserObject();

        Element parentElement = null;//doc.getOwnerDocument().getElementById(get.getName());
        state.currPos = noded;
        
        //STAC:If neither of these cases occur, the state position will no be reset and the state that
        //allows the vulnerability to occur will be set
        if(v instanceof Directory){
                       
            Directory dir = (Directory) v;
            // Apply properties to root element...
            Attr attrName = doc.getOwnerDocument().createAttribute("url");
            attrName.setNodeValue(dir.getName());
            
            parentElement = doc.getOwnerDocument().createElement("link");
            parentElement.getAttributes().setNamedItem(attrName);
        }
        //STAC: Red herring. This logic element causes an eternal loop that is limited by JVM stack size
        //The Eventual StackOverflow error occurs quickly and is handled gracefully -- so NO STAC ERROR
        if(v instanceof DocumentI){
            
            DocumentI dir = (DocumentI) v;
            // Apply properties to root element...
            Attr attrName = doc.getOwnerDocument().createAttribute("url");
            attrName.setNodeValue(dir.getName());
            
            parentElement = doc.getOwnerDocument().createElement("link");
            parentElement.getAttributes().setNamedItem(attrName);
        }
        return parentElement;

    }

}
