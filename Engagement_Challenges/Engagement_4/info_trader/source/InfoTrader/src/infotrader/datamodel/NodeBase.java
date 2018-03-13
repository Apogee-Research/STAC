/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package infotrader.datamodel;

import java.util.HashMap;
import java.util.Map;
import org.w3c.dom.Element;

/**
 *
 * @author user
 */
public abstract class NodeBase implements Node{
    
    public static Map<String, Node> nodes;
    
    
    
    String name;
    static{
        nodes = new HashMap<String, Node>();
    
    }
    
    public NodeBase() {
        name = null;
    }
    
    public NodeBase(String name) {
        this.name = name;
        //parent = parent;
        
        nodes.put(name, this);
        
    }
    
    public String getName() {
        return name;
    }
    
        public String toString() {
        return name;
    }
    public abstract Element serialize(Node node, SerializationPosition state);

    void children() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
