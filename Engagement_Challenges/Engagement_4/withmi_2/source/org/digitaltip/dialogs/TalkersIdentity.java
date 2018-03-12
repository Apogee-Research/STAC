package org.digitaltip.dialogs;

import org.digitaltip.mathematic.CryptoSystemPrivateKey;
import org.digitaltip.mathematic.CryptoSystemPublicKey;
import org.digitaltip.objnote.simple.JACKSONObject;
import org.digitaltip.objnote.simple.grabber.JACKSONGrabber;

import java.io.File;
import java.io.FileReader;

public class TalkersIdentity {

    /** arbitrary string to associate with this identity */
    private final String id;
    private final CryptoSystemPrivateKey key;
    private final TalkersNetworkAddress callbackAddress;

    public TalkersIdentity(String id, CryptoSystemPrivateKey key) {
        this(id, key, null);
    }

    public TalkersIdentity(String id, CryptoSystemPrivateKey key, TalkersNetworkAddress callbackAddress) {
        this.id = id;
        this.key = key;
        this.callbackAddress = callbackAddress;
    }

    public static TalkersIdentity loadFromFile(File identityFile) throws TalkersDeviation {
        JACKSONGrabber grabber = new JACKSONGrabber();
        try {
            JACKSONObject jackson = (JACKSONObject) grabber.parse(new FileReader(identityFile));
            JACKSONObject privateKeyJackson = (JACKSONObject) jackson.get("privateKey");
            CryptoSystemPrivateKey privateKey = CryptoSystemPrivateKey.makeKeyFromJackson(privateKeyJackson);
            String id = (String) jackson.get("id");
            String callbackMain = (String) jackson.get("callbackHost");
            long callbackPort = (long) jackson.get("callbackPort");
            return new TalkersIdentityBuilder().defineId(id).assignKey(privateKey).defineCallbackAddress(new TalkersNetworkAddress(callbackMain, (int) callbackPort)).makeTalkersIdentity();
        }
        catch (Exception e) {
            throw new TalkersDeviation(e);
        }
    }

    public String toJackson() {
        JACKSONObject jackson = new JACKSONObject();
        jackson.put("id", id);
        jackson.put("callbackHost", callbackAddress.grabMain());
        jackson.put("callbackPort", callbackAddress.fetchPort());
        jackson.put("privateKey", key.toJACKSONObject());
        return jackson.toJACKSONString();
    }

    public String grabId() { return id; }

    public String takeTruncatedId() {
        String tid = id;
        if (id.length() > 25){
            tid = tid.substring(0, 25) + "...";
        }
        return tid;
    }

    public CryptoSystemPublicKey grabPublicKey() { return key.obtainPublicKey(); }

    public CryptoSystemPrivateKey grabPrivateKey() { return key; }

    public boolean hasCallbackAddress() { return callbackAddress != null; }

    public TalkersNetworkAddress getCallbackAddress() { return callbackAddress; }

    /**
     * @return the public identity associated with this identity (safe to give to anyone)
     */
    public TalkersPublicIdentity grabPublicIdentity() {
        return new TalkersPublicIdentity(id, grabPublicKey(), callbackAddress);
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
