package org.digitaltip.chatroom;

public class ListConferencesCommandBuilder {
    private HangIn withMi;

    public ListConferencesCommandBuilder assignWithMi(HangIn withMi) {
        this.withMi = withMi;
        return this;
    }

    public ListConferencesCommand makeListConferencesCommand() {
        return new ListConferencesCommand(withMi);
    }
}