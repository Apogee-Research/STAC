package com.nicnilov.textmeter;

/**
 * Created as part of textmeter project
 * by Nic Nilov on 26.10.13 at 22:18
 */
public class NotInitializedException extends RuntimeException {

    public NotInitializedException() {
        super();
    }

    public NotInitializedException(String message) {
        super(message);
    }
}
