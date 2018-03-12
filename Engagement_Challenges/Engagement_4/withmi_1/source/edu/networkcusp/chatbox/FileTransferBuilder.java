package edu.networkcusp.chatbox;

public class FileTransferBuilder {
    private HangIn withMi;

    public FileTransferBuilder setWithMi(HangIn withMi) {
        this.withMi = withMi;
        return this;
    }

    public FileTransfer createFileTransfer() {
        return new FileTransfer(withMi);
    }
}