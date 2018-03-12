package com.techtip.chatbox;

public class EnterForumCompleterBuilder {
    private DropBy withMi;

    public EnterForumCompleterBuilder defineWithMi(DropBy withMi) {
        this.withMi = withMi;
        return this;
    }

    public EnterForumCompleter formEnterForumCompleter() {
        return new EnterForumCompleter(withMi);
    }
}