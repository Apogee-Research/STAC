package com.digitalpoint.togethertalk;

import com.digitalpoint.terminal.Console;

import java.io.File;

public class DispatcherBuilder {
    private File incomingDir;
    private Console command;
    private ConversationManager conferenceManager;
    private HangIn withMi;

    public DispatcherBuilder fixIncomingDir(File incomingDir) {
        this.incomingDir = incomingDir;
        return this;
    }

    public DispatcherBuilder defineCommand(Console command) {
        this.command = command;
        return this;
    }

    public DispatcherBuilder defineConferenceManager(ConversationManager conferenceManager) {
        this.conferenceManager = conferenceManager;
        return this;
    }

    public DispatcherBuilder assignWithMi(HangIn withMi) {
        this.withMi = withMi;
        return this;
    }

    public Dispatcher makeDispatcher() {
        return new Dispatcher(withMi, conferenceManager, incomingDir, command);
    }
}