package com.techtip.communications;

import com.techtip.numerical.CipherPublicKey;
import com.techtip.numerical.CipherUtil;
import com.google.protobuf.ByteString;

import java.math.BigInteger;

/**
 * Serializes and deserializes Comms messages
 */
public class SerializerUtil {

    public static Comms.PublicKey serializePublicKey(CipherPublicKey publicKey) {
        Comms.PublicKey dialogsPublicKey = Comms.PublicKey.newBuilder()
                .setE(ByteString.copyFrom(publicKey.takeE().toByteArray()))
                .setModulus(ByteString.copyFrom(publicKey.obtainModulo().toByteArray()))
                .build();
        return dialogsPublicKey;
    }

    public static CipherPublicKey deserializePublicKey(Comms.PublicKey publicKey) {
        byte[] e = publicKey.getE().toByteArray();
        byte[] modulo = publicKey.getModulus().toByteArray();
        return new CipherPublicKey(CipherUtil.toBigInt(modulo), CipherUtil.toBigInt(e));
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
        String origin = networkAddressMsg.getHost();
        int port = networkAddressMsg.getPort();
        return new DialogsNetworkAddress(origin, port);
    }

    public static Comms.NetworkAddress serializeNetworkAddress(DialogsNetworkAddress callbackAddress) {
        if (callbackAddress == null) {
            return null;
        }

        Comms.NetworkAddress address = Comms.NetworkAddress.newBuilder()
                .setHost(callbackAddress.obtainOrigin())
                .setPort(callbackAddress.grabPort())
                .build();

        return address;
    }

    public static Comms.Identity serializeIdentity(DialogsPublicIdentity identity) {
        Comms.Identity.Builder serializedIdBuilder = Comms.Identity.newBuilder()
                .setId(identity.getId())
                .setPublicKey(serializePublicKey(identity.grabPublicKey()));

        if (identity.hasCallbackAddress()) {
            serializedIdBuilder.setCallbackAddress(serializeNetworkAddress(identity.fetchCallbackAddress()));
        }

        return serializedIdBuilder.build();
    }

    public static DialogsPublicIdentity deserializeIdentity(Comms.Identity identity) {
        String id = identity.getId();
        Comms.PublicKey publicKey = identity.getPublicKey();
        CipherPublicKey CipherPublicKey = deserializePublicKey(publicKey);

        if (identity.hasCallbackAddress()) {
            return new DialogsPublicIdentityBuilder().fixId(id).assignPublicKey(CipherPublicKey).setCallbackAddress(deserializeNetworkAddress(identity.getCallbackAddress())).formDialogsPublicIdentity();
        }

        return new DialogsPublicIdentityBuilder().fixId(id).assignPublicKey(CipherPublicKey).formDialogsPublicIdentity();
    }
}
