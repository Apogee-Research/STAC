package edu.networkcusp.chatbox;

public class DeliverMessageLineGuideBuilder {
    private HangIn withMi;

    public DeliverMessageLineGuideBuilder defineWithMi(HangIn withMi) {
        this.withMi = withMi;
        return this;
    }

    public DeliverMessageLineGuide createDeliverMessageLineGuide() {
        return new DeliverMessageLineGuide(withMi);
    }
}