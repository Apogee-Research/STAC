/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package infotrader.datamodel;

import javax.swing.tree.DefaultMutableTreeNode;
import org.w3c.dom.Element;

/**
 *
 * @author user
 */
public class SerializationPosition {

        
    
    
        public DefaultMutableTreeNode currPos;
        public Element doc;
        //public int level = 0;

        
        public SerializationPosition dup(){
            SerializationPosition statedup = new SerializationPosition();
            statedup.currPos = currPos;
            statedup.doc = doc;
            return statedup;
        }

    boolean contains(HyperLink next) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    void add(HyperLink next) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    };
