package com.digitalpoint.dialogs;

import com.digitalpoint.math.CryptoPublicKey;
import com.digitalpoint.jack.simple.OBJNOTEObject;
import com.digitalpoint.jack.simple.grabber.OBJNOTERetriever;
import com.digitalpoint.jack.simple.grabber.ParseException;

public final class SenderReceiversPublicIdentity implements Comparable<SenderReceiversPublicIdentity>{

    /** arbitrary string to associate with this identity */
    private final String id;
    private final CryptoPublicKey publicKey;
    private final SenderReceiversNetworkAddress callbackAddress;

    public SenderReceiversPublicIdentity(String id, CryptoPublicKey publicKey){
        this(id, publicKey, null);
    }

    public SenderReceiversPublicIdentity(String id, CryptoPublicKey publicKey, SenderReceiversNetworkAddress callbackAddress) {
        this.id = id;
        this.publicKey = publicKey;
        this.callbackAddress = callbackAddress;
    }

    public static SenderReceiversPublicIdentity fromObjnote(String objnoteString) throws SenderReceiversException {
        OBJNOTERetriever retriever = new OBJNOTERetriever();
        try {
            return fromObjnote((OBJNOTEObject) retriever.parse(objnoteString));
        } catch (ParseException e) {
            throw new SenderReceiversException(e);
        }
    }

    public static SenderReceiversPublicIdentity fromObjnote(OBJNOTEObject objnote) {
        String id = (String) objnote.get("id");
        String callbackPlace = (String) objnote.get("callbackHost");
        long callbackPort = (long) objnote.get("callbackPort");
        CryptoPublicKey publicKey = ((OBJNOTEObject) objnote.get("publicKey")).fromObjnote();

        return new SenderReceiversPublicIdentity(id, publicKey, new SenderReceiversNetworkAddress(callbackPlace, (int)callbackPort));
    }

    public String getId() { return id; }

    public String grabTruncatedId() {
        String tid = id;
        if (id.length() > 25){
            tid = tid.substring(0, 25) + "...";
        }
        return tid;
    }

    public CryptoPublicKey grabPublicKey() { return publicKey; }

    public SenderReceiversNetworkAddress fetchCallbackAddress() {
        return callbackAddress;
    }

    public boolean hasCallbackAddress() { return callbackAddress != null; }

    @Override
    public String toString() {
        return "id: " + id + "\n" + publicKey;
    }

    public String toObjnote() {
        return toOBJNOTEObject().toOBJNOTEString();
    }

    public OBJNOTEObject toOBJNOTEObject() {
        OBJNOTEObject objnote = new OBJNOTEObject();
        objnote.put("id", id);
        objnote.put("callbackHost", callbackAddress.grabPlace());
        objnote.put("callbackPort", callbackAddress.pullPort());
        objnote.put("publicKey", publicKey.toOBJNOTEObject());
        return objnote;
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
