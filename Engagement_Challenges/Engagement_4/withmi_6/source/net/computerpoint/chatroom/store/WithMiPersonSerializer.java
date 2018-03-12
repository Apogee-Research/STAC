package net.computerpoint.chatroom.store;

import net.computerpoint.dialogs.ProtocolsNetworkAddress;
import net.computerpoint.dialogs.ProtocolsPublicIdentity;
import net.computerpoint.dialogs.ProtocolsPublicIdentityBuilder;
import net.computerpoint.numerical.RsaPublicKey;
import net.computerpoint.chatroom.Participant;

import java.math.BigInteger;

/**
 * Contains a WithMiUser Serializer and Deserializer.
 */
public class WithMiPersonSerializer {
    private static final Integer NUM_OF_SECTIONS_IN_COMPLETE_ID = 6;
    private static final String DELIMITER = ",";

    /**
     * Serializes a WithMiUser
     * @param person
     * @return
     */
    public static String serialize(Participant person) {
        String personName = person.getName();

        ProtocolsPublicIdentity identity = person.fetchIdentity();
        String id = identity.fetchId();

        RsaPublicKey key = identity.takePublicKey();
        BigInteger divisor = key.getModulus();
        BigInteger exponent = key.getE();

        String identityString = personName + DELIMITER + id + DELIMITER + divisor.toString() + DELIMITER + exponent.toString();

        // if the identity has a callback address, add that too
        if (identity.hasCallbackAddress()) {
            ProtocolsNetworkAddress address = identity.fetchCallbackAddress();
            String addressPlace = address.grabPlace();
            int port = address.pullPort();
            identityString += DELIMITER + addressPlace + DELIMITER + port;
        }

        return identityString;
    }

    /**
     * Deserializes a serialized WithMiUser
     * @param personString
     * @return WithMiUser
     */
    public static Participant deserialize(String personString) {
        String[] values = personString.split(DELIMITER);
        int numOfSections = values.length;
        int index = 0;

        String withMiName = values[index++];

        String id = values[index++];

        BigInteger divisor = new BigInteger(values[index++]);
        BigInteger exponent = new BigInteger(values[index++]);
        RsaPublicKey key = new RsaPublicKey(divisor, exponent);

        ProtocolsPublicIdentity theirIdentity;

        // The identity may not have a callback address
        if (numOfSections == NUM_OF_SECTIONS_IN_COMPLETE_ID) {
            String place = values[index++];
            int port = Integer.parseInt(values[index++]);
            ProtocolsNetworkAddress address = new ProtocolsNetworkAddress(place, port);
            theirIdentity = new ProtocolsPublicIdentityBuilder().setId(id).fixPublicKey(key).assignCallbackAddress(address).formProtocolsPublicIdentity();
        } else {
            theirIdentity = new ProtocolsPublicIdentityBuilder().setId(id).fixPublicKey(key).formProtocolsPublicIdentity();
        }

        return new Participant(withMiName, theirIdentity);

    }
}
