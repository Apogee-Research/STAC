package net.robotictip.dropbys;

import net.robotictip.protocols.SenderReceiversTrouble;
import net.robotictip.display.LineManager;

import java.io.PrintStream;

public class TransferMessageLineManager implements LineManager {
    private final HangIn withMi;

    public TransferMessageLineManager(HangIn withMi) {
        this.withMi = withMi;
    }

    @Override
    public void handleLine(String line, PrintStream out) {
        try {
            this.withMi.transferMessage(line);
        } catch (SenderReceiversTrouble e) {
            out.println("- Could not send message: " + e.getMessage());
        }
    }
}
