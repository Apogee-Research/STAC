/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client;

/**
 *
 * @author user
 */
public class CollabConnException extends Exception {

    /**
     * Creates a new instance of <code>CollabConnException</code> without detail
     * message.
     */
    public CollabConnException() {
    }

    /**
     * Constructs an instance of <code>CollabConnException</code> with the
     * specified detail message.
     *
     * @param msg the detail message.
     */
    public CollabConnException(String msg) {
        super(msg);
    }
}
