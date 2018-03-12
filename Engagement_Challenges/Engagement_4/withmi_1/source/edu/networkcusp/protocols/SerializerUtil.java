package edu.networkcusp.protocols;

import edu.networkcusp.math.CryptoPublicKey;
import edu.networkcusp.math.CryptoUtil;
import com.google.protobuf.ByteString;

import java.math.BigInteger;

/**
 * Serializes and deserializes Comms messages
 */
public class SerializerUtil {

    public static Comms.PublicKey serializePublicKey(CryptoPublicKey publicKey) {
        Comms.PublicKey communicationsPublicKey = Comms.PublicKey.newBuilder()
                .setE(ByteString.copyFrom(publicKey.getE().toByteArray()))
                .setModulus(ByteString.copyFrom(publicKey.takeFloormod().toByteArray()))
                .build();
        return communicationsPublicKey;
    }

    public static CryptoPublicKey deserializePublicKey(Comms.PublicKey publicKey) {
        byte[] e = publicKey.getE().toByteArray();
        byte[] floormod = publicKey.getModulus().toByteArray();
        return new CryptoPublicKey(CryptoUtil.toBigInt(floormod), CryptoUtil.toBigInt(e));
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
                .setHost(callbackAddress.getHost())
                .setPort(callbackAddress.pullPort())
                .build();

        return address;
    }

    public static CommunicationsPublicIdentity deserializeIdentity(Comms.Identity identity) {
        String id = identity.getId();
        Comms.PublicKey publicKey = identity.getPublicKey();
        CryptoPublicKey CryptoPublicKey = deserializePublicKey(publicKey);

        if (identity.hasCallbackAddress()) {
            return new CommunicationsPublicIdentity(id, CryptoPublicKey, deserializeNetworkAddress(identity.getCallbackAddress()));
        }

        return new CommunicationsPublicIdentity(id, CryptoPublicKey);
    }
}
