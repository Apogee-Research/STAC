package stac.crypto;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Also should be self-explanatory
 */
public class SHA256 {
    private MessageDigest msg;
    public SHA256() {
        try {
            msg = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public byte[] digest() {
        byte[] digest = msg.digest();
        msg = null;
        return digest;
    }

    public static byte[] digest(byte[]... array) {
        try {
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");

            for (byte[] bytes : array) {
                sha256.update(bytes);
            }

            return sha256.digest();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to acquire required crytographic libraries.");
        }
    }

    public void update(byte[] buffer, int bpos, int i) {
        if (msg == null) {
            throw new RuntimeException("Closed SHA256 update detected");
        }
        msg.update(buffer, bpos, i);
    }

    public void update(byte[] bytes) {
        if (msg == null) {
            throw new RuntimeException("Closed SHA256 update detected");
        }
        msg.update(bytes);
    }
}
