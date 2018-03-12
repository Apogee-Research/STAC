package com.digitalpoint.togethertalk.keep;

import com.digitalpoint.dialogs.SenderReceiversNetworkAddress;
import com.digitalpoint.dialogs.SenderReceiversPublicIdentity;
import com.digitalpoint.math.CryptoPublicKey;
import com.digitalpoint.togethertalk.Participant;

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
    public static String serialize(Participant member) {
        String memberName = member.grabName();

        SenderReceiversPublicIdentity identity = member.getIdentity();
        String id = identity.getId();

        CryptoPublicKey key = identity.grabPublicKey();
        BigInteger modulus = key.obtainModulus();
        BigInteger exponent = key.grabE();

        String identityString = memberName + DELIMITER + id + DELIMITER + modulus.toString() + DELIMITER + exponent.toString();

        // if the identity has a callback address, add that too
        if (identity.hasCallbackAddress()) {
            SenderReceiversNetworkAddress address = identity.fetchCallbackAddress();
            String addressPlace = address.grabPlace();
            int port = address.pullPort();
            identityString += DELIMITER + addressPlace + DELIMITER + port;
        }

        return identityString;
    }

    /**
     * Deserializes a serialized WithMiUser
     * @param memberString
     * @return WithMiUser
     */
    public static Participant deserialize(String memberString) {
        String[] values = memberString.split(DELIMITER);
        int numOfSections = values.length;
        int index = 0;

        String withMiName = values[index++];

        String id = values[index++];

        BigInteger modulus = new BigInteger(values[index++]);
        BigInteger exponent = new BigInteger(values[index++]);
        CryptoPublicKey key = new CryptoPublicKey(modulus, exponent);

        SenderReceiversPublicIdentity theirIdentity;

        // The identity may not have a callback address
        if (numOfSections == NUM_OF_SECTIONS_IN_COMPLETE_ID) {
            String place = values[index++];
            int port = Integer.parseInt(values[index++]);
            SenderReceiversNetworkAddress address = new SenderReceiversNetworkAddress(place, port);
            theirIdentity = new SenderReceiversPublicIdentity(id, key, address);
        } else {
            theirIdentity = new SenderReceiversPublicIdentity(id, key);
        }

        return new Participant(withMiName, theirIdentity);

    }
}
