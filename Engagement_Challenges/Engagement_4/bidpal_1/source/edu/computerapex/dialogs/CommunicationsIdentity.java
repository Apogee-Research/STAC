package edu.computerapex.dialogs;

import edu.computerapex.math.EncryptionPrivateKey;
import edu.computerapex.math.EncryptionPublicKey;
import edu.computerapex.json.simple.JSONObject;
import edu.computerapex.json.simple.parser.JSONRetriever;

import java.io.File;
import java.io.FileReader;

public class CommunicationsIdentity {

    /** arbitrary string to associate with this identity */
    private final String id;
    private final EncryptionPrivateKey key;
    private final CommunicationsNetworkAddress callbackAddress;

    public CommunicationsIdentity(String id, EncryptionPrivateKey key) {
        this(id, key, null);
    }

    public CommunicationsIdentity(String id, EncryptionPrivateKey key, CommunicationsNetworkAddress callbackAddress) {
        this.id = id;
        this.key = key;
        this.callbackAddress = callbackAddress;
    }

    public static CommunicationsIdentity loadFromFile(File identityFile) throws CommunicationsDeviation {
        JSONRetriever retriever = new JSONRetriever();
        try {
            JSONObject json = (JSONObject) retriever.parse(new FileReader(identityFile));
            JSONObject privateKeyJson = (JSONObject) json.get("privateKey");
            EncryptionPrivateKey privateKey = EncryptionPrivateKey.generateKeyFromJson(privateKeyJson);
            String id = (String) json.get("id");
            String callbackHost = (String) json.get("callbackHost");
            long callbackPort = (long)json.get("callbackPort");
            return new CommunicationsIdentity(id, privateKey, new CommunicationsNetworkAddress(callbackHost, (int)callbackPort));
        }
        catch (Exception e) {
            throw new CommunicationsDeviation(e);
        }
    }

    public String toJson() {
        JSONObject json = new JSONObject();
        json.put("id", id);
        json.put("callbackHost", callbackAddress.fetchHost());
        json.put("callbackPort", callbackAddress.fetchPort());
        json.put("privateKey", key.toJSONObject());
        return json.toJSONString();
    }

    public String obtainId() { return id; }

    public String pullTruncatedId() {
        String tid = id;
        if (id.length() > 25){
            tid = tid.substring(0, 25) + "...";
        }
        return tid;
    }

    public EncryptionPublicKey grabPublicKey() { return key.getPublicKey(); }

    public EncryptionPrivateKey fetchPrivateKey() { return key; }

    public boolean hasCallbackAddress() { return callbackAddress != null; }

    public CommunicationsNetworkAddress pullCallbackAddress() { return callbackAddress; }

    /**
     * @return the public identity associated with this identity (safe to give to anyone)
     */
    public CommunicationsPublicIdentity takePublicIdentity() {
        return new CommunicationsPublicIdentity(id, grabPublicKey(), callbackAddress);
    }

    @Override
    public String toString() {
        return "id: " + id + "\n" + key;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CommunicationsIdentity identity = (CommunicationsIdentity) o;

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
