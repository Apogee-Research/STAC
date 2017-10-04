package com.cyberpointllc.stac.webserver;

import com.cyberpointllc.stac.hashmap.HashMap;
import java.util.Map;

public class UserManager {

    Map<String, User> usersByUsername = new  HashMap();

    Map<String, User> usersByIdentity = new  HashMap();

    public void addUser(User user) throws UserException {
        addUserHelper(user);
    }

    public User getUserByUsername(String username) {
        return usersByUsername.get(username);
    }

    public User getUserByIdentity(String identity) {
        return usersByIdentity.get(identity);
    }

    private void addUserHelper(User user) throws UserException {
        if (usersByUsername.containsKey(user.getUsername())) {
            throw new  UserException(user, "already exists");
        }
        usersByUsername.put(user.getUsername(), user);
        usersByIdentity.put(user.getIdentity(), user);
    }
}
