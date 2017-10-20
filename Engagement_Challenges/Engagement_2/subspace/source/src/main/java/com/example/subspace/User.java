package com.example.subspace;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.codec.digest.Crypt;

import com.example.util.Crypto;
import com.example.util.GeoPoint;

public class User
    implements Serializable
{
    private static final long serialVersionUID = 0L;

    /**
     * The beginning of any string that uses the current {@link
     * Crypt#crypt(String)} algorithm.
     */
    private static final String CURRENT_CRYPT_ALG;
    static
    {
        Pattern algPattern = Pattern.compile("^\\$[^\\$]+\\$");

        String crypt = Crypt.crypt("");

        Matcher algMatcher = algPattern.matcher(crypt);

        if (!algMatcher.find())
        {
            throw new RuntimeException(
                "Invalid crypt() output: " + crypt);
        }

        CURRENT_CRYPT_ALG = algMatcher.group();
    }

    /**
     * Represent the status of a user.
     */
    public static enum Status
    {
        /**
         * The most common state.
         */
        OK,

        /**
         * For new users before they've confirmed their registrations.
         */
        NEW,

        /**
         * For deleted users.
         */
        INACTIVE,
    }

    private Status mStatus;

    private final String mUsername;

    /**
     * The output of a crypt()-style hash of the user's password.
     */
    private String mPasswordCrypt;

    private final String mEmailAddress;

    /**
     * The user's current location, or null if the location is
     * unknown.
     */
    private GeoPoint mLocation;

    public User(
        String username,
        String password,
        String emailAddress)
    {
        mStatus = Status.NEW;

        mUsername = username;
        mPasswordCrypt = Crypt.crypt(password);
        mEmailAddress = emailAddress;

        mLocation = null;
    }

    public synchronized Status getStatus()
    {
        return mStatus;
    }

    public synchronized boolean isActive()
    {
        switch (mStatus)
        {
            case OK:
                return true;

            default:
                return false;
        }
    }

    public synchronized void setStatus(
        Status status)
    {
        mStatus = status;
    }

    public String getUsername()
    {
        return mUsername;
    }

    public String getEmailAddress()
    {
        return mEmailAddress;
    }

    public synchronized GeoPoint getLocation()
    {
        return mLocation;
    }

    public synchronized void setLocation(
        GeoPoint location)
    {
        mLocation = location;
    }

    /**
     * Attempt to authenticate the user.
     *
     * @param password
     *     The user's password.
     * @return
     *     True iff the authentication succeeds.
     */
    public synchronized boolean authenticate(
        String password)
    {
        // If you change this method's implementation, you should also
        // look at InvalidUser#authenticate(String) to ensure that
        // method still works as desired.

        boolean authOk = Crypto.isEqual(
            mPasswordCrypt,
            Crypt.crypt(password, mPasswordCrypt));
        if (!authOk)
        {
            return false;
        }

        if (!mPasswordCrypt.startsWith(CURRENT_CRYPT_ALG))
        {
            mPasswordCrypt = Crypt.crypt(password);
        }

        return true;
    }

    @Override
    public String toString()
    {
        return String.format(
            "%s[%s]",
            getClass().getName(),
            mUsername);
    }
}
