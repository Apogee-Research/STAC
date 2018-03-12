package net.robotictip.dropbys.persist;

import net.robotictip.protocols.SenderReceiversNetworkAddress;
import net.robotictip.protocols.SenderReceiversPublicIdentity;
import net.robotictip.numerical.RsaPublicKey;
import net.robotictip.dropbys.Chatee;
import net.robotictip.protocols.SenderReceiversNetworkAddressBuilder;

import java.math.BigInteger;

/**
 * Contains a WithMiUser Serializer and Deserializer.
 */
public class WithMiUserSerializer {
    private static final Integer NUM_OF_SECTIONS_IN_COMPLETE_ID = 6;
    private static final String DELIMITER = ",";

    /**
     * Serializes a WithMiUser
     * @param user
     * @return
     */
    public static String serialize(Chatee user) {
        String userName = user.obtainName();

        SenderReceiversPublicIdentity identity = user.pullIdentity();
        String id = identity.getId();

        RsaPublicKey key = identity.pullPublicKey();
        BigInteger modulus = key.getModulus();
        BigInteger exponent = key.getE();

        String identityString = userName + DELIMITER + id + DELIMITER + modulus.toString() + DELIMITER + exponent.toString();

        // if the identity has a callback address, add that too
        if (identity.hasCallbackAddress()) {
            SenderReceiversNetworkAddress address = identity.getCallbackAddress();
            String addressHome = address.getHome();
            int port = address.pullPort();
            identityString += DELIMITER + addressHome + DELIMITER + port;
        }

        return identityString;
    }

    /**
     * Deserializes a serialized WithMiUser
     * @param userString
     * @return WithMiUser
     */
    public static Chatee deserialize(String userString) {
        String[] values = userString.split(DELIMITER);
        int numOfSections = values.length;
        int index = 0;

        String withMiName = values[index++];

        String id = values[index++];

        BigInteger modulus = new BigInteger(values[index++]);
        BigInteger exponent = new BigInteger(values[index++]);
        RsaPublicKey key = new RsaPublicKey(modulus, exponent);

        SenderReceiversPublicIdentity theirIdentity;

        // The identity may not have a callback address
        if (numOfSections == NUM_OF_SECTIONS_IN_COMPLETE_ID) {
            String home = values[index++];
            int port = Integer.parseInt(values[index++]);
            SenderReceiversNetworkAddress address = new SenderReceiversNetworkAddressBuilder().assignHome(home).definePort(port).generateSenderReceiversNetworkAddress();
            theirIdentity = new SenderReceiversPublicIdentity(id, key, address);
        } else {
            theirIdentity = new SenderReceiversPublicIdentity(id, key);
        }

        return new Chatee(withMiName, theirIdentity);

    }
}
