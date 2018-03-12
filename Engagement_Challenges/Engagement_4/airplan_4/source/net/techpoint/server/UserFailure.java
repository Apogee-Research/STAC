package net.techpoint.server;

public class UserFailure extends Exception {

    public UserFailure(User user, String message) {
        super(String.format("user: %s: %s", user, message));
    }
}
