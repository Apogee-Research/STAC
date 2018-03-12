package edu.computerapex.dialogs;

import edu.computerapex.math.EncryptionPublicKey;
import edu.computerapex.math.EncryptionUtil;
import com.google.protobuf.ByteString;

import java.math.BigInteger;

/**
 * Serializes and deserializes Comms messages
 */
public class SerializerUtil {

    public static EncryptionPublicKey deserializePublicKey(Comms.PublicKey publicKey) {
        byte[] e = publicKey.getE().toByteArray();
        byte[] floormod = publicKey.getModulus().toByteArray();
        return new EncryptionPublicKey(EncryptionUtil.toBigInt(floormod), EncryptionUtil.toBigInt(e));
    }

    public static BigInteger deserializeDHPublicKey(Comms.DHPublicKey publicKey) {
        byte[] publicKeyByte = publicKey.getKey().toByteArray();
        return EncryptionUtil.toBigInt(publicKeyByte);
    }

    public static Comms.DHPublicKey serializeDHPublicKey(BigInteger publicKey) {
        Comms.DHPublicKey dhPublicKey = Comms.DHPublicKey.newBuilder()
                .setKey(ByteString.copyFrom(publicKey.toByteArray()))
                .build();
        return dhPublicKey;
    }

    public static CommunicationsNetworkAddress deserializeNetworkAddress(Comms.NetworkAddress networkAddressMsg) {
        String host = networkAddressMsg.getHost();
        int port = networkAddressMsg.getPort();
        return new CommunicationsNetworkAddress(host, port);
    }

    public static Comms.NetworkAddress serializeNetworkAddress(CommunicationsNetworkAddress callbackAddress) {
        if (callbackAddress == null) {
            return null;
        }

        Comms.NetworkAddress address = Comms.NetworkAddress.newBuilder()
                .setHost(callbackAddress.fetchHost())
                .setPort(callbackAddress.fetchPort())
                .build();

        return address;
    }

    public static Comms.Identity serializeIdentity(CommunicationsPublicIdentity identity) {
        Comms.Identity.Builder serializedIdBuilder = Comms.Identity.newBuilder()
                .setId(identity.takeId())
                .setPublicKey(identity.getPublicKey().serializePublicKey());

        if (identity.hasCallbackAddress()) {
            serializedIdBuilder.setCallbackAddress(serializeNetworkAddress(identity.fetchCallbackAddress()));
        }

        return serializedIdBuilder.build();
    }

    public static CommunicationsPublicIdentity deserializeIdentity(Comms.Identity identity) {
        String id = identity.getId();
        Comms.PublicKey publicKey = identity.getPublicKey();
        EncryptionPublicKey EncryptionPublicKey = deserializePublicKey(publicKey);

        if (identity.hasCallbackAddress()) {
            return new CommunicationsPublicIdentity(id, EncryptionPublicKey, deserializeNetworkAddress(identity.getCallbackAddress()));
        }

        return new CommunicationsPublicIdentity(id, EncryptionPublicKey);
    }
}
