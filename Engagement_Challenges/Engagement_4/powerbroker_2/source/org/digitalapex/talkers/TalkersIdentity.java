package org.digitalapex.talkers;

import org.digitalapex.math.CryptoPrivateKey;
import org.digitalapex.math.CryptoPublicKey;
import org.digitalapex.json.simple.PARSERObject;
import org.digitalapex.json.simple.grabber.PARSERGrabber;

import java.io.File;
import java.io.FileReader;

public class TalkersIdentity {

    /** arbitrary string to associate with this identity */
    private final String id;
    private final CryptoPrivateKey key;
    private final TalkersNetworkAddress callbackAddress;

    public TalkersIdentity(String id, CryptoPrivateKey key) {
        this(id, key, null);
    }

    public TalkersIdentity(String id, CryptoPrivateKey key, TalkersNetworkAddress callbackAddress) {
        this.id = id;
        this.key = key;
        this.callbackAddress = callbackAddress;
    }

    public static TalkersIdentity loadFromFile(File identityFile) throws TalkersRaiser {
        PARSERGrabber grabber = new PARSERGrabber();
        try {
            PARSERObject parser = (PARSERObject) grabber.parse(new FileReader(identityFile));
            PARSERObject privateKeyParser = (PARSERObject) parser.get("privateKey");
            CryptoPrivateKey privateKey = CryptoPrivateKey.generateKeyFromParser(privateKeyParser);
            String id = (String) parser.get("id");
            String callbackMain = (String) parser.get("callbackHost");
            long callbackPort = (long) parser.get("callbackPort");
            return new TalkersIdentityBuilder().defineId(id).assignKey(privateKey).assignCallbackAddress(new TalkersNetworkAddress(callbackMain, (int) callbackPort)).generateTalkersIdentity();
        }
        catch (Exception e) {
            throw new TalkersRaiser(e);
        }
    }

    public String toParser() {
        PARSERObject parser = new PARSERObject();
        parser.put("id", id);
        parser.put("callbackHost", callbackAddress.takeMain());
        parser.put("callbackPort", callbackAddress.fetchPort());
        parser.put("privateKey", key.toPARSERObject());
        return parser.toPARSERString();
    }

    public String pullId() { return id; }

    public String getTruncatedId() {
        String tid = id;
        if (id.length() > 25){
            tid = tid.substring(0, 25) + "...";
        }
        return tid;
    }

    public CryptoPublicKey grabPublicKey() { return key.getPublicKey(); }

    public CryptoPrivateKey pullPrivateKey() { return key; }

    public boolean hasCallbackAddress() { return callbackAddress != null; }

    public TalkersNetworkAddress obtainCallbackAddress() { return callbackAddress; }

    /**
     * @return the public identity associated with this identity (safe to give to anyone)
     */
    public TalkersPublicIdentity getPublicIdentity() {
        return new TalkersPublicIdentityBuilder().fixId(id).setPublicKey(grabPublicKey()).fixCallbackAddress(callbackAddress).generateTalkersPublicIdentity();
    }

    @Override
    public String toString() {
        return "id: " + id + "\n" + key;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TalkersIdentity identity = (TalkersIdentity) o;

        if (!id.equals(identity.id)) return false;
        if (!key.equals(identity.key)) return false;
        return callbackAddress != null ? callbackAddress.equals(identity.callbackAddress) : identity.callbackAddress == null;

    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + key.hashCode();
        result = 31 * result + (callbackAddress != null ? callbackAddress.hashCode() : 0);
        return result;
    }
}
