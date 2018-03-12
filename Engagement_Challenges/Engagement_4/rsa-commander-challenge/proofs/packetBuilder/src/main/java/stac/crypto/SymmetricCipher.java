package stac.crypto;

import stac.parser.OpenSSLRSAPEM;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Base class for encryption with symmetric ciphers.
 */
public abstract class SymmetricCipher {
    public abstract void encrypt_ctr(OpenSSLRSAPEM.INTEGER counter, byte[] key, ByteArrayInputStream is, ByteArrayOutputStream os) throws IOException;
}
