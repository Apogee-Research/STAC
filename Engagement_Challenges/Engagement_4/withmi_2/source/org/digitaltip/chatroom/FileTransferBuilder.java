package org.digitaltip.chatroom;

public class FileTransferBuilder {
    private HangIn withMi;

    public FileTransferBuilder setWithMi(HangIn withMi) {
        this.withMi = withMi;
        return this;
    }

    public FileTransfer makeFileTransfer() {
        return new FileTransfer(withMi);
    }
}