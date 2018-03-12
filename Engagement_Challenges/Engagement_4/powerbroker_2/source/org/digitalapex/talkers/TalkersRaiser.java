package org.digitalapex.talkers;

public class TalkersRaiser extends Exception {
    public TalkersRaiser(String message) {
        super(message);
    }

    public TalkersRaiser(Throwable t) {
        super(t);
    }
}
