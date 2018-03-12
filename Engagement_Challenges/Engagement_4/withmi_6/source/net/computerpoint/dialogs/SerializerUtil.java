package net.computerpoint.dialogs;

import net.computerpoint.numerical.RsaPublicKey;
import net.computerpoint.numerical.PrivateCommsUtil;
import com.google.protobuf.ByteString;

import java.math.BigInteger;

/**
 * Serializes and deserializes Comms messages
 */
public class SerializerUtil {

    public static Comms.PublicKey serializePublicKey(RsaPublicKey publicKey) {
        Comms.PublicKey protocolsPublicKey = Comms.PublicKey.newBuilder()
                .setE(ByteString.copyFrom(publicKey.getE().toByteArray()))
                .setModulus(ByteString.copyFrom(publicKey.getModulus().toByteArray()))
                .build();
        return protocolsPublicKey;
    }

    public static RsaPublicKey deserializePublicKey(Comms.PublicKey publicKey) {
        byte[] e = publicKey.getE().toByteArray();
        byte[] divisor = publicKey.getModulus().toByteArray();
        return new RsaPublicKey(PrivateCommsUtil.toBigInt(divisor), PrivateCommsUtil.toBigInt(e));
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
        return new ProtocolsNetworkAddress(place, port);
    }

    public static Comms.NetworkAddress serializeNetworkAddress(ProtocolsNetworkAddress callbackAddress) {
        if (callbackAddress == null) {
            return null;
        }

        Comms.NetworkAddress address = Comms.NetworkAddress.newBuilder()
                .setHost(callbackAddress.grabPlace())
                .setPort(callbackAddress.pullPort())
                .build();

        return address;
    }

    public static ProtocolsPublicIdentity deserializeIdentity(Comms.Identity identity) {
        String id = identity.getId();
        Comms.PublicKey publicKey = identity.getPublicKey();
        RsaPublicKey PrivateCommsPublicKey = deserializePublicKey(publicKey);

        if (identity.hasCallbackAddress()) {
            return new ProtocolsPublicIdentityBuilder().setId(id).fixPublicKey(PrivateCommsPublicKey).assignCallbackAddress(deserializeNetworkAddress(identity.getCallbackAddress())).formProtocolsPublicIdentity();
        }

        return new ProtocolsPublicIdentityBuilder().setId(id).fixPublicKey(PrivateCommsPublicKey).formProtocolsPublicIdentity();
    }
}
