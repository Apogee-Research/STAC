package edu.networkcusp.chatbox.keep;

import edu.networkcusp.protocols.CommunicationsNetworkAddress;
import edu.networkcusp.protocols.CommunicationsPublicIdentity;
import edu.networkcusp.math.CryptoPublicKey;
import edu.networkcusp.chatbox.WithMiUser;

import java.math.BigInteger;

/**
 * Contains a WithMiUser Serializer and Deserializer.
 */
public class WithMiMemberSerializer {
    private static final Integer NUM_OF_SECTIONS_IN_COMPLETE_ID = 6;
    private static final String DELIMITER = ",";

    /**
     * Serializes a WithMiUser
     * @param member
     * @return
     */
    public static String serialize(WithMiUser member) {
        String memberName = member.obtainName();

        CommunicationsPublicIdentity identity = member.getIdentity();
        String id = identity.pullId();

        CryptoPublicKey key = identity.takePublicKey();
        BigInteger floormod = key.takeFloormod();
        BigInteger exponent = key.getE();

        String identityString = memberName + DELIMITER + id + DELIMITER + floormod.toString() + DELIMITER + exponent.toString();

        // if the identity has a callback address, add that too
        if (identity.hasCallbackAddress()) {
            CommunicationsNetworkAddress address = identity.obtainCallbackAddress();
            String addressHost = address.getHost();
            int port = address.pullPort();
            identityString += DELIMITER + addressHost + DELIMITER + port;
        }

        return identityString;
    }

    /**
     * Deserializes a serialized WithMiUser
     * @param memberString
     * @return WithMiUser
     */
    public static WithMiUser deserialize(String memberString) {
        String[] values = memberString.split(DELIMITER);
        int numOfSections = values.length;
        int index = 0;

        String withMiName = values[index++];

        String id = values[index++];

        BigInteger floormod = new BigInteger(values[index++]);
        BigInteger exponent = new BigInteger(values[index++]);
        CryptoPublicKey key = new CryptoPublicKey(floormod, exponent);

        CommunicationsPublicIdentity theirIdentity;

        // The identity may not have a callback address
        if (numOfSections == NUM_OF_SECTIONS_IN_COMPLETE_ID) {
            String host = values[index++];
            int port = Integer.parseInt(values[index++]);
            CommunicationsNetworkAddress address = new CommunicationsNetworkAddress(host, port);
            theirIdentity = new CommunicationsPublicIdentity(id, key, address);
        } else {
            theirIdentity = new CommunicationsPublicIdentity(id, key);
        }

        return new WithMiUser(withMiName, theirIdentity);

    }
}
