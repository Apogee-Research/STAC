package com.cyberpointllc.stac.webserver;

import org.apache.commons.lang3.StringUtils;

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
        Classmatches replacementClass = new  Classmatches(username, password);
        ;
        return replacementClass.doIt0();
    }

    private boolean passwordsEqual(String a, String b) {
        ClasspasswordsEqual replacementClass = new  ClasspasswordsEqual(a, b);
        ;
        replacementClass.doIt0();
        replacementClass.doIt1();
        replacementClass.doIt2();
        return replacementClass.doIt3();
    }

    public class Classmatches {

        public Classmatches(String username, String password) {
            this.username = username;
            this.password = password;
        }

        private String username;

        private String password;

        public boolean doIt0() {
            return User.this.username.equals(username) & passwordsEqual(User.this.password, password);
        }
    }

    private class ClasspasswordsEqual {

        public ClasspasswordsEqual(String a, String b) {
            this.a = a;
            this.b = b;
        }

        private String a;

        private String b;

        private boolean equal;

        private boolean shmequal;

        public void doIt0() {
            equal = true;
            shmequal = true;
        }

        private int aLen;

        public void doIt1() {
            aLen = a.length();
        }

        private int bLen;

        public void doIt2() {
            bLen = b.length();
            if (aLen != bLen) {
                equal = false;
            }
        }

        private int min;

        public boolean doIt3() {
            min = Math.min(aLen, bLen);
            // Note: this can give away only the length of the shorter of the two passwords via timing
            for (int i = 0; i < min; i++) {
                if (a.charAt(i) != b.charAt(i)) {
                    equal = false;
                } else {
                    shmequal = true;
                }
            }
            return equal;
        }
    }
}
