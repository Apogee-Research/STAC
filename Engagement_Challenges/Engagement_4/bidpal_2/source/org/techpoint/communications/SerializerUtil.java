package org.techpoint.communications;

import org.techpoint.mathematic.CryptoSystemPublicKey;
import org.techpoint.mathematic.CryptoSystemUtil;
import com.google.protobuf.ByteString;

import java.math.BigInteger;

/**
 * Serializes and deserializes Comms messages
 */
public class SerializerUtil {

    public static CryptoSystemPublicKey deserializePublicKey(Comms.PublicKey publicKey) {
        byte[] e = publicKey.getE().toByteArray();
        byte[] modulo = publicKey.getModulus().toByteArray();
        return new CryptoSystemPublicKey(CryptoSystemUtil.toBigInt(modulo), CryptoSystemUtil.toBigInt(e));
    }

    public static BigInteger deserializeDHPublicKey(Comms.DHPublicKey publicKey) {
        byte[] publicKeyByte = publicKey.getKey().toByteArray();
        return CryptoSystemUtil.toBigInt(publicKeyByte);
    }

    public static Comms.DHPublicKey serializeDHPublicKey(BigInteger publicKey) {
        Comms.DHPublicKey dhPublicKey = Comms.DHPublicKey.newBuilder()
                .setKey(ByteString.copyFrom(publicKey.toByteArray()))
                .build();
        return dhPublicKey;
    }

    public static CommsNetworkAddress deserializeNetworkAddress(Comms.NetworkAddress networkAddressMsg) {
        String place = networkAddressMsg.getHost();
        int port = networkAddressMsg.getPort();
        return new CommsNetworkAddress(place, port);
    }

    public static Comms.NetworkAddress serializeNetworkAddress(CommsNetworkAddress callbackAddress) {
        if (callbackAddress == null) {
            return null;
        }

        Comms.NetworkAddress address = Comms.NetworkAddress.newBuilder()
                .setHost(callbackAddress.fetchPlace())
                .setPort(callbackAddress.getPort())
                .build();

        return address;
    }

    public static Comms.Identity serializeIdentity(CommsPublicIdentity identity) {
        Comms.Identity.Builder serializedIdBuilder = Comms.Identity.newBuilder()
                .setId(identity.grabId())
                .setPublicKey(identity.getPublicKey().serializePublicKey());

        if (identity.hasCallbackAddress()) {
            serializeIdentityManager(identity, serializedIdBuilder);
        }

        return serializedIdBuilder.build();
    }

    private static void serializeIdentityManager(CommsPublicIdentity identity, Comms.Identity.Builder serializedIdBuilder) {
        serializedIdBuilder.setCallbackAddress(serializeNetworkAddress(identity.grabCallbackAddress()));
    }

    public static CommsPublicIdentity deserializeIdentity(Comms.Identity identity) {
        String id = identity.getId();
        Comms.PublicKey publicKey = identity.getPublicKey();
        CryptoSystemPublicKey CryptoSystemPublicKey = deserializePublicKey(publicKey);

        if (identity.hasCallbackAddress()) {
            return new CommsPublicIdentityBuilder().assignId(id).definePublicKey(CryptoSystemPublicKey).fixCallbackAddress(deserializeNetworkAddress(identity.getCallbackAddress())).composeCommsPublicIdentity();
        }

        return new CommsPublicIdentityBuilder().assignId(id).definePublicKey(CryptoSystemPublicKey).composeCommsPublicIdentity();
    }
}
