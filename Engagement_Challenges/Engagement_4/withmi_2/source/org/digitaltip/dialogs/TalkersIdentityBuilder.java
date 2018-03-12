package org.digitaltip.dialogs;

import org.digitaltip.mathematic.CryptoSystemPrivateKey;

public class TalkersIdentityBuilder {
    private String id;
    private TalkersNetworkAddress callbackAddress = null;
    private CryptoSystemPrivateKey key;

    public TalkersIdentityBuilder defineId(String id) {
        this.id = id;
        return this;
    }

    public TalkersIdentityBuilder defineCallbackAddress(TalkersNetworkAddress callbackAddress) {
        this.callbackAddress = callbackAddress;
        return this;
    }

    public TalkersIdentityBuilder assignKey(CryptoSystemPrivateKey key) {
        this.key = key;
        return this;
    }

    public TalkersIdentity makeTalkersIdentity() {
        return new TalkersIdentity(id, key, callbackAddress);
    }
}