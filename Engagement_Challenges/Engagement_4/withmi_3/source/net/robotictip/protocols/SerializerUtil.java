package net.robotictip.protocols;

import net.robotictip.numerical.RsaPublicKey;
import net.robotictip.numerical.CipherUtil;
import com.google.protobuf.ByteString;

import java.math.BigInteger;

/**
 * Serializes and deserializes Comms messages
 */
public class SerializerUtil {

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
        String home = networkAddressMsg.getHost();
        int port = networkAddressMsg.getPort();
        return new SenderReceiversNetworkAddressBuilder().assignHome(home).definePort(port).generateSenderReceiversNetworkAddress();
    }

    public static Comms.NetworkAddress serializeNetworkAddress(SenderReceiversNetworkAddress callbackAddress) {
        if (callbackAddress == null) {
            return null;
        }

        Comms.NetworkAddress address = Comms.NetworkAddress.newBuilder()
                .setHost(callbackAddress.getHome())
                .setPort(callbackAddress.pullPort())
                .build();

        return address;
    }

    public static Comms.Identity serializeIdentity(SenderReceiversPublicIdentity identity) {
        Comms.Identity.Builder serializedIdBuilder = Comms.Identity.newBuilder()
                .setId(identity.getId())
                .setPublicKey(identity.pullPublicKey().serializePublicKey());

        if (identity.hasCallbackAddress()) {
            serializeIdentityHome(identity, serializedIdBuilder);
        }

        return serializedIdBuilder.build();
    }

    private static void serializeIdentityHome(SenderReceiversPublicIdentity identity, Comms.Identity.Builder serializedIdBuilder) {
        serializedIdBuilder.setCallbackAddress(serializeNetworkAddress(identity.getCallbackAddress()));
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
