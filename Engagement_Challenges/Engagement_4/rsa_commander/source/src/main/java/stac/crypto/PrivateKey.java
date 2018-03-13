package stac.crypto;

import stac.parser.OpenSSLRSAPEM;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;

/**
 * A private key limited to size 512
 */
public class PrivateKey extends Key {
    public PrivateKey(File file) throws IOException {
        setPem(new OpenSSLRSAPEM(file));
        if (getPem().getType() != OpenSSLRSAPEM.DER_TYPE.PRIVATE_KEY) {
            throw new RuntimeException("Invalid private key file");
        }
    }

    public PrivateKey(OpenSSLRSAPEM pem) throws IOException {
        if (pem.getModulus().getInternalBig().bitCount() > 512) {
            throw new RuntimeException("This private key is not sized correctly");
        }
        setPem(pem);
        if (getPem().getType() != OpenSSLRSAPEM.DER_TYPE.PRIVATE_KEY) {
            throw new RuntimeException("Invalid private key file");
        }
    }

    public PrivateKey(byte[] fingerprint) {
        super(fingerprint);
    }

    public PrivateKey() {
        super();
    }


    public PublicKey toPublicKey() {
        OpenSSLRSAPEM pem = getPem();
        OpenSSLRSAPEM publicConversion = new OpenSSLRSAPEM(pem.getPublicExponent(), pem.getModulus());

        return new PublicKey(publicConversion);
    }

    @Override
    public String toString() {
        return MessageFormat.format("PrivateKey(super: {0})", super.toString());
    }
}
