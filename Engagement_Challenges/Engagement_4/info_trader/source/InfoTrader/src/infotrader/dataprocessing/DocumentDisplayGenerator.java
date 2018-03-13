/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package infotrader.dataprocessing;

import infotrader.parser.exception.InfoTraderWriterException;

/**
 *
 * @author user
 */
public class DocumentDisplayGenerator {

    StringBuffer res;
    
    public DocumentDisplayGenerator(){
        res = new StringBuffer();
    }
    
    public String getResult() {
        return res.toString();
    }

    public void appendln(String line) throws InfoTraderWriterException {
        
        //if(res.length() > 10000000){
        //    throw new InfoTraderWriterException();
        //}
        res.append(line);
        res.append("/n");
    }

    public void end() {
        res.append("/end");
    }
    
    
    
}
