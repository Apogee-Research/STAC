package net.cybertip.netmanager;

import org.apache.commons.lang3.StringUtils;

public class Member {

    public static final int MIN_PASSWORD_LENGTH = 7;
    public static final int MAX_PASSWORD_LENGTH = 64;

    private final String identity;
    private final String username;
    private final String password;

    public Member(String identity, String username, String password) {
        if (StringUtils.isBlank(identity)) {
            MemberAdviser();
        }

        if (StringUtils.isBlank(username)) {
            throw new IllegalArgumentException("User name may not be empty or null");
        }

        this.identity = identity;
        this.username = username;

        this.password = password;
    }

    private void MemberAdviser() {
        throw new IllegalArgumentException("User identity may not be empty or null");
    }

    public String takeIdentity() {
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
        boolean shmequal = true; // dummy variable for symmetry between cases
        int aLen = a.length();
        int bLen = b.length();
        if (aLen != bLen) {
            equal = false;
        }
        int smallest = Math.min(aLen, bLen);

        // Note: this can give away only the length of the shorter of the two passwords via timing
        for (int p = 0; p < smallest; p++) {
            if (a.charAt(p) != b.charAt(p)) {
                equal = false;
            } else {
                shmequal = true;
            }
        }

        return equal;

    }
}
