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
class DuplicateKeyException extends Exception {

    public DuplicateKeyException() {
    }
    
    public DuplicateKeyException(String message) {
        super(message);
    }
    
}
