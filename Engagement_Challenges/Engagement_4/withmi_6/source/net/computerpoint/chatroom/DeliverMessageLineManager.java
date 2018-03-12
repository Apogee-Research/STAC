package net.computerpoint.chatroom;

import net.computerpoint.dialogs.ProtocolsDeviation;
import net.computerpoint.console.LineManager;

import java.io.PrintStream;

public class DeliverMessageLineManager implements LineManager {
    private final HangIn withMi;

    public DeliverMessageLineManager(HangIn withMi) {
        this.withMi = withMi;
    }

    @Override
    public void handleLine(String line, PrintStream out) {
        try {
            this.withMi.deliverMessage(line);
        } catch (ProtocolsDeviation e) {
            out.println("- Could not send message: " + e.getMessage());
        }
    }
}
