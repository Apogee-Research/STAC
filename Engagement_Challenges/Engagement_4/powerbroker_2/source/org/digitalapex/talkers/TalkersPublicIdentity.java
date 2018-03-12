package org.digitalapex.talkers;

import org.digitalapex.math.CryptoPublicKey;
import org.digitalapex.json.simple.PARSERObject;
import org.digitalapex.json.simple.grabber.PARSERGrabber;
import org.digitalapex.json.simple.grabber.ParseRaiser;

public final class TalkersPublicIdentity implements Comparable<TalkersPublicIdentity>{

    /** arbitrary string to associate with this identity */
    private final String id;
    private final CryptoPublicKey publicKey;
    private final TalkersNetworkAddress callbackAddress;

    public TalkersPublicIdentity(String id, CryptoPublicKey publicKey){
        this(id, publicKey, null);
    }

    public TalkersPublicIdentity(String id, CryptoPublicKey publicKey, TalkersNetworkAddress callbackAddress) {
        this.id = id;
        this.publicKey = publicKey;
        this.callbackAddress = callbackAddress;
    }

    public static TalkersPublicIdentity fromParser(String parserString) throws TalkersRaiser {
        PARSERGrabber grabber = new PARSERGrabber();
        try {
            return fromParser((PARSERObject) grabber.parse(parserString));
        } catch (ParseRaiser e) {
            throw new TalkersRaiser(e);
        }
    }

    public static TalkersPublicIdentity fromParser(PARSERObject parser) {
        String id = (String) parser.get("id");
        String callbackMain = (String) parser.get("callbackHost");
        long callbackPort = (long) parser.get("callbackPort");
        CryptoPublicKey publicKey = ((PARSERObject) parser.get("publicKey")).fromParser();

        return new TalkersPublicIdentityBuilder().fixId(id).setPublicKey(publicKey).fixCallbackAddress(new TalkersNetworkAddress(callbackMain, (int) callbackPort)).generateTalkersPublicIdentity();
    }

    public String getId() { return id; }

    public String grabTruncatedId() {
        String tid = id;
        if (id.length() > 25){
            tid = tid.substring(0, 25) + "...";
        }
        return tid;
    }

    public CryptoPublicKey fetchPublicKey() { return publicKey; }

    public TalkersNetworkAddress pullCallbackAddress() {
        return callbackAddress;
    }

    public boolean hasCallbackAddress() { return callbackAddress != null; }

    @Override
    public String toString() {
        return "id: " + id + "\n" + publicKey;
    }

    public String toParser() {
        return toPARSERObject().toPARSERString();
    }

    public PARSERObject toPARSERObject() {
        PARSERObject parser = new PARSERObject();
        parser.put("id", id);
        parser.put("callbackHost", callbackAddress.takeMain());
        parser.put("callbackPort", callbackAddress.fetchPort());
        parser.put("publicKey", publicKey.toPARSERObject());
        return parser;
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
