package net.roboticapex.senderReceivers;

import net.roboticapex.algorithm.CipherPrivateKey;
import net.roboticapex.algorithm.RsaPublicKey;
import net.roboticapex.parser.simple.PARSINGObject;
import net.roboticapex.parser.simple.grabber.PARSINGParser;

import java.io.File;
import java.io.FileReader;

public class SenderReceiversIdentity {

    /** arbitrary string to associate with this identity */
    private final String id;
    private final CipherPrivateKey key;
    private final SenderReceiversNetworkAddress callbackAddress;

    public SenderReceiversIdentity(String id, CipherPrivateKey key) {
        this(id, key, null);
    }

    public SenderReceiversIdentity(String id, CipherPrivateKey key, SenderReceiversNetworkAddress callbackAddress) {
        this.id = id;
        this.key = key;
        this.callbackAddress = callbackAddress;
    }

    public static SenderReceiversIdentity loadFromFile(File identityFile) throws SenderReceiversDeviation {
        PARSINGParser parser = new PARSINGParser();
        try {
            PARSINGObject parsing = (PARSINGObject) parser.parse(new FileReader(identityFile));
            PARSINGObject privateKeyParsing = (PARSINGObject) parsing.get("privateKey");
            CipherPrivateKey privateKey = CipherPrivateKey.makeKeyFromParsing(privateKeyParsing);
            String id = (String) parsing.get("id");
            String callbackPlace = (String) parsing.get("callbackHost");
            long callbackPort = (long) parsing.get("callbackPort");
            return new SenderReceiversIdentity(id, privateKey, new SenderReceiversNetworkAddress(callbackPlace, (int)callbackPort));
        }
        catch (Exception e) {
            throw new SenderReceiversDeviation(e);
        }
    }

    public String toParsing() {
        PARSINGObject parsing = new PARSINGObject();
        parsing.put("id", id);
        parsing.put("callbackHost", callbackAddress.getPlace());
        parsing.put("callbackPort", callbackAddress.pullPort());
        parsing.put("privateKey", key.toPARSINGObject());
        return parsing.toPARSINGString();
    }

    public String obtainId() { return id; }

    public String fetchTruncatedId() {
        String tid = id;
        if (id.length() > 25){
            tid = tid.substring(0, 25) + "...";
        }
        return tid;
    }

    public RsaPublicKey fetchPublicKey() { return key.pullPublicKey(); }

    public CipherPrivateKey getPrivateKey() { return key; }

    public boolean hasCallbackAddress() { return callbackAddress != null; }

    public SenderReceiversNetworkAddress getCallbackAddress() { return callbackAddress; }

    /**
     * @return the public identity associated with this identity (safe to give to anyone)
     */
    public SenderReceiversPublicIdentity pullPublicIdentity() {
        return new SenderReceiversPublicIdentity(id, fetchPublicKey(), callbackAddress);
    }

    @Override
    public String toString() {
        return "id: " + id + "\n" + key;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SenderReceiversIdentity identity = (SenderReceiversIdentity) o;

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
