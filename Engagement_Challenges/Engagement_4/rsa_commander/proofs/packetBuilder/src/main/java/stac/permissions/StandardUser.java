package stac.permissions;

import stac.crypto.PublicKey;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Scanner;

/**
 *
 */
public class StandardUser extends User {
    private StandardUser(String username, String group, File pemFile) throws IOException {
        this(username, group, new PublicKey(pemFile));
    }

    public StandardUser(String username, String group, PublicKey key) throws IOException {
        super(username, group);
        setKey(key);
    }

    public static User loadFromStore(String s, File storePath) throws IOException {
        User user;
        Scanner scanner = new Scanner(s);
        scanner.useDelimiter(":");
        if (!scanner.hasNext()) throw new MalformedUserEntry();
        String username = scanner.next();

        if (!scanner.hasNext()) throw new MalformedUserEntry();
        String group = scanner.next();

        if (!scanner.hasNext()) throw new MalformedUserEntry();
        String keyFile = scanner.next();

        File file = Paths.get(storePath.toString(), keyFile).toFile();

        if (file.getCanonicalFile().equals(file.getAbsoluteFile())) {
            if (file.exists()) {
                user = new StandardUser(username, group, file);
                Permissions permissions = user.getPermissions();
                while (scanner.hasNext()) {
                    permissions.putPermission(scanner.next());
                }
            } else {
                throw new MalformedUserEntry();
            }
        } else {
            throw new MalformedUserEntry();
        }

        return user;
    }
}
