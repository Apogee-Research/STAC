package stac.permissions;

import java.util.TreeSet;

/**
 *
 */
public class Permissions {
    private TreeSet<ACLEntry> permissions = new TreeSet<>();

    public void putPermission(String next) {
        try {
            permissions.add(ACLEntry.valueOf(next));
        } catch (IllegalArgumentException e) {
            throw new NoSuchPermission();
        }
    }

    public boolean hasPermission(String permissionName) {
        return permissions.contains(ACLEntry.valueOf(permissionName));
    }

    public boolean hasPermission(ACLEntry permission) {
        return permissions.contains(permission);
    }

    public enum ACLEntry {
        Message, Execute
    }

}
