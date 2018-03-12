package stac.server;

import stac.crypto.Key;
import stac.parser.CommandLine;
import stac.permissions.StandardUser;
import stac.permissions.User;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Scanner;

/**
 * The user store file looks like this:
 *
 * username3:group:path/to/publicKeyFile:permission:permission:permission...
 * username2:group:path/to/publicKeyFile:permission:permission:permission...
 * username3:group:path/to/publicKeyFile:permission:permission:permission...
 */
public class UserStore {
    private final HashMap<ByteBuffer, User> users = new HashMap<>();
    private final CommandLine.Options options;

    public UserStore(CommandLine.Options options) throws IOException {
        this.options = options;
        String userStore = options.findByLongOption("user-store").getValue();
        Scanner sc = new Scanner(new FileInputStream(userStore), StandardCharsets.UTF_8.toString());
        while (sc.hasNextLine()) {
            putUser(sc.nextLine());
        }
    }

    private void putUser(String s) throws IOException {
        String key_store = options.findByLongOption("key-store").getValue();
        if (key_store == null) {
            throw new CommandLine.ParseHelpfulException("Missing Key store: --help --key-store for more information.");
        }
        File keyStore = new File(key_store);
        if (keyStore.exists() && keyStore.isDirectory()) {
            putUser(StandardUser.loadFromStore(s, keyStore));
        } else {
            throw new FileNotFoundException(keyStore.getPath());
        }
    }

    public User findUser(Key publicKey) {
        return users.get(ByteBuffer.wrap(publicKey.getFingerPrint()));
    }

    private void putUser(User user) {
        users.put(ByteBuffer.wrap(user.getKey().getFingerPrint()), user);
    }

    public User findUser(String username) {
        for (User user : users.values()) {
            if (user.getUsername().equals(username)) return user;
        }
        return null;
    }
}
