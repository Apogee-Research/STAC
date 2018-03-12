/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package collab;

/**
 *
 * @author user
 */
public class CollabRuntimeException extends  RuntimeException {

    public CollabRuntimeException(String message) {
        super(message);
    }

    public CollabRuntimeException() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
