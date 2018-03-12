package com.digitalpoint.togethertalk;

public class SendFileCompleterBuilder {
    private HangIn withMi;

    public SendFileCompleterBuilder assignWithMi(HangIn withMi) {
        this.withMi = withMi;
        return this;
    }

    public SendFileCompleter makeSendFileCompleter() {
        return new SendFileCompleter(withMi);
    }
}