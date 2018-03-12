package stac.permissions;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.assertTrue;

/**
 *
 */
public class PermissionsTest {
    @Test
    public void putPermissionExecute() throws Exception {
        Permissions permissions = new Permissions();
        permissions.putPermission("Execute");
        assertTrue(permissions.hasPermission("Execute"));
        assertTrue(permissions.hasPermission(Permissions.ACLEntry.Execute));
    }


    @Test
    public void putPermissionMessage() throws Exception {
        Permissions permissions = new Permissions();
        permissions.putPermission("Message");
        assertTrue(permissions.hasPermission("Message"));
        assertTrue(permissions.hasPermission(Permissions.ACLEntry.Message));
    }

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void putPermissionNoSuchPermission() throws Exception {
        Permissions permissions = new Permissions();

        expectedException.expect(NoSuchPermission.class);

        permissions.putPermission("NotAPerm");
    }
}