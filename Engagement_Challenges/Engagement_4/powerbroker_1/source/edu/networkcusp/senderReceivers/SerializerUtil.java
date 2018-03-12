package edu.networkcusp.senderReceivers;

import edu.networkcusp.math.PrivateCommsPublicKey;
import edu.networkcusp.math.PrivateCommsUtil;
import com.google.protobuf.ByteString;

import java.math.BigInteger;

/**
 * Serializes and deserializes Comms messages
 */
public class SerializerUtil {

    public static PrivateCommsPublicKey deserializePublicKey(Comms.PublicKey publicKey) {
        byte[] e = publicKey.getE().toByteArray();
        byte[] modulo = publicKey.getModulus().toByteArray();
        return new PrivateCommsPublicKey(PrivateCommsUtil.toBigInt(modulo), PrivateCommsUtil.toBigInt(e));
    }

    public static BigInteger deserializeDHPublicKey(Comms.DHPublicKey publicKey) {
        byte[] publicKeyByte = publicKey.getKey().toByteArray();
        return PrivateCommsUtil.toBigInt(publicKeyByte);
    }

    public static Comms.DHPublicKey serializeDHPublicKey(BigInteger publicKey) {
        Comms.DHPublicKey dhPublicKey = Comms.DHPublicKey.newBuilder()
                .setKey(ByteString.copyFrom(publicKey.toByteArray()))
                .build();
        return dhPublicKey;
    }

    public static ProtocolsNetworkAddress deserializeNetworkAddress(Comms.NetworkAddress networkAddressMsg) {
        String place = networkAddressMsg.getHost();
        int port = networkAddressMsg.getPort();
        return new ProtocolsNetworkAddressBuilder().setPlace(place).definePort(port).formProtocolsNetworkAddress();
    }

    public static Comms.NetworkAddress serializeNetworkAddress(ProtocolsNetworkAddress callbackAddress) {
        if (callbackAddress == null) {
            return null;
        }

        Comms.NetworkAddress address = Comms.NetworkAddress.newBuilder()
                .setHost(callbackAddress.obtainPlace())
                .setPort(callbackAddress.takePort())
                .build();

        return address;
    }

    public static Comms.Identity serializeIdentity(ProtocolsPublicIdentity identity) {
        Comms.Identity.Builder serializedIdBuilder = Comms.Identity.newBuilder()
                .setId(identity.fetchId())
                .setPublicKey(identity.takePublicKey().serializePublicKey());

        if (identity.hasCallbackAddress()) {
            serializedIdBuilder.setCallbackAddress(serializeNetworkAddress(identity.takeCallbackAddress()));
        }

        return serializedIdBuilder.build();
    }

    public static ProtocolsPublicIdentity deserializeIdentity(Comms.Identity identity) {
        String id = identity.getId();
        Comms.PublicKey publicKey = identity.getPublicKey();
        PrivateCommsPublicKey PrivateCommsPublicKey = deserializePublicKey(publicKey);

        if (identity.hasCallbackAddress()) {
            return new ProtocolsPublicIdentity(id, PrivateCommsPublicKey, deserializeNetworkAddress(identity.getCallbackAddress()));
        }

        return new ProtocolsPublicIdentity(id, PrivateCommsPublicKey);
    }
}
