package edu.networkcusp.chatbox;

import edu.networkcusp.protocols.CommunicationsFailure;
import edu.networkcusp.terminal.LineGuide;

import java.io.PrintStream;

public class DeliverMessageLineGuide implements LineGuide {
    private final HangIn withMi;

    public DeliverMessageLineGuide(HangIn withMi) {
        this.withMi = withMi;
    }

    @Override
    public void handleLine(String line, PrintStream out) {
        try {
            this.withMi.deliverMessage(line);
        } catch (CommunicationsFailure e) {
            out.println("- Could not send message: " + e.getMessage());
        }
    }
}
