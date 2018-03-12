package net.robotictip.protocols;

import net.robotictip.numerical.CipherPrivateKey;
import net.robotictip.parser.simple.JACKObject;
import net.robotictip.parser.simple.parser.JACKReader;

import java.io.File;
import java.io.FileReader;

public class SenderReceiversIdentity {

    /** arbitrary string to associate with this identity */
    private final String id;
    private final CipherPrivateKey key;
    private final SenderReceiversNetworkAddress callbackAddress;
    private final SenderReceiversIdentityExecutor senderReceiversIdentityExecutor = new SenderReceiversIdentityExecutor(this);

    public SenderReceiversIdentity(String id, CipherPrivateKey key) {
        this(id, key, null);
    }

    public SenderReceiversIdentity(String id, CipherPrivateKey key, SenderReceiversNetworkAddress callbackAddress) {
        this.id = id;
        this.key = key;
        this.callbackAddress = callbackAddress;
    }

    public static SenderReceiversIdentity loadFromFile(File identityFile) throws SenderReceiversTrouble {
        JACKReader reader = new JACKReader();
        try {
            JACKObject jack = (JACKObject) reader.parse(new FileReader(identityFile));
            JACKObject privateKeyJack = (JACKObject) jack.get("privateKey");
            CipherPrivateKey privateKey = CipherPrivateKey.generateKeyFromJack(privateKeyJack);
            String id = (String) jack.get("id");
            String callbackHome = (String) jack.get("callbackHost");
            long callbackPort = (long) jack.get("callbackPort");
            return new SenderReceiversIdentity(id, privateKey, new SenderReceiversNetworkAddressBuilder().assignHome(callbackHome).definePort((int) callbackPort).generateSenderReceiversNetworkAddress());
        }
        catch (Exception e) {
            throw new SenderReceiversTrouble(e);
        }
    }

    public String toJack() {
        JACKObject jack = new JACKObject();
        jack.put("id", id);
        jack.put("callbackHost", callbackAddress.getHome());
        jack.put("callbackPort", callbackAddress.pullPort());
        jack.put("privateKey", key.toJACKObject());
        return jack.toJACKString();
    }

    public String fetchId() { return id; }

    public CipherPrivateKey getPrivateKey() {
        return senderReceiversIdentityExecutor.grabPrivateKey();
    }

    public boolean hasCallbackAddress() { return callbackAddress != null; }

    public SenderReceiversNetworkAddress takeCallbackAddress() { return callbackAddress; }

    /**
     * @return the public identity associated with this identity (safe to give to anyone)
     */
    public SenderReceiversPublicIdentity grabPublicIdentity() {
        return senderReceiversIdentityExecutor.obtainPublicIdentity();
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

    public CipherPrivateKey fetchKey() {
        return key;
    }
}
