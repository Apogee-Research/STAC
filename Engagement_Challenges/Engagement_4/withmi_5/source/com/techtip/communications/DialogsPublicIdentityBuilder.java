package com.techtip.communications;

import com.techtip.numerical.CipherPublicKey;

public class DialogsPublicIdentityBuilder {
    private CipherPublicKey publicKey;
    private String id;
    private DialogsNetworkAddress callbackAddress = null;

    public DialogsPublicIdentityBuilder assignPublicKey(CipherPublicKey publicKey) {
        this.publicKey = publicKey;
        return this;
    }

    public DialogsPublicIdentityBuilder fixId(String id) {
        this.id = id;
        return this;
    }

    public DialogsPublicIdentityBuilder setCallbackAddress(DialogsNetworkAddress callbackAddress) {
        this.callbackAddress = callbackAddress;
        return this;
    }

    public DialogsPublicIdentity formDialogsPublicIdentity() {
        return new DialogsPublicIdentity(id, publicKey, callbackAddress);
    }
}