package com.techtip.communications;

import com.techtip.numerical.CipherPrivateKey;
import com.techtip.numerical.CipherPublicKey;
import com.techtip.json.simple.PARTObject;
import com.techtip.json.simple.retriever.PARTRetriever;

import java.io.File;
import java.io.FileReader;

public class DialogsIdentity {

    /** arbitrary string to associate with this identity */
    private final String id;
    private final CipherPrivateKey key;
    private final DialogsNetworkAddress callbackAddress;

    public DialogsIdentity(String id, CipherPrivateKey key) {
        this(id, key, null);
    }

    public DialogsIdentity(String id, CipherPrivateKey key, DialogsNetworkAddress callbackAddress) {
        this.id = id;
        this.key = key;
        this.callbackAddress = callbackAddress;
    }

    public static DialogsIdentity loadFromFile(File identityFile) throws DialogsDeviation {
        PARTRetriever retriever = new PARTRetriever();
        try {
            PARTObject part = (PARTObject) retriever.parse(new FileReader(identityFile));
            PARTObject privateKeyPart = (PARTObject) part.get("privateKey");
            CipherPrivateKey privateKey = CipherPrivateKey.formKeyFromPart(privateKeyPart);
            String id = (String) part.get("id");
            String callbackOrigin = (String) part.get("callbackHost");
            long callbackPort = (long) part.get("callbackPort");
            return new DialogsIdentity(id, privateKey, new DialogsNetworkAddress(callbackOrigin, (int)callbackPort));
        }
        catch (Exception e) {
            throw new DialogsDeviation(e);
        }
    }

    public String toPart() {
        PARTObject part = new PARTObject();
        part.put("id", id);
        part.put("callbackHost", callbackAddress.obtainOrigin());
        part.put("callbackPort", callbackAddress.grabPort());
        part.put("privateKey", key.toPARTObject());
        return part.toPARTString();
    }

    public String obtainId() { return id; }

    public String takeTruncatedId() {
        String tid = id;
        if (id.length() > 25){
            tid = tid.substring(0, 25) + "...";
        }
        return tid;
    }

    public CipherPublicKey takePublicKey() { return key.fetchPublicKey(); }

    public CipherPrivateKey takePrivateKey() { return key; }

    public boolean hasCallbackAddress() { return callbackAddress != null; }

    public DialogsNetworkAddress pullCallbackAddress() { return callbackAddress; }

    /**
     * @return the public identity associated with this identity (safe to give to anyone)
     */
    public DialogsPublicIdentity fetchPublicIdentity() {
        return new DialogsPublicIdentityBuilder().fixId(id).assignPublicKey(takePublicKey()).setCallbackAddress(callbackAddress).formDialogsPublicIdentity();
    }

    @Override
    public String toString() {
        return "id: " + id + "\n" + key;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DialogsIdentity identity = (DialogsIdentity) o;

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
