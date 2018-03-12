package com.techtip.communications;

public class DialogsClientBuilder {
    private DialogsIdentity identity;
    private DialogsHandler dialogsHandler;

    public DialogsClientBuilder fixIdentity(DialogsIdentity identity) {
        this.identity = identity;
        return this;
    }

    public DialogsClientBuilder defineDialogsHandler(DialogsHandler dialogsHandler) {
        this.dialogsHandler = dialogsHandler;
        return this;
    }

    public DialogsClient formDialogsClient() {
        return new DialogsClient(dialogsHandler, identity);
    }
}