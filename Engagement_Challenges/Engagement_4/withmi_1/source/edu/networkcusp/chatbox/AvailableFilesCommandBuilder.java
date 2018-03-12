package edu.networkcusp.chatbox;

public class AvailableFilesCommandBuilder {
    private HangIn withMi;

    public AvailableFilesCommandBuilder assignWithMi(HangIn withMi) {
        this.withMi = withMi;
        return this;
    }

    public AvailableFilesCommand createAvailableFilesCommand() {
        return new AvailableFilesCommand(withMi);
    }
}