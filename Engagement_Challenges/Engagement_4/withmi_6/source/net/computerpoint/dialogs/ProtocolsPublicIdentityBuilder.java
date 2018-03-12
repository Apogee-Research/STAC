package net.computerpoint.dialogs;

import net.computerpoint.numerical.RsaPublicKey;

public class ProtocolsPublicIdentityBuilder {
    private RsaPublicKey publicKey;
    private String id;
    private ProtocolsNetworkAddress callbackAddress = null;

    public ProtocolsPublicIdentityBuilder fixPublicKey(RsaPublicKey publicKey) {
        this.publicKey = publicKey;
        return this;
    }

    public ProtocolsPublicIdentityBuilder setId(String id) {
        this.id = id;
        return this;
    }

    public ProtocolsPublicIdentityBuilder assignCallbackAddress(ProtocolsNetworkAddress callbackAddress) {
        this.callbackAddress = callbackAddress;
        return this;
    }

    public ProtocolsPublicIdentity formProtocolsPublicIdentity() {
        return new ProtocolsPublicIdentity(id, publicKey, callbackAddress);
    }
}