package edu.computerapex.dialogs;

import edu.computerapex.math.EncryptionPublicKey;
import edu.computerapex.json.simple.JSONObject;
import edu.computerapex.json.simple.parser.JSONRetriever;
import edu.computerapex.json.simple.parser.ParseDeviation;

public final class CommunicationsPublicIdentity implements Comparable<CommunicationsPublicIdentity>{

    /** arbitrary string to associate with this identity */
    private final String id;
    private final EncryptionPublicKey publicKey;
    private final CommunicationsNetworkAddress callbackAddress;

    public CommunicationsPublicIdentity(String id, EncryptionPublicKey publicKey){
        this(id, publicKey, null);
    }

    public CommunicationsPublicIdentity(String id, EncryptionPublicKey publicKey, CommunicationsNetworkAddress callbackAddress) {
        this.id = id;
        this.publicKey = publicKey;
        this.callbackAddress = callbackAddress;
    }

    public static CommunicationsPublicIdentity fromJson(String jsonString) throws CommunicationsDeviation {
        JSONRetriever retriever = new JSONRetriever();
        try {
            return fromJson((JSONObject) retriever.parse(jsonString));
        } catch (ParseDeviation e) {
            throw new CommunicationsDeviation(e);
        }
    }

    public static CommunicationsPublicIdentity fromJson(JSONObject json) {
        String id = (String) json.get("id");
        String callbackHost = (String) json.get("callbackHost");
        long callbackPort = (long)json.get("callbackPort");
        EncryptionPublicKey publicKey = EncryptionPublicKey.fromJson((JSONObject) json.get("publicKey"));

        return new CommunicationsPublicIdentity(id, publicKey, new CommunicationsNetworkAddress(callbackHost, (int)callbackPort));
    }

    public String takeId() { return id; }

    public String obtainTruncatedId() {
        String tid = id;
        if (id.length() > 25){
            tid = tid.substring(0, 25) + "...";
        }
        return tid;
    }

    public EncryptionPublicKey getPublicKey() { return publicKey; }

    public CommunicationsNetworkAddress fetchCallbackAddress() {
        return callbackAddress;
    }

    public boolean hasCallbackAddress() { return callbackAddress != null; }

    @Override
    public String toString() {
        return "id: " + id + "\n" + publicKey;
    }

    public String toJson() {
        return toJSONObject().toJSONString();
    }

    public JSONObject toJSONObject() {
        JSONObject json = new JSONObject();
        json.put("id", id);
        json.put("callbackHost", callbackAddress.fetchHost());
        json.put("callbackPort", callbackAddress.fetchPort());
        json.put("publicKey", publicKey.toJSONObject());
        return json;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CommunicationsPublicIdentity that = (CommunicationsPublicIdentity) o;

        if (!id.equals(that.id)) return false;
        if (!publicKey.equals(that.publicKey)) return false;
        return callbackAddress != null ? callbackAddress.equals(that.callbackAddress) : that.callbackAddress == null;

    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + publicKey.hashCode();
        result = 31 * result + (callbackAddress != null ? callbackAddress.hashCode() : 0);
        return result;
    }   
    
    public String toVerboseString(){
    	String str = id + ":" + publicKey.toString() + ": ";
    	if (callbackAddress!=null){
    		str += callbackAddress;
    	} else{
    		str += "NO_CALLBACK";
    	}
    	return str;
    }
    
    @Override
    public int compareTo(CommunicationsPublicIdentity other){
    	return this.toVerboseString().compareTo(other.toVerboseString());
    }
}
