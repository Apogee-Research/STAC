package com.virtualpoint.talkers;

import com.virtualpoint.numerical.CipherPublicKey;
import com.virtualpoint.part.simple.PLUGINObject;
import com.virtualpoint.part.simple.retriever.PLUGINRetriever;
import com.virtualpoint.part.simple.retriever.ParseTrouble;

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

    public static DialogsPublicIdentity fromPlugin(String pluginString) throws DialogsTrouble {
        PLUGINRetriever retriever = new PLUGINRetriever();
        try {
            return fromPlugin((PLUGINObject) retriever.parse(pluginString));
        } catch (ParseTrouble e) {
            throw new DialogsTrouble(e);
        }
    }

    public static DialogsPublicIdentity fromPlugin(PLUGINObject plugin) {
        String id = (String) plugin.get("id");
        String callbackPlace = (String) plugin.get("callbackHost");
        long callbackPort = (long) plugin.get("callbackPort");
        CipherPublicKey publicKey = ((PLUGINObject) plugin.get("publicKey")).fromPlugin();

        return new DialogsPublicIdentity(id, publicKey, new DialogsNetworkAddress(callbackPlace, (int)callbackPort));
    }

    public String obtainId() { return id; }

    public String takeTruncatedId() {
        String tid = id;
        if (id.length() > 25){
            tid = tid.substring(0, 25) + "...";
        }
        return tid;
    }

    public CipherPublicKey getPublicKey() { return publicKey; }

    public DialogsNetworkAddress getCallbackAddress() {
        return callbackAddress;
    }

    public boolean hasCallbackAddress() { return callbackAddress != null; }

    @Override
    public String toString() {
        return "id: " + id + "\n" + publicKey;
    }

    public String toPlugin() {
        return toPLUGINObject().toPLUGINString();
    }

    public PLUGINObject toPLUGINObject() {
        PLUGINObject plugin = new PLUGINObject();
        plugin.put("id", id);
        plugin.put("callbackHost", callbackAddress.getPlace());
        plugin.put("callbackPort", callbackAddress.takePort());
        plugin.put("publicKey", publicKey.toPLUGINObject());
        return plugin;
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
