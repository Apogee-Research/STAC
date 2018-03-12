package net.roboticapex.senderReceivers;

import net.roboticapex.algorithm.RsaPublicKey;
import net.roboticapex.parser.simple.PARSINGObject;
import net.roboticapex.parser.simple.grabber.PARSINGParser;
import net.roboticapex.parser.simple.grabber.ParseDeviation;

public final class SenderReceiversPublicIdentity implements Comparable<SenderReceiversPublicIdentity>{

    /** arbitrary string to associate with this identity */
    private final String id;
    private final RsaPublicKey publicKey;
    private final SenderReceiversNetworkAddress callbackAddress;

    public SenderReceiversPublicIdentity(String id, RsaPublicKey publicKey){
        this(id, publicKey, null);
    }

    public SenderReceiversPublicIdentity(String id, RsaPublicKey publicKey, SenderReceiversNetworkAddress callbackAddress) {
        this.id = id;
        this.publicKey = publicKey;
        this.callbackAddress = callbackAddress;
    }

    public static SenderReceiversPublicIdentity fromParsing(String parsingString) throws SenderReceiversDeviation {
        PARSINGParser parser = new PARSINGParser();
        try {
            return fromParsing((PARSINGObject) parser.parse(parsingString));
        } catch (ParseDeviation e) {
            throw new SenderReceiversDeviation(e);
        }
    }

    public static SenderReceiversPublicIdentity fromParsing(PARSINGObject parsing) {
        String id = (String) parsing.get("id");
        String callbackPlace = (String) parsing.get("callbackHost");
        long callbackPort = (long) parsing.get("callbackPort");
        RsaPublicKey publicKey = RsaPublicKey.fromJson((PARSINGObject) parsing.get("publicKey"));

        return new SenderReceiversPublicIdentity(id, publicKey, new SenderReceiversNetworkAddress(callbackPlace, (int)callbackPort));
    }

    public String obtainId() { return id; }

    public String pullTruncatedId() {
        String tid = id;
        if (id.length() > 25){
            tid = tid.substring(0, 25) + "...";
        }
        return tid;
    }

    public RsaPublicKey pullPublicKey() { return publicKey; }

    public SenderReceiversNetworkAddress pullCallbackAddress() {
        return callbackAddress;
    }

    public boolean hasCallbackAddress() { return callbackAddress != null; }

    @Override
    public String toString() {
        return "id: " + id + "\n" + publicKey;
    }

    public String toParsing() {
        return toPARSINGObject().toPARSINGString();
    }

    public PARSINGObject toPARSINGObject() {
        PARSINGObject parsing = new PARSINGObject();
        parsing.put("id", id);
        parsing.put("callbackHost", callbackAddress.getPlace());
        parsing.put("callbackPort", callbackAddress.pullPort());
        parsing.put("publicKey", publicKey.toJSONObject());
        return parsing;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SenderReceiversPublicIdentity that = (SenderReceiversPublicIdentity) o;

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
    public int compareTo(SenderReceiversPublicIdentity other){
    	return this.toVerboseString().compareTo(other.toVerboseString());
    }
}
