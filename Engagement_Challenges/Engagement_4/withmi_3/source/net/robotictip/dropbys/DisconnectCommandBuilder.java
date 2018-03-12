package net.robotictip.dropbys;

public class DisconnectCommandBuilder {
    private HangIn withMi;

    public DisconnectCommandBuilder setWithMi(HangIn withMi) {
        this.withMi = withMi;
        return this;
    }

    public DisconnectCommand generateDisconnectCommand() {
        return new DisconnectCommand(withMi);
    }
}