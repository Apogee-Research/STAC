package stac.crypto;

import stac.parser.OpenSSLRSAPEM;

import java.io.File;
import java.io.IOException;

/**
 *
 */
public class PublicKey extends Key {
    public PublicKey(File file) throws IOException {

        OpenSSLRSAPEM finalPem, pem = new OpenSSLRSAPEM(file);
        if (pem.getType() != OpenSSLRSAPEM.DER_TYPE.PUBLIC_KEY) {
            System.err.println("Warning: Invalid public key file '" + file.getPath() + "'. Auto-converting it.");
            finalPem = new OpenSSLRSAPEM(pem.getPublicExponent(), pem.getModulus());
        } else {
            finalPem = pem;
        }

        setPem(finalPem);
    }

    public PublicKey(OpenSSLRSAPEM pem) {
        OpenSSLRSAPEM finalPem;
        if (pem.getType() != OpenSSLRSAPEM.DER_TYPE.PUBLIC_KEY) {
            System.err.println("Warning: Invalid public key. Auto-converting it.");
            finalPem = new OpenSSLRSAPEM(pem.getPublicExponent(), pem.getModulus());
        } else {
            finalPem = pem;
        }
        setPem(finalPem);
    }
    
    public PublicKey(byte[] fingerprint) {
        super(fingerprint);
    }

    public PublicKey() {
        super();
    }
}
