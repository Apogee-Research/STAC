package edu.networkcusp.chatbox;

public class ConversationConductorBuilder {
    private HangIn withMi;

    public ConversationConductorBuilder assignWithMi(HangIn withMi) {
        this.withMi = withMi;
        return this;
    }

    public ConversationManager createConversationConductor() {
        return new ConversationManager(withMi);
    }
}