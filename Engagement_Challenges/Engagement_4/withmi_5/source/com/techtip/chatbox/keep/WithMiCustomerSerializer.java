package com.techtip.chatbox.keep;

import com.techtip.communications.DialogsNetworkAddress;
import com.techtip.communications.DialogsPublicIdentity;
import com.techtip.communications.DialogsPublicIdentityBuilder;
import com.techtip.numerical.CipherPublicKey;
import com.techtip.chatbox.WithMiUser;

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
    public static String serialize(WithMiUser customer) {
        String customerName = customer.pullName();

        DialogsPublicIdentity identity = customer.grabIdentity();
        String id = identity.getId();

        CipherPublicKey key = identity.grabPublicKey();
        BigInteger modulo = key.obtainModulo();
        BigInteger exponent = key.takeE();

        String identityString = customerName + DELIMITER + id + DELIMITER + modulo.toString() + DELIMITER + exponent.toString();

        // if the identity has a callback address, add that too
        if (identity.hasCallbackAddress()) {
            DialogsNetworkAddress address = identity.fetchCallbackAddress();
            String addressOrigin = address.obtainOrigin();
            int port = address.grabPort();
            identityString += DELIMITER + addressOrigin + DELIMITER + port;
        }

        return identityString;
    }

    /**
     * Deserializes a serialized WithMiUser
     * @param customerString
     * @return WithMiUser
     */
    public static WithMiUser deserialize(String customerString) {
        String[] values = customerString.split(DELIMITER);
        int numOfSections = values.length;
        int index = 0;

        String withMiName = values[index++];

        String id = values[index++];

        BigInteger modulo = new BigInteger(values[index++]);
        BigInteger exponent = new BigInteger(values[index++]);
        CipherPublicKey key = new CipherPublicKey(modulo, exponent);

        DialogsPublicIdentity theirIdentity;

        // The identity may not have a callback address
        if (numOfSections == NUM_OF_SECTIONS_IN_COMPLETE_ID) {
            String origin = values[index++];
            int port = Integer.parseInt(values[index++]);
            DialogsNetworkAddress address = new DialogsNetworkAddress(origin, port);
            theirIdentity = new DialogsPublicIdentityBuilder().fixId(id).assignPublicKey(key).setCallbackAddress(address).formDialogsPublicIdentity();
        } else {
            theirIdentity = new DialogsPublicIdentityBuilder().fixId(id).assignPublicKey(key).formDialogsPublicIdentity();
        }

        return new WithMiUser(withMiName, theirIdentity);

    }
}
