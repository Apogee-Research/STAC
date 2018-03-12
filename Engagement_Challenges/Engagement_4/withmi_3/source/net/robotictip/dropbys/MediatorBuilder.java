package net.robotictip.dropbys;

import net.robotictip.display.Display;

import java.io.File;

public class MediatorBuilder {
    private ConversationManager discussionManager;
    private File incomingDir;
    private Display display;
    private HangIn withMi;

    public MediatorBuilder fixDiscussionManager(ConversationManager discussionManager) {
        this.discussionManager = discussionManager;
        return this;
    }

    public MediatorBuilder defineIncomingDir(File incomingDir) {
        this.incomingDir = incomingDir;
        return this;
    }

    public MediatorBuilder defineDisplay(Display display) {
        this.display = display;
        return this;
    }

    public MediatorBuilder fixWithMi(HangIn withMi) {
        this.withMi = withMi;
        return this;
    }

    public Mediator generateMediator() {
        return new Mediator(withMi, discussionManager, incomingDir, display);
    }
}