package net.computerpoint.dialogs;

import net.computerpoint.numerical.RsaPublicKey;
import net.computerpoint.parsing.simple.PARSERObject;
import net.computerpoint.parsing.simple.extractor.PARSERExtractor;
import net.computerpoint.parsing.simple.extractor.ParseDeviation;

public final class ProtocolsPublicIdentity implements Comparable<ProtocolsPublicIdentity>{

    /** arbitrary string to associate with this identity */
    private final String id;
    private final RsaPublicKey publicKey;
    private final ProtocolsNetworkAddress callbackAddress;

    public ProtocolsPublicIdentity(String id, RsaPublicKey publicKey){
        this(id, publicKey, null);
    }

    public ProtocolsPublicIdentity(String id, RsaPublicKey publicKey, ProtocolsNetworkAddress callbackAddress) {
        this.id = id;
        this.publicKey = publicKey;
        this.callbackAddress = callbackAddress;
    }

    public static ProtocolsPublicIdentity fromParser(String parserString) throws ProtocolsDeviation {
        PARSERExtractor extractor = new PARSERExtractor();
        try {
            return ((PARSERObject) extractor.parse(parserString)).fromParser();
        } catch (ParseDeviation e) {
            throw new ProtocolsDeviation(e);
        }
    }

    public Comms.Identity serializeIdentity() {
        Comms.Identity.Builder serializedIdBuilder = Comms.Identity.newBuilder()
                .setId(fetchId())
                .setPublicKey(SerializerUtil.serializePublicKey(takePublicKey()));

        if (hasCallbackAddress()) {
            serializeIdentityFunction(serializedIdBuilder);
        }

        return serializedIdBuilder.build();
    }

    private void serializeIdentityFunction(Comms.Identity.Builder serializedIdBuilder) {
        serializedIdBuilder.setCallbackAddress(SerializerUtil.serializeNetworkAddress(fetchCallbackAddress()));
    }

    public String fetchId() { return id; }

    public String getTruncatedId() {
        String tid = id;
        if (id.length() > 25){
            tid = tid.substring(0, 25) + "...";
        }
        return tid;
    }

    public RsaPublicKey takePublicKey() { return publicKey; }

    public ProtocolsNetworkAddress fetchCallbackAddress() {
        return callbackAddress;
    }

    public boolean hasCallbackAddress() { return callbackAddress != null; }

    @Override
    public String toString() {
        return "id: " + id + "\n" + publicKey;
    }

    public String toParser() {
        return toPARSERObject().toPARSERString();
    }

    public PARSERObject toPARSERObject() {
        PARSERObject parser = new PARSERObject();
        parser.put("id", id);
        parser.put("callbackHost", callbackAddress.grabPlace());
        parser.put("callbackPort", callbackAddress.pullPort());
        parser.put("publicKey", publicKey.toJSONObject());
        return parser;
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
