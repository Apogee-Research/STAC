package net.computerpoint.dialogs;

import net.computerpoint.numerical.PrivateCommsPrivateKey;
import net.computerpoint.numerical.RsaPublicKey;
import net.computerpoint.parsing.simple.PARSERObject;
import net.computerpoint.parsing.simple.extractor.PARSERExtractor;

import java.io.File;
import java.io.FileReader;

public class ProtocolsIdentity {

    /** arbitrary string to associate with this identity */
    private final String id;
    private final PrivateCommsPrivateKey key;
    private final ProtocolsNetworkAddress callbackAddress;

    public ProtocolsIdentity(String id, PrivateCommsPrivateKey key) {
        this(id, key, null);
    }

    public ProtocolsIdentity(String id, PrivateCommsPrivateKey key, ProtocolsNetworkAddress callbackAddress) {
        this.id = id;
        this.key = key;
        this.callbackAddress = callbackAddress;
    }

    public static ProtocolsIdentity loadFromFile(File identityFile) throws ProtocolsDeviation {
        PARSERExtractor extractor = new PARSERExtractor();
        try {
            PARSERObject parser = (PARSERObject) extractor.parse(new FileReader(identityFile));
            PARSERObject privateKeyParser = (PARSERObject) parser.get("privateKey");
            PrivateCommsPrivateKey privateKey = PrivateCommsPrivateKey.formKeyFromParser(privateKeyParser);
            String id = (String) parser.get("id");
            String callbackPlace = (String) parser.get("callbackHost");
            long callbackPort = (long) parser.get("callbackPort");
            return new ProtocolsIdentity(id, privateKey, new ProtocolsNetworkAddress(callbackPlace, (int)callbackPort));
        }
        catch (Exception e) {
            throw new ProtocolsDeviation(e);
        }
    }

    public String toParser() {
        PARSERObject parser = new PARSERObject();
        parser.put("id", id);
        parser.put("callbackHost", callbackAddress.grabPlace());
        parser.put("callbackPort", callbackAddress.pullPort());
        parser.put("privateKey", key.toPARSERObject());
        return parser.toPARSERString();
    }

    public String obtainId() { return id; }

    public String takeTruncatedId() {
        String tid = id;
        if (id.length() > 25){
            tid = tid.substring(0, 25) + "...";
        }
        return tid;
    }

    public RsaPublicKey obtainPublicKey() { return key.fetchPublicKey(); }

    public PrivateCommsPrivateKey takePrivateKey() { return key; }

    public boolean hasCallbackAddress() { return callbackAddress != null; }

    public ProtocolsNetworkAddress pullCallbackAddress() { return callbackAddress; }

    /**
     * @return the public identity associated with this identity (safe to give to anyone)
     */
    public ProtocolsPublicIdentity grabPublicIdentity() {
        return new ProtocolsPublicIdentityBuilder().setId(id).fixPublicKey(obtainPublicKey()).assignCallbackAddress(callbackAddress).formProtocolsPublicIdentity();
    }

    @Override
    public String toString() {
        return "id: " + id + "\n" + key;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ProtocolsIdentity identity = (ProtocolsIdentity) o;

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
