package org.digitalapex.talkers;

import org.digitalapex.math.CryptoPublicKey;
import org.digitalapex.math.CryptoUtil;
import com.google.protobuf.ByteString;

import java.math.BigInteger;

/**
 * Serializes and deserializes Comms messages
 */
public class SerializerUtil {

    public static CryptoPublicKey deserializePublicKey(Comms.PublicKey publicKey) {
        byte[] e = publicKey.getE().toByteArray();
        byte[] factor = publicKey.getModulus().toByteArray();
        return new CryptoPublicKey(CryptoUtil.toBigInt(factor), CryptoUtil.toBigInt(e));
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
                .setHost(callbackAddress.takeMain())
                .setPort(callbackAddress.fetchPort())
                .build();

        return address;
    }

    public static Comms.Identity serializeIdentity(TalkersPublicIdentity identity) {
        Comms.Identity.Builder serializedIdBuilder = Comms.Identity.newBuilder()
                .setId(identity.getId())
                .setPublicKey(identity.fetchPublicKey().serializePublicKey());

        if (identity.hasCallbackAddress()) {
            serializedIdBuilder.setCallbackAddress(serializeNetworkAddress(identity.pullCallbackAddress()));
        }

        return serializedIdBuilder.build();
    }

    public static TalkersPublicIdentity deserializeIdentity(Comms.Identity identity) {
        String id = identity.getId();
        Comms.PublicKey publicKey = identity.getPublicKey();
        CryptoPublicKey CryptoPublicKey = deserializePublicKey(publicKey);

        if (identity.hasCallbackAddress()) {
            return new TalkersPublicIdentityBuilder().fixId(id).setPublicKey(CryptoPublicKey).fixCallbackAddress(deserializeNetworkAddress(identity.getCallbackAddress())).generateTalkersPublicIdentity();
        }

        return new TalkersPublicIdentityBuilder().fixId(id).setPublicKey(CryptoPublicKey).generateTalkersPublicIdentity();
    }
}
