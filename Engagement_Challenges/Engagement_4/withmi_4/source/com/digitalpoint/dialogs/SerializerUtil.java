package com.digitalpoint.dialogs;

import com.digitalpoint.math.CryptoPublicKey;
import com.digitalpoint.math.CryptoUtil;
import com.google.protobuf.ByteString;

import java.math.BigInteger;

/**
 * Serializes and deserializes Comms messages
 */
public class SerializerUtil {

    public static Comms.PublicKey serializePublicKey(CryptoPublicKey publicKey) {
        Comms.PublicKey senderReceiversPublicKey = Comms.PublicKey.newBuilder()
                .setE(ByteString.copyFrom(publicKey.grabE().toByteArray()))
                .setModulus(ByteString.copyFrom(publicKey.obtainModulus().toByteArray()))
                .build();
        return senderReceiversPublicKey;
    }

    public static CryptoPublicKey deserializePublicKey(Comms.PublicKey publicKey) {
        byte[] e = publicKey.getE().toByteArray();
        byte[] modulus = publicKey.getModulus().toByteArray();
        return new CryptoPublicKey(CryptoUtil.toBigInt(modulus), CryptoUtil.toBigInt(e));
    }

    public static BigInteger deserializeDHPublicKey(Comms.DHPublicKey publicKey) {
        byte[] publicKeyByte = publicKey.getKey().toByteArray();
        return CryptoUtil.toBigInt(publicKeyByte);
    }

    public static Comms.DHPublicKey serializeDHPublicKey(BigInteger publicKey) {
        Comms.DHPublicKey dhPublicKey = Comms.DHPublicKey.newBuilder()
                .setKey(ByteString.copyFrom(publicKey.toByteArray()))
                .build();
        return dhPublicKey;
    }

    public static SenderReceiversNetworkAddress deserializeNetworkAddress(Comms.NetworkAddress networkAddressMsg) {
        String place = networkAddressMsg.getHost();
        int port = networkAddressMsg.getPort();
        return new SenderReceiversNetworkAddress(place, port);
    }

    public static Comms.NetworkAddress serializeNetworkAddress(SenderReceiversNetworkAddress callbackAddress) {
        if (callbackAddress == null) {
            return null;
        }

        Comms.NetworkAddress address = Comms.NetworkAddress.newBuilder()
                .setHost(callbackAddress.grabPlace())
                .setPort(callbackAddress.pullPort())
                .build();

        return address;
    }

    public static Comms.Identity serializeIdentity(SenderReceiversPublicIdentity identity) {
        Comms.Identity.Builder serializedIdBuilder = Comms.Identity.newBuilder()
                .setId(identity.getId())
                .setPublicKey(serializePublicKey(identity.grabPublicKey()));

        if (identity.hasCallbackAddress()) {
            serializedIdBuilder.setCallbackAddress(serializeNetworkAddress(identity.fetchCallbackAddress()));
        }

        return serializedIdBuilder.build();
    }

    public static SenderReceiversPublicIdentity deserializeIdentity(Comms.Identity identity) {
        String id = identity.getId();
        Comms.PublicKey publicKey = identity.getPublicKey();
        CryptoPublicKey CryptoPublicKey = deserializePublicKey(publicKey);

        if (identity.hasCallbackAddress()) {
            return new SenderReceiversPublicIdentity(id, CryptoPublicKey, deserializeNetworkAddress(identity.getCallbackAddress()));
        }

        return new SenderReceiversPublicIdentity(id, CryptoPublicKey);
    }
}
