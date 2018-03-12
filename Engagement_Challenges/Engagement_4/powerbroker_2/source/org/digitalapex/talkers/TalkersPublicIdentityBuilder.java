package org.digitalapex.talkers;

import org.digitalapex.math.CryptoPublicKey;

public class TalkersPublicIdentityBuilder {
    private CryptoPublicKey publicKey;
    private String id;
    private TalkersNetworkAddress callbackAddress = null;

    public TalkersPublicIdentityBuilder setPublicKey(CryptoPublicKey publicKey) {
        this.publicKey = publicKey;
        return this;
    }

    public TalkersPublicIdentityBuilder fixId(String id) {
        this.id = id;
        return this;
    }

    public TalkersPublicIdentityBuilder fixCallbackAddress(TalkersNetworkAddress callbackAddress) {
        this.callbackAddress = callbackAddress;
        return this;
    }

    public TalkersPublicIdentity generateTalkersPublicIdentity() {
        return new TalkersPublicIdentity(id, publicKey, callbackAddress);
    }
}