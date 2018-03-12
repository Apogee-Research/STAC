package com.digitalpoint.dialogs;

import com.digitalpoint.math.CryptoPrivateKey;
import com.digitalpoint.math.CryptoPublicKey;
import com.digitalpoint.jack.simple.OBJNOTEObject;
import com.digitalpoint.jack.simple.grabber.OBJNOTERetriever;

import java.io.File;
import java.io.FileReader;

public class SenderReceiversIdentity {

    /** arbitrary string to associate with this identity */
    private final String id;
    private final CryptoPrivateKey key;
    private final SenderReceiversNetworkAddress callbackAddress;

    public SenderReceiversIdentity(String id, CryptoPrivateKey key) {
        this(id, key, null);
    }

    public SenderReceiversIdentity(String id, CryptoPrivateKey key, SenderReceiversNetworkAddress callbackAddress) {
        this.id = id;
        this.key = key;
        this.callbackAddress = callbackAddress;
    }

    public static SenderReceiversIdentity loadFromFile(File identityFile) throws SenderReceiversException {
        OBJNOTERetriever retriever = new OBJNOTERetriever();
        try {
            OBJNOTEObject objnote = (OBJNOTEObject) retriever.parse(new FileReader(identityFile));
            OBJNOTEObject privateKeyObjnote = (OBJNOTEObject) objnote.get("privateKey");
            CryptoPrivateKey privateKey = CryptoPrivateKey.makeKeyFromObjnote(privateKeyObjnote);
            String id = (String) objnote.get("id");
            String callbackPlace = (String) objnote.get("callbackHost");
            long callbackPort = (long) objnote.get("callbackPort");
            return new SenderReceiversIdentityBuilder().assignId(id).fixKey(privateKey).defineCallbackAddress(new SenderReceiversNetworkAddress(callbackPlace, (int) callbackPort)).makeSenderReceiversIdentity();
        }
        catch (Exception e) {
            throw new SenderReceiversException(e);
        }
    }

    public String toObjnote() {
        OBJNOTEObject objnote = new OBJNOTEObject();
        objnote.put("id", id);
        objnote.put("callbackHost", callbackAddress.grabPlace());
        objnote.put("callbackPort", callbackAddress.pullPort());
        objnote.put("privateKey", key.toOBJNOTEObject());
        return objnote.toOBJNOTEString();
    }

    public String fetchId() { return id; }

    public String takeTruncatedId() {
        String tid = id;
        if (id.length() > 25){
            tid = tid.substring(0, 25) + "...";
        }
        return tid;
    }

    public CryptoPublicKey grabPublicKey() { return key.obtainPublicKey(); }

    public CryptoPrivateKey getPrivateKey() { return key; }

    public boolean hasCallbackAddress() { return callbackAddress != null; }

    public SenderReceiversNetworkAddress fetchCallbackAddress() { return callbackAddress; }

    /**
     * @return the public identity associated with this identity (safe to give to anyone)
     */
    public SenderReceiversPublicIdentity obtainPublicIdentity() {
        return new SenderReceiversPublicIdentity(id, grabPublicKey(), callbackAddress);
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
