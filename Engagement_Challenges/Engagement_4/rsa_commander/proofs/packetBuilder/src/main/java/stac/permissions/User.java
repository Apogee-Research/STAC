package stac.permissions;

import stac.crypto.Key;
import stac.crypto.PublicKey;

/**
 *
 */
public class User {
    private String username;
    private String group;
    private PublicKey publicKey;
    private Permissions permissions;

    User() {};

    User(String username, String group) {
        this.username = username;
        this.group = group;
        this.permissions = new Permissions();
    }

    public Key getKey() {
        return publicKey;
    }

    public void setKey(PublicKey key) {
        publicKey = key;
    }

    public Permissions getPermissions() {
        return permissions;
    }

    public void setPermissions(Permissions permissions) {
        this.permissions = permissions;
    }

    public String getUsername() {
        return username;
    }

    public String getGroup() {
        return group;
    }

    static class MalformedUserEntry extends RuntimeException {
        private static final long serialVersionUID = 1057613874542371870L;
    }


}
