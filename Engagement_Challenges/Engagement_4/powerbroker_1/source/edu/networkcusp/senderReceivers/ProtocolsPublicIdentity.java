package edu.networkcusp.senderReceivers;

import edu.networkcusp.math.PrivateCommsPublicKey;
import edu.networkcusp.jackson.simple.JACKObject;
import edu.networkcusp.jackson.simple.parser.JACKParser;
import edu.networkcusp.jackson.simple.parser.ParseRaiser;

public final class ProtocolsPublicIdentity implements Comparable<ProtocolsPublicIdentity>{

    /** arbitrary string to associate with this identity */
    private final String id;
    private final PrivateCommsPublicKey publicKey;
    private final ProtocolsNetworkAddress callbackAddress;

    public ProtocolsPublicIdentity(String id, PrivateCommsPublicKey publicKey){
        this(id, publicKey, null);
    }

    public ProtocolsPublicIdentity(String id, PrivateCommsPublicKey publicKey, ProtocolsNetworkAddress callbackAddress) {
        this.id = id;
        this.publicKey = publicKey;
        this.callbackAddress = callbackAddress;
    }

    public static ProtocolsPublicIdentity fromJack(String jackString) throws ProtocolsRaiser {
        JACKParser parser = new JACKParser();
        try {
            return fromJack((JACKObject) parser.parse(jackString));
        } catch (ParseRaiser e) {
            throw new ProtocolsRaiser(e);
        }
    }

    public static ProtocolsPublicIdentity fromJack(JACKObject jack) {
        String id = (String) jack.get("id");
        String callbackPlace = (String) jack.get("callbackHost");
        long callbackPort = (long) jack.get("callbackPort");
        PrivateCommsPublicKey publicKey = PrivateCommsPublicKey.fromJack((JACKObject) jack.get("publicKey"));

        return new ProtocolsPublicIdentity(id, publicKey, new ProtocolsNetworkAddressBuilder().setPlace(callbackPlace).definePort((int) callbackPort).formProtocolsNetworkAddress());
    }

    public String fetchId() { return id; }

    public String obtainTruncatedId() {
        String tid = id;
        if (id.length() > 25){
            tid = tid.substring(0, 25) + "...";
        }
        return tid;
    }

    public PrivateCommsPublicKey takePublicKey() { return publicKey; }

    public ProtocolsNetworkAddress takeCallbackAddress() {
        return callbackAddress;
    }

    public boolean hasCallbackAddress() { return callbackAddress != null; }

    @Override
    public String toString() {
        return "id: " + id + "\n" + publicKey;
    }

    public String toJack() {
        return toJACKObject().toJACKString();
    }

    public JACKObject toJACKObject() {
        JACKObject jack = new JACKObject();
        jack.put("id", id);
        jack.put("callbackHost", callbackAddress.obtainPlace());
        jack.put("callbackPort", callbackAddress.takePort());
        jack.put("publicKey", publicKey.toJACKObject());
        return jack;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ProtocolsPublicIdentity that = (ProtocolsPublicIdentity) o;

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
    public int compareTo(ProtocolsPublicIdentity other){
    	return this.toVerboseString().compareTo(other.toVerboseString());
    }
}
