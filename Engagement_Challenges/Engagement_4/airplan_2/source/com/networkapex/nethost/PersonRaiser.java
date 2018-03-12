package com.networkapex.nethost;

public class PersonRaiser extends Exception {

    public PersonRaiser(Person person, String message) {
        super(String.format("user: %s: %s", person, message));
    }
}
