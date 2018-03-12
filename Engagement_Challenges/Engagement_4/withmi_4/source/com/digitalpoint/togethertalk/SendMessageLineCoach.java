package com.digitalpoint.togethertalk;

import com.digitalpoint.dialogs.SenderReceiversException;
import com.digitalpoint.terminal.LineCoach;

import java.io.PrintStream;

public class SendMessageLineCoach implements LineCoach {
    private final HangIn withMi;

    public SendMessageLineCoach(HangIn withMi) {
        this.withMi = withMi;
    }

    @Override
    public void handleLine(String line, PrintStream out) {
        try {
            this.withMi.sendMessage(line);
        } catch (SenderReceiversException e) {
            out.println("- Could not send message: " + e.getMessage());
        }
    }
}
