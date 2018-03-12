package org.techpoint.communications;

import org.techpoint.mathematic.CryptoSystemPublicKey;
import org.techpoint.parsing.simple.PARTObject;
import org.techpoint.parsing.simple.reader.PARTReader;
import org.techpoint.parsing.simple.reader.ParseRaiser;

public final class CommsPublicIdentity  implements Comparable<CommsPublicIdentity>{

    /** arbitrary string to associate with this identity */
    private final String id;
    private final CryptoSystemPublicKey publicKey;
    private final CommsNetworkAddress callbackAddress;

    public CommsPublicIdentity(String id, CryptoSystemPublicKey publicKey){
        this(id, publicKey, null);
    }

    public CommsPublicIdentity(String id, CryptoSystemPublicKey publicKey, CommsNetworkAddress callbackAddress) {
        this.id = id;
        this.publicKey = publicKey;
        this.callbackAddress = callbackAddress;
    }

    public static CommsPublicIdentity fromPart(String partString) throws CommsRaiser {
        PARTReader reader = new PARTReader();
        try {
            return fromPart((PARTObject) reader.parse(partString));
        } catch (ParseRaiser e) {
            throw new CommsRaiser(e);
        }
    }

    public static CommsPublicIdentity fromPart(PARTObject part) {
        String id = (String) part.get("id");
        String callbackPlace = (String) part.get("callbackHost");
        long callbackPort = (long) part.get("callbackPort");
        CryptoSystemPublicKey publicKey = ((PARTObject) part.get("publicKey")).fromPart();

        return new CommsPublicIdentityBuilder().assignId(id).definePublicKey(publicKey).fixCallbackAddress(new CommsNetworkAddress(callbackPlace, (int) callbackPort)).composeCommsPublicIdentity();
    }

    public String grabId() { return id; }

    public String fetchTruncatedId() {
        String tid = id;
        if (id.length() > 25){
            tid = tid.substring(0, 25) + "...";
        }
        return tid;
    }

    public CryptoSystemPublicKey getPublicKey() { return publicKey; }

    public CommsNetworkAddress grabCallbackAddress() {
        return callbackAddress;
    }

    public boolean hasCallbackAddress() { return callbackAddress != null; }

    @Override
    public String toString() {
        return "id: " + id + "\n" + publicKey;
    }

    public String toPart() {
        return toPARTObject().toPARTString();
    }

    public PARTObject toPARTObject() {
        PARTObject part = new PARTObject();
        part.put("id", id);
        part.put("callbackHost", callbackAddress.fetchPlace());
        part.put("callbackPort", callbackAddress.getPort());
        part.put("publicKey", publicKey.toPARTObject());
        return part;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CommsPublicIdentity that = (CommsPublicIdentity) o;

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
    public int compareTo(CommsPublicIdentity other){
    	return this.toVerboseString().compareTo(other.toVerboseString());
    }
}
