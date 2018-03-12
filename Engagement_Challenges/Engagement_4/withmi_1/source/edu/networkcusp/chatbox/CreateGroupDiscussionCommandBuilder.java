package edu.networkcusp.chatbox;

public class CreateGroupDiscussionCommandBuilder {
    private HangIn withMi;

    public CreateGroupDiscussionCommandBuilder assignWithMi(HangIn withMi) {
        this.withMi = withMi;
        return this;
    }

    public CreateGroupDiscussionCommand createCreateGroupDiscussionCommand() {
        return new CreateGroupDiscussionCommand(withMi);
    }
}