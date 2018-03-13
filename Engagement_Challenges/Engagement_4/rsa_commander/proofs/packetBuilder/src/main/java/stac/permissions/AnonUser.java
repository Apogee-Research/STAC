package stac.permissions;

import java.net.SocketAddress;

/**
 *
 */
public class AnonUser extends User {
    public AnonUser(SocketAddress remoteAddress) {
        super(remoteAddress != null ? remoteAddress.toString() : "Unknown", "Anonymous");
    }
}
