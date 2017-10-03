/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package user.commands;

/**
 *
 * @author user
 */
public class UserInputException extends Exception {

    /**
     * Creates a new instance of <code>UserInputException</code> without detail
     * message.
     */
    public UserInputException() {
    }

    /**
     * Constructs an instance of <code>UserInputException</code> with the
     * specified detail message.
     *
     * @param msg the detail message.
     */
    public UserInputException(String msg) {
        super(msg);
    }
}
