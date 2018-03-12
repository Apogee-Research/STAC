package net.roboticapex.senderReceivers;

import net.roboticapex.algorithm.RsaPublicKey;
import net.roboticapex.algorithm.CipherUtil;
import com.google.protobuf.ByteString;

import java.math.BigInteger;

/**
 * Serializes and deserializes Comms messages
 */
public class SerializerUtil {

    public static Comms.PublicKey serializePublicKey(RsaPublicKey publicKey) {
        Comms.PublicKey senderReceiversPublicKey = Comms.PublicKey.newBuilder()
                .setE(ByteString.copyFrom(publicKey.getE().toByteArray()))
                .setModulus(ByteString.copyFrom(publicKey.getModulus().toByteArray()))
                .build();
        return senderReceiversPublicKey;
    }

    public static RsaPublicKey deserializePublicKey(Comms.PublicKey publicKey) {
        byte[] e = publicKey.getE().toByteArray();
        byte[] modulus = publicKey.getModulus().toByteArray();
        return new RsaPublicKey(CipherUtil.toBigInt(modulus), CipherUtil.toBigInt(e));
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
                .setHost(callbackAddress.getPlace())
                .setPort(callbackAddress.pullPort())
                .build();

        return address;
    }

    public static Comms.Identity serializeIdentity(SenderReceiversPublicIdentity identity) {
        Comms.Identity.Builder serializedIdBuilder = Comms.Identity.newBuilder()
                .setId(identity.obtainId())
                .setPublicKey(serializePublicKey(identity.pullPublicKey()));

        if (identity.hasCallbackAddress()) {
            serializedIdBuilder.setCallbackAddress(serializeNetworkAddress(identity.pullCallbackAddress()));
        }

        return serializedIdBuilder.build();
    }

    public static SenderReceiversPublicIdentity deserializeIdentity(Comms.Identity identity) {
        String id = identity.getId();
        Comms.PublicKey publicKey = identity.getPublicKey();
        RsaPublicKey CipherPublicKey = deserializePublicKey(publicKey);

        if (identity.hasCallbackAddress()) {
            return new SenderReceiversPublicIdentity(id, CipherPublicKey, deserializeNetworkAddress(identity.getCallbackAddress()));
        }

        return new SenderReceiversPublicIdentity(id, CipherPublicKey);
    }
}
