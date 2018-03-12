package org.digitalapex.talkers;

import org.digitalapex.math.CryptoPrivateKey;

public class TalkersIdentityBuilder {
    private String id;
    private TalkersNetworkAddress callbackAddress = null;
    private CryptoPrivateKey key;

    public TalkersIdentityBuilder defineId(String id) {
        this.id = id;
        return this;
    }

    public TalkersIdentityBuilder assignCallbackAddress(TalkersNetworkAddress callbackAddress) {
        this.callbackAddress = callbackAddress;
        return this;
    }

    public TalkersIdentityBuilder assignKey(CryptoPrivateKey key) {
        this.key = key;
        return this;
    }

    public TalkersIdentity generateTalkersIdentity() {
        return new TalkersIdentity(id, key, callbackAddress);
    }
}