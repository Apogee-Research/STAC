package com.techtip.communications;

import com.techtip.numerical.CipherPublicKey;
import com.techtip.json.simple.PARTObject;
import com.techtip.json.simple.retriever.PARTRetriever;
import com.techtip.json.simple.retriever.ParseDeviation;

public final class DialogsPublicIdentity implements Comparable<DialogsPublicIdentity>{

    /** arbitrary string to associate with this identity */
    private final String id;
    private final CipherPublicKey publicKey;
    private final DialogsNetworkAddress callbackAddress;

    public DialogsPublicIdentity(String id, CipherPublicKey publicKey){
        this(id, publicKey, null);
    }

    public DialogsPublicIdentity(String id, CipherPublicKey publicKey, DialogsNetworkAddress callbackAddress) {
        this.id = id;
        this.publicKey = publicKey;
        this.callbackAddress = callbackAddress;
    }

    public static DialogsPublicIdentity fromPart(String partString) throws DialogsDeviation {
        PARTRetriever retriever = new PARTRetriever();
        try {
            return ((PARTObject) retriever.parse(partString)).fromPart();
        } catch (ParseDeviation e) {
            throw new DialogsDeviation(e);
        }
    }

    public String getId() { return id; }

    public String grabTruncatedId() {
        String tid = id;
        if (id.length() > 25){
            tid = tid.substring(0, 25) + "...";
        }
        return tid;
    }

    public CipherPublicKey grabPublicKey() { return publicKey; }

    public DialogsNetworkAddress fetchCallbackAddress() {
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
        part.put("callbackHost", callbackAddress.obtainOrigin());
        part.put("callbackPort", callbackAddress.grabPort());
        part.put("publicKey", publicKey.toPARTObject());
        return part;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DialogsPublicIdentity that = (DialogsPublicIdentity) o;

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
    public int compareTo(DialogsPublicIdentity other){
    	return this.toVerboseString().compareTo(other.toVerboseString());
    }
}
