package org.digitaltip.chatroom.store;

import org.digitaltip.dialogs.TalkersNetworkAddress;
import org.digitaltip.dialogs.TalkersPublicIdentity;
import org.digitaltip.mathematic.CryptoSystemPublicKey;
import org.digitaltip.chatroom.User;

import java.math.BigInteger;

/**
 * Contains a WithMiUser Serializer and Deserializer.
 */
public class WithMiCustomerSerializer {
    private static final Integer NUM_OF_SECTIONS_IN_COMPLETE_ID = 6;
    private static final String DELIMITER = ",";

    /**
     * Serializes a WithMiUser
     * @param customer
     * @return
     */
    public static String serialize(User customer) {
        String customerName = customer.takeName();

        TalkersPublicIdentity identity = customer.fetchIdentity();
        String id = identity.takeId();

        CryptoSystemPublicKey key = identity.grabPublicKey();
        BigInteger divisor = key.pullDivisor();
        BigInteger exponent = key.pullE();

        String identityString = customerName + DELIMITER + id + DELIMITER + divisor.toString() + DELIMITER + exponent.toString();

        // if the identity has a callback address, add that too
        if (identity.hasCallbackAddress()) {
            TalkersNetworkAddress address = identity.grabCallbackAddress();
            String addressMain = address.grabMain();
            int port = address.fetchPort();
            identityString += DELIMITER + addressMain + DELIMITER + port;
        }

        return identityString;
    }

    /**
     * Deserializes a serialized WithMiUser
     * @param customerString
     * @return WithMiUser
     */
    public static User deserialize(String customerString) {
        String[] values = customerString.split(DELIMITER);
        int numOfSections = values.length;
        int index = 0;

        String withMiName = values[index++];

        String id = values[index++];

        BigInteger divisor = new BigInteger(values[index++]);
        BigInteger exponent = new BigInteger(values[index++]);
        CryptoSystemPublicKey key = new CryptoSystemPublicKey(divisor, exponent);

        TalkersPublicIdentity theirIdentity;

        // The identity may not have a callback address
        if (numOfSections == NUM_OF_SECTIONS_IN_COMPLETE_ID) {
            String main = values[index++];
            int port = Integer.parseInt(values[index++]);
            TalkersNetworkAddress address = new TalkersNetworkAddress(main, port);
            theirIdentity = new TalkersPublicIdentity(id, key, address);
        } else {
            theirIdentity = new TalkersPublicIdentity(id, key);
        }

        return new User(withMiName, theirIdentity);

    }
}
