package org.techpoint.communications;

import org.techpoint.mathematic.CryptoSystemPublicKey;

public class CommsPublicIdentityBuilder {
    private CryptoSystemPublicKey publicKey;
    private String id;
    private CommsNetworkAddress callbackAddress = null;

    public CommsPublicIdentityBuilder definePublicKey(CryptoSystemPublicKey publicKey) {
        this.publicKey = publicKey;
        return this;
    }

    public CommsPublicIdentityBuilder assignId(String id) {
        this.id = id;
        return this;
    }

    public CommsPublicIdentityBuilder fixCallbackAddress(CommsNetworkAddress callbackAddress) {
        this.callbackAddress = callbackAddress;
        return this;
    }

    public CommsPublicIdentity composeCommsPublicIdentity() {
        return new CommsPublicIdentity(id, publicKey, callbackAddress);
    }
}