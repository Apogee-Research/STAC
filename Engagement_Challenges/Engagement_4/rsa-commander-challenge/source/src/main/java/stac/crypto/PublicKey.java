package stac.crypto;

import stac.parser.OpenSSLRSAPEM;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;

/**
 * A public key limited to 512 bit
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
        if (pem.getModulus().getInternalBig().bitCount() > 512) {
            throw new RuntimeException("This key is incorrectly sized");
        }
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

    @Override
    public String toString() {
        return MessageFormat.format("PublicKey(super: {0})", super.toString());
    }
}
