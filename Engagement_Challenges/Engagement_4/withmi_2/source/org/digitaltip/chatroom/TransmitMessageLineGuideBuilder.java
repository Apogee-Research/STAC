package org.digitaltip.chatroom;

public class TransmitMessageLineGuideBuilder {
    private HangIn withMi;

    public TransmitMessageLineGuideBuilder defineWithMi(HangIn withMi) {
        this.withMi = withMi;
        return this;
    }

    public TransmitMessageLineGuide makeTransmitMessageLineGuide() {
        return new TransmitMessageLineGuide(withMi);
    }
}