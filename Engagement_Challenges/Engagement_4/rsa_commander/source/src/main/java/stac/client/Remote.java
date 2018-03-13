package stac.client;

import stac.crypto.PrivateKey;
import stac.crypto.PublicKey;

/**
 * Remotes represent any information we need to know at the handler level about the
 * remote user that we don't already have from the session level.
 */
public class Remote {
    private PublicKey publicKey;

    public Remote() {
    }

    public Remote(PrivateKey senderKey) {
        publicKey = senderKey.toPublicKey();
    }

    public Remote(PublicKey senderKey) {
        publicKey = senderKey;
    }

    public PublicKey getKey() {
        return publicKey;
    }

    public void setKey(PublicKey key) {
        publicKey = key;
    }
}
