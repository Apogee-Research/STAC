/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package infotrader.dataprocessing;

import infotrader.datamodel.DocumentI;

/**
 *
 * @author user
 */
public class ContentExtractor extends Extractor{

    public ContentExtractor(DocumentI doc) {
        super(doc);
    }


    
    @Override
    public void extract(String doc) {
        
        
        
    }
    
    
    public void addLink(String link){
        
        
        doc.addLink(link);
        
    }
    
    
    
}
