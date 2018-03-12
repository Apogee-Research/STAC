/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package infotrader.datamodel;

import infotrader.dataprocessing.SiteMapGenerator;
import java.util.Enumeration;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

/**
 *
 * @author user
 */
public class Directory extends NodeBase {


    //private String parent;

    public Directory(String name) {
        super(name);
    }




//STAC:4.3.1.1 -- This method checks whether a Node is a legit directory by name
    public static boolean isa( String name, SerializationPosition state) {
        
        //STAC:Eleminates non-dirs with reflection
        if(state.currPos.getUserObject() instanceof HyperLink || state.currPos.getUserObject() instanceof DocumentI)
            return false;
        
        Object root = SiteMapGenerator.model.getRoot();

        //STAC:Find any duplicate entries in the tree
        boolean checkifdirmatches= checkifdirmatches(new Directory(root.toString()), (DefaultMutableTreeNode)root, name);
        return checkifdirmatches;
        
    }

    public Directory() {
        
    }
    
    private static  boolean checkifdirmatches(NodeBase node, DefaultMutableTreeNode tnode, String name){
        
        //STAC:Recursively move through tree to look for a match
        if(node instanceof Directory){
        
            if(node.getName().equalsIgnoreCase(name))
                return true;
        }
        Enumeration children = tnode.children();
        while(children.hasMoreElements()){
        
            DefaultMutableTreeNode nextElement = (DefaultMutableTreeNode)children.nextElement();
            NodeBase chuserObject = (NodeBase)nextElement.getUserObject();
            
            if(checkifdirmatches(chuserObject, nextElement, name))
                return true;
            
        }
    
       return false; 
    }

    public static Element exists( String name, SerializationPosition state) {
        //This is without vulnerability
        //TreeNode visitAllNodes = TreeExample.visitAllNodes(name);

        //if(node instanceof Directory)
        ////System.out.println("doc_rootnname: "+state.currPos.toString());
        NodeList elementsByTagName = SiteMapGenerator.doc.getElementsByTagName("directory");
        for(int i=0;i<elementsByTagName.getLength();i++){
        
            org.w3c.dom.Node item = elementsByTagName.item(i);
            String toString = item.toString();
            
            NamedNodeMap attributes = item.getAttributes();
            org.w3c.dom.Node namedItem = attributes.getNamedItem("name");
            String nodeValue = namedItem.getNodeValue();
            ////System.out.println("nname: "+nodeValue);
            if(nodeValue.equalsIgnoreCase(state.currPos.toString())){
                return serialize(state);
                //return (Element)item;
            }
        }
        return null;
        
    }
    
public static Element serialize( SerializationPosition state) {
        /*serialize_directory(dir, state):
         write out dir;
         for each child c of dir, left to right:
         serialize(c, state) under where dir was written;*/
        Element doc = state.doc;
        DefaultMutableTreeNode treeNode = state.currPos;
        
        Object value = treeNode.getUserObject();
        Element parentElement = null;  
        
            parentElement = doc.getOwnerDocument().createElement("directory");

            
            // Apply properties to root element...
            Attr attrName = doc.getOwnerDocument().createAttribute("name");
            attrName.setNodeValue(state.currPos.toString());
            parentElement.getAttributes().setNamedItem(attrName);
            
            return parentElement;
    }
    @Override
    public Element serialize(Node node, SerializationPosition state) {
        /*serialize_directory(dir, state):
         write out dir;
         for each child c of dir, left to right:
         serialize(c, state) under where dir was written;*/
        Element doc = state.doc;
        DefaultMutableTreeNode treeNode = state.currPos;
        
        Object value = treeNode.getUserObject();
        Element parentElement = null;  
        
            parentElement = doc.getOwnerDocument().createElement("directory");

            Directory book = (Directory) value;
            // Apply properties to root element...
            Attr attrName = doc.getOwnerDocument().createAttribute("name");
            attrName.setNodeValue(book.getName());
            parentElement.getAttributes().setNamedItem(attrName);
            
            return parentElement;
    }

}
