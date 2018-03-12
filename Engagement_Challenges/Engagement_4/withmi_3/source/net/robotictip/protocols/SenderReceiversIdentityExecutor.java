package net.robotictip.protocols;

import net.robotictip.numerical.CipherPrivateKey;
import net.robotictip.numerical.RsaPublicKey;

public class SenderReceiversIdentityExecutor {
    private final SenderReceiversIdentity senderReceiversIdentity;

    public SenderReceiversIdentityExecutor(SenderReceiversIdentity senderReceiversIdentity) {
        this.senderReceiversIdentity = senderReceiversIdentity;
    }

    public String obtainTruncatedId() {
        String tid = senderReceiversIdentity.fetchId();
        if (senderReceiversIdentity.fetchId().length() > 25) {
            tid = tid.substring(0, 25) + "...";
        }
        return tid;
    }

    public RsaPublicKey takePublicKey() {
        return senderReceiversIdentity.fetchKey().obtainPublicKey();
    }

    public CipherPrivateKey grabPrivateKey() {
        return senderReceiversIdentity.fetchKey();
    }

    /**
     * @return the public identity associated with this identity (safe to give to anyone)
     */
    public SenderReceiversPublicIdentity obtainPublicIdentity() {
        return new SenderReceiversPublicIdentity(senderReceiversIdentity.fetchId(), takePublicKey(), senderReceiversIdentity.takeCallbackAddress());
    }
}