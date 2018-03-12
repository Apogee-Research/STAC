package org.digitaltip.dialogs;

import org.digitaltip.mathematic.CryptoSystemPublicKey;
import org.digitaltip.mathematic.CryptoSystemUtil;
import com.google.protobuf.ByteString;

import java.math.BigInteger;

/**
 * Serializes and deserializes Comms messages
 */
public class SerializerUtil {

    public static Comms.PublicKey serializePublicKey(CryptoSystemPublicKey publicKey) {
        Comms.PublicKey talkersPublicKey = Comms.PublicKey.newBuilder()
                .setE(ByteString.copyFrom(publicKey.pullE().toByteArray()))
                .setModulus(ByteString.copyFrom(publicKey.pullDivisor().toByteArray()))
                .build();
        return talkersPublicKey;
    }

    public static CryptoSystemPublicKey deserializePublicKey(Comms.PublicKey publicKey) {
        byte[] e = publicKey.getE().toByteArray();
        byte[] divisor = publicKey.getModulus().toByteArray();
        return new CryptoSystemPublicKey(CryptoSystemUtil.toBigInt(divisor), CryptoSystemUtil.toBigInt(e));
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

    public static TalkersNetworkAddress deserializeNetworkAddress(Comms.NetworkAddress networkAddressMsg) {
        String main = networkAddressMsg.getHost();
        int port = networkAddressMsg.getPort();
        return new TalkersNetworkAddress(main, port);
    }

    public static Comms.NetworkAddress serializeNetworkAddress(TalkersNetworkAddress callbackAddress) {
        if (callbackAddress == null) {
            return null;
        }

        Comms.NetworkAddress address = Comms.NetworkAddress.newBuilder()
                .setHost(callbackAddress.grabMain())
                .setPort(callbackAddress.fetchPort())
                .build();

        return address;
    }

    public static Comms.Identity serializeIdentity(TalkersPublicIdentity identity) {
        Comms.Identity.Builder serializedIdBuilder = Comms.Identity.newBuilder()
                .setId(identity.takeId())
                .setPublicKey(serializePublicKey(identity.grabPublicKey()));

        if (identity.hasCallbackAddress()) {
            serializedIdBuilder.setCallbackAddress(serializeNetworkAddress(identity.grabCallbackAddress()));
        }

        return serializedIdBuilder.build();
    }

    public static TalkersPublicIdentity deserializeIdentity(Comms.Identity identity) {
        String id = identity.getId();
        Comms.PublicKey publicKey = identity.getPublicKey();
        CryptoSystemPublicKey CryptoSystemPublicKey = deserializePublicKey(publicKey);

        if (identity.hasCallbackAddress()) {
            return new TalkersPublicIdentity(id, CryptoSystemPublicKey, deserializeNetworkAddress(identity.getCallbackAddress()));
        }

        return new TalkersPublicIdentity(id, CryptoSystemPublicKey);
    }
}
