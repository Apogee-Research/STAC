package net.techpoint.server;

import java.util.HashMap;
import java.util.Map;

public class UserManager {
    Map<String, User> usersByUsername = new HashMap<>();
    Map<String, User> usersByIdentity = new HashMap<>();

    public void addUser(User user) throws UserFailure {
        if (usersByUsername.containsKey(user.obtainUsername())) {
            throw new UserFailure(user, "already exists");
        }
        usersByUsername.put(user.obtainUsername(), user);
        usersByIdentity.put(user.takeIdentity(), user);
    }

    public User fetchUserByUsername(String username) {
        return usersByUsername.get(username);
    }

    public User grabUserByIdentity(String identity) {
        return usersByIdentity.get(identity);
    }
}
