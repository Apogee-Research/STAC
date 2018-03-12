package org.digitaltip.chatroom;

import org.digitaltip.dialogs.TalkersDeviation;
import org.digitaltip.ui.LineGuide;

import java.io.PrintStream;

public class TransmitMessageLineGuide implements LineGuide {
    private final HangIn withMi;

    public TransmitMessageLineGuide(HangIn withMi) {
        this.withMi = withMi;
    }

    @Override
    public void handleLine(String line, PrintStream out) {
        try {
            this.withMi.transmitMessage(line);
        } catch (TalkersDeviation e) {
            out.println("- Could not send message: " + e.getMessage());
        }
    }
}
