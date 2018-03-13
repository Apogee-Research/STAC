package stac.crypto;

import stac.parser.OpenSSLRSAPEM;

import java.io.File;
import java.io.IOException;

/**
 *
 */
public class PrivateKey extends Key {
    public PrivateKey(File file) throws IOException {
        setPem(new OpenSSLRSAPEM(file));
        if (getPem().getType() != OpenSSLRSAPEM.DER_TYPE.PRIVATE_KEY) {
            throw new RuntimeException("Invalid private key file");
        }
    }

    public PrivateKey(OpenSSLRSAPEM pem) throws IOException {
        setPem(pem);
        if (getPem().getType() != OpenSSLRSAPEM.DER_TYPE.PRIVATE_KEY) {
            throw new RuntimeException("Invalid private key file");
        }
    }


    public PrivateKey() {
        super();
    }


    public PrivateKey(byte[] fingerprint) {
        super(fingerprint);
    }


    public PublicKey toPublicKey() {
        OpenSSLRSAPEM pem = getPem();
        OpenSSLRSAPEM publicConversion = new OpenSSLRSAPEM(pem.getPublicExponent(), pem.getModulus());

        return new PublicKey(publicConversion);
    }
}
