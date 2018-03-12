package net.computerpoint.chatroom;

public class AvailableFilesCommandBuilder {
    private HangIn withMi;

    public AvailableFilesCommandBuilder setWithMi(HangIn withMi) {
        this.withMi = withMi;
        return this;
    }

    public AvailableFilesCommand formAvailableFilesCommand() {
        return new AvailableFilesCommand(withMi);
    }
}