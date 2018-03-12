package com.techtip.chatbox;

import com.techtip.communications.DialogsDeviation;
import com.techtip.control.LineHandler;

import java.io.PrintStream;

public class TransmitMessageLineHandler implements LineHandler {
    private final DropBy withMi;

    public TransmitMessageLineHandler(DropBy withMi) {
        this.withMi = withMi;
    }

    @Override
    public void handleLine(String line, PrintStream out) {
        try {
            this.withMi.transmitMessage(line);
        } catch (DialogsDeviation e) {
            out.println("- Could not send message: " + e.getMessage());
        }
    }
}
