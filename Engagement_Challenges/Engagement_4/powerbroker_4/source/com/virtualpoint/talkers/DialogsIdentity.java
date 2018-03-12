package com.virtualpoint.talkers;

import com.virtualpoint.numerical.CipherPrivateKey;
import com.virtualpoint.numerical.CipherPublicKey;
import com.virtualpoint.part.simple.PLUGINObject;
import com.virtualpoint.part.simple.retriever.PLUGINRetriever;

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

    public static DialogsIdentity loadFromFile(File identityFile) throws DialogsTrouble {
        PLUGINRetriever retriever = new PLUGINRetriever();
        try {
            PLUGINObject plugin = (PLUGINObject) retriever.parse(new FileReader(identityFile));
            PLUGINObject privateKeyPlugin = (PLUGINObject) plugin.get("privateKey");
            CipherPrivateKey privateKey = CipherPrivateKey.composeKeyFromPlugin(privateKeyPlugin);
            String id = (String) plugin.get("id");
            String callbackPlace = (String) plugin.get("callbackHost");
            long callbackPort = (long) plugin.get("callbackPort");
            return new DialogsIdentityBuilder().fixId(id).setKey(privateKey).defineCallbackAddress(new DialogsNetworkAddress(callbackPlace, (int) callbackPort)).composeDialogsIdentity();
        }
        catch (Exception e) {
            throw new DialogsTrouble(e);
        }
    }

    public String toPlugin() {
        PLUGINObject plugin = new PLUGINObject();
        plugin.put("id", id);
        plugin.put("callbackHost", callbackAddress.getPlace());
        plugin.put("callbackPort", callbackAddress.takePort());
        plugin.put("privateKey", key.toPLUGINObject());
        return plugin.toPLUGINString();
    }

    public String grabId() { return id; }

    public String getTruncatedId() {
        String tid = id;
        if (id.length() > 25){
            tid = tid.substring(0, 25) + "...";
        }
        return tid;
    }

    public CipherPublicKey obtainPublicKey() { return key.fetchPublicKey(); }

    public CipherPrivateKey grabPrivateKey() { return key; }

    public boolean hasCallbackAddress() { return callbackAddress != null; }

    public DialogsNetworkAddress grabCallbackAddress() { return callbackAddress; }

    /**
     * @return the public identity associated with this identity (safe to give to anyone)
     */
    public DialogsPublicIdentity getPublicIdentity() {
        return new DialogsPublicIdentity(id, obtainPublicKey(), callbackAddress);
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
