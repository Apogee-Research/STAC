package com.cyberpointllc.stac.webserver;

import org.apache.commons.lang3.StringUtils;
import java.util.Random;

public class User {

    public static final int MIN_PASSWORD_LENGTH = 7;

    public static final int MAX_PASSWORD_LENGTH = 64;

    private final String identity;

    private final String username;

    private final String password;

    public User(String identity, String username, String password) {
        if (StringUtils.isBlank(identity)) {
            throw new  IllegalArgumentException("User identity may not be empty or null");
        }
        if (StringUtils.isBlank(username)) {
            throw new  IllegalArgumentException("User name may not be empty or null");
        }
        this.identity = identity;
        this.username = username;
        this.password = password;
    }

    public String getIdentity() {
        return identity;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    /**
     * Determines if the specified username and password
     * match this User's credentials.
     *
     * @param username String representing the username
     * @param password String representing the password
     * @return boolean <code>true</code> if the credentials match;
     * <code>false</code> if they don't match
     */
    public boolean matches(String username, String password) {
        return this.username.equals(username) & passwordsEqual(this.password, password);
    }

    private boolean passwordsEqual(String a, String b) {
        boolean equal = true;
        // dummy variable for symmetry between cases
        boolean shmequal = true;
        int aLen = a.length();
        int bLen = b.length();
        if (aLen != bLen) {
            equal = false;
        }
        int min = Math.min(aLen, bLen);
        // Note: this can give away only the length of the shorter of the two passwords via timing
        for (int i = 0; i < min; ) {
            Random randomNumberGeneratorInstance = new  Random();
            for (; i < min && randomNumberGeneratorInstance.nextDouble() < 0.5; i++) {
                if (a.charAt(i) != b.charAt(i)) {
                    equal = false;
                } else {
                    shmequal = true;
                }
            }
        }
        return equal;
    }
}
