package com.digitalpoint.dialogs;

import com.digitalpoint.math.CryptoPrivateKey;

public class SenderReceiversIdentityBuilder {
    private String id;
    private SenderReceiversNetworkAddress callbackAddress = null;
    private CryptoPrivateKey key;

    public SenderReceiversIdentityBuilder assignId(String id) {
        this.id = id;
        return this;
    }

    public SenderReceiversIdentityBuilder defineCallbackAddress(SenderReceiversNetworkAddress callbackAddress) {
        this.callbackAddress = callbackAddress;
        return this;
    }

    public SenderReceiversIdentityBuilder fixKey(CryptoPrivateKey key) {
        this.key = key;
        return this;
    }

    public SenderReceiversIdentity makeSenderReceiversIdentity() {
        return new SenderReceiversIdentity(id, key, callbackAddress);
    }
}