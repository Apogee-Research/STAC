package net.computerpoint.dialogs;

public class ProtocolsDeviation extends Exception {
    public ProtocolsDeviation(String message) {
        super(message);
    }

    public ProtocolsDeviation(Throwable t) {
        super(t);
    }
}
