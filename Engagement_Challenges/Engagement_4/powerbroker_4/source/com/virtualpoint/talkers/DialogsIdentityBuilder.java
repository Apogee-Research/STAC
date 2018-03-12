package com.virtualpoint.talkers;

import com.virtualpoint.numerical.CipherPrivateKey;

public class DialogsIdentityBuilder {
    private String id;
    private DialogsNetworkAddress callbackAddress = null;
    private CipherPrivateKey key;

    public DialogsIdentityBuilder fixId(String id) {
        this.id = id;
        return this;
    }

    public DialogsIdentityBuilder defineCallbackAddress(DialogsNetworkAddress callbackAddress) {
        this.callbackAddress = callbackAddress;
        return this;
    }

    public DialogsIdentityBuilder setKey(CipherPrivateKey key) {
        this.key = key;
        return this;
    }

    public DialogsIdentity composeDialogsIdentity() {
        return new DialogsIdentity(id, key, callbackAddress);
    }
}