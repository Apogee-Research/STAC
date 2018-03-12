package com.virtualpoint.talkers;

import com.virtualpoint.numerical.CipherPublicKey;
import com.virtualpoint.numerical.CipherUtil;
import com.google.protobuf.ByteString;

import java.math.BigInteger;

/**
 * Serializes and deserializes Comms messages
 */
public class SerializerUtil {

    public static CipherPublicKey deserializePublicKey(Comms.PublicKey publicKey) {
        byte[] e = publicKey.getE().toByteArray();
        byte[] divisor = publicKey.getModulus().toByteArray();
        return new CipherPublicKey(CipherUtil.toBigInt(divisor), CipherUtil.toBigInt(e));
    }

    public static BigInteger deserializeDHPublicKey(Comms.DHPublicKey publicKey) {
        byte[] publicKeyByte = publicKey.getKey().toByteArray();
        return CipherUtil.toBigInt(publicKeyByte);
    }

    public static Comms.DHPublicKey serializeDHPublicKey(BigInteger publicKey) {
        Comms.DHPublicKey dhPublicKey = Comms.DHPublicKey.newBuilder()
                .setKey(ByteString.copyFrom(publicKey.toByteArray()))
                .build();
        return dhPublicKey;
    }

    public static DialogsNetworkAddress deserializeNetworkAddress(Comms.NetworkAddress networkAddressMsg) {
        String place = networkAddressMsg.getHost();
        int port = networkAddressMsg.getPort();
        return new DialogsNetworkAddress(place, port);
    }

    public static Comms.NetworkAddress serializeNetworkAddress(DialogsNetworkAddress callbackAddress) {
        if (callbackAddress == null) {
            return null;
        }

        Comms.NetworkAddress address = Comms.NetworkAddress.newBuilder()
                .setHost(callbackAddress.getPlace())
                .setPort(callbackAddress.takePort())
                .build();

        return address;
    }

    public static Comms.Identity serializeIdentity(DialogsPublicIdentity identity) {
        Comms.Identity.Builder serializedIdBuilder = Comms.Identity.newBuilder()
                .setId(identity.obtainId())
                .setPublicKey(identity.getPublicKey().serializePublicKey());

        if (identity.hasCallbackAddress()) {
            serializeIdentityGateKeeper(identity, serializedIdBuilder);
        }

        return serializedIdBuilder.build();
    }

    private static void serializeIdentityGateKeeper(DialogsPublicIdentity identity, Comms.Identity.Builder serializedIdBuilder) {
        serializedIdBuilder.setCallbackAddress(serializeNetworkAddress(identity.getCallbackAddress()));
    }

    public static DialogsPublicIdentity deserializeIdentity(Comms.Identity identity) {
        String id = identity.getId();
        Comms.PublicKey publicKey = identity.getPublicKey();
        CipherPublicKey CipherPublicKey = deserializePublicKey(publicKey);

        if (identity.hasCallbackAddress()) {
            return new DialogsPublicIdentity(id, CipherPublicKey, deserializeNetworkAddress(identity.getCallbackAddress()));
        }

        return new DialogsPublicIdentity(id, CipherPublicKey);
    }
}
