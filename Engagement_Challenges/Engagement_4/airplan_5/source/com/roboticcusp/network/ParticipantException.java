package com.roboticcusp.network;

public class ParticipantException extends Exception {

    public ParticipantException(Participant participant, String message) {
        super(String.format("user: %s: %s", participant, message));
    }
}
