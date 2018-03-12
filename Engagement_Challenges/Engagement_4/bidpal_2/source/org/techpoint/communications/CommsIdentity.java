package org.techpoint.communications;

import org.techpoint.mathematic.CryptoSystemPrivateKey;
import org.techpoint.mathematic.CryptoSystemPublicKey;
import org.techpoint.parsing.simple.PARTObject;
import org.techpoint.parsing.simple.reader.PARTReader;

import java.io.File;
import java.io.FileReader;

public class CommsIdentity {

    /** arbitrary string to associate with this identity */
    private final String id;
    private final CryptoSystemPrivateKey key;
    private final CommsNetworkAddress callbackAddress;

    public CommsIdentity(String id, CryptoSystemPrivateKey key) {
        this(id, key, null);
    }

    public CommsIdentity(String id, CryptoSystemPrivateKey key, CommsNetworkAddress callbackAddress) {
        this.id = id;
        this.key = key;
        this.callbackAddress = callbackAddress;
    }

    public static CommsIdentity loadFromFile(File identityFile) throws CommsRaiser {
        PARTReader reader = new PARTReader();
        try {
            PARTObject part = (PARTObject) reader.parse(new FileReader(identityFile));
            PARTObject privateKeyPart = (PARTObject) part.get("privateKey");
            CryptoSystemPrivateKey privateKey = CryptoSystemPrivateKey.composeKeyFromPart(privateKeyPart);
            String id = (String) part.get("id");
            String callbackPlace = (String) part.get("callbackHost");
            long callbackPort = (long) part.get("callbackPort");
            return new CommsIdentity(id, privateKey, new CommsNetworkAddress(callbackPlace, (int)callbackPort));
        }
        catch (Exception e) {
            throw new CommsRaiser(e);
        }
    }

    public String toPart() {
        PARTObject part = new PARTObject();
        part.put("id", id);
        part.put("callbackHost", callbackAddress.fetchPlace());
        part.put("callbackPort", callbackAddress.getPort());
        part.put("privateKey", key.toPARTObject());
        return part.toPARTString();
    }

    public String takeId() { return id; }

    public String fetchTruncatedId() {
        String tid = id;
        if (id.length() > 25){
            tid = tid.substring(0, 25) + "...";
        }
        return tid;
    }

    public CryptoSystemPublicKey getPublicKey() { return key.takePublicKey(); }

    public CryptoSystemPrivateKey obtainPrivateKey() { return key; }

    public boolean hasCallbackAddress() { return callbackAddress != null; }

    public CommsNetworkAddress grabCallbackAddress() { return callbackAddress; }

    /**
     * @return the public identity associated with this identity (safe to give to anyone)
     */
    public CommsPublicIdentity grabPublicIdentity() {
        return new CommsPublicIdentityBuilder().assignId(id).definePublicKey(getPublicKey()).fixCallbackAddress(callbackAddress).composeCommsPublicIdentity();
    }

    @Override
    public String toString() {
        return "id: " + id + "\n" + key;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CommsIdentity identity = (CommsIdentity) o;

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
