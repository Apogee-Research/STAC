package org.digitaltip.dialogs;

import org.digitaltip.mathematic.CryptoSystemPublicKey;
import org.digitaltip.objnote.simple.JACKSONObject;
import org.digitaltip.objnote.simple.grabber.JACKSONGrabber;
import org.digitaltip.objnote.simple.grabber.ParseDeviation;

public final class TalkersPublicIdentity implements Comparable<TalkersPublicIdentity>{

    /** arbitrary string to associate with this identity */
    private final String id;
    private final CryptoSystemPublicKey publicKey;
    private final TalkersNetworkAddress callbackAddress;

    public TalkersPublicIdentity(String id, CryptoSystemPublicKey publicKey){
        this(id, publicKey, null);
    }

    public TalkersPublicIdentity(String id, CryptoSystemPublicKey publicKey, TalkersNetworkAddress callbackAddress) {
        this.id = id;
        this.publicKey = publicKey;
        this.callbackAddress = callbackAddress;
    }

    public static TalkersPublicIdentity fromJackson(String jacksonString) throws TalkersDeviation {
        JACKSONGrabber grabber = new JACKSONGrabber();
        try {
            return fromJackson((JACKSONObject) grabber.parse(jacksonString));
        } catch (ParseDeviation e) {
            throw new TalkersDeviation(e);
        }
    }

    public static TalkersPublicIdentity fromJackson(JACKSONObject jackson) {
        String id = (String) jackson.get("id");
        String callbackMain = (String) jackson.get("callbackHost");
        long callbackPort = (long) jackson.get("callbackPort");
        CryptoSystemPublicKey publicKey = ((JACKSONObject) jackson.get("publicKey")).fromJackson();

        return new TalkersPublicIdentity(id, publicKey, new TalkersNetworkAddress(callbackMain, (int)callbackPort));
    }

    public String takeId() { return id; }

    public String getTruncatedId() {
        String tid = id;
        if (id.length() > 25){
            tid = tid.substring(0, 25) + "...";
        }
        return tid;
    }

    public CryptoSystemPublicKey grabPublicKey() { return publicKey; }

    public TalkersNetworkAddress grabCallbackAddress() {
        return callbackAddress;
    }

    public boolean hasCallbackAddress() { return callbackAddress != null; }

    @Override
    public String toString() {
        return "id: " + id + "\n" + publicKey;
    }

    public String toJackson() {
        return toJACKSONObject().toJACKSONString();
    }

    public JACKSONObject toJACKSONObject() {
        JACKSONObject jackson = new JACKSONObject();
        jackson.put("id", id);
        jackson.put("callbackHost", callbackAddress.grabMain());
        jackson.put("callbackPort", callbackAddress.fetchPort());
        jackson.put("publicKey", publicKey.toJACKSONObject());
        return jackson;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TalkersPublicIdentity that = (TalkersPublicIdentity) o;

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
    public int compareTo(TalkersPublicIdentity other){
    	return this.toVerboseString().compareTo(other.toVerboseString());
    }
}
