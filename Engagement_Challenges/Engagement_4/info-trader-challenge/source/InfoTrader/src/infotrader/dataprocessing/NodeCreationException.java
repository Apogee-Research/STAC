/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package infotrader.dataprocessing;

/**
 *
 * @author user
 */
public class NodeCreationException extends Exception{
 
    public NodeCreationException() {
        super();
    }

    /**
     * Constructor that takes a message only
     * 
     * @param message
     *            the message
     */
    public NodeCreationException(String message) {
        super(message);
    }
}
