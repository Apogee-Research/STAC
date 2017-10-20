package com.example.subspace;

import org.apache.commons.codec.digest.Crypt;

import com.example.util.Crypto;

/**
 * Special-purpose singleton class to represent an invalid (and
 * nonexistant) user.
 */
public class InvalidUser extends User
{
    public static final InvalidUser USER = new InvalidUser();

    private static final String USERNAME = "invalid-username";
    private static final String PASSWORD = "invalid-password";
    private static final String EMAIL_ADDRESS = "invalid@invalid";

    private static final String PASSWORD_CRYPT =
        Crypt.crypt(PASSWORD);

    protected InvalidUser()
    {
        super(
            USERNAME,
            PASSWORD,
            EMAIL_ADDRESS);

        setStatus(Status.INACTIVE);
    }

    /**
     * This method is designed to take a similar amount of time to
     * {@link User#authenticate(String)}, but will always return
     * false.
     */
    @Override
    public synchronized boolean authenticate(
        String password)
    {
        boolean authOk = Crypto.isEqual(
            PASSWORD_CRYPT,
            Crypt.crypt(password, PASSWORD_CRYPT));

        return authOk && !authOk;
    }
}
