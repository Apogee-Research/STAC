package com.cyberpointllc.stac.webserver;


public class UserException extends Exception {

    public UserException(User user, String message) {
        super(String.format("user: %s: %s", user, message));
    }
}
