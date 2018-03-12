package stac.crypto;

import stac.parser.OpenSSLRSAPEM;

import java.text.MessageFormat;
import java.util.Arrays;

/**
 * Keys represent public and private keys.
 */
abstract public class Key {
    private OpenSSLRSAPEM pem;
    private byte[] fingerPrint;

    public Key() {
    }

    public Key(byte[] fingerprint) {
        this.pem = null;
        this.fingerPrint = fingerprint;
    }

    public OpenSSLRSAPEM getPem() {
        return pem;
    }

    public void setPem(OpenSSLRSAPEM pem) {
        this.pem = pem;
        this.fingerPrint = null;
        getFingerPrint();
    }

    public byte[] getFingerPrint() {
        return fingerPrint == null
                ? (fingerPrint = SHA256.digest(pem.getPublicExponent().getBytes(), pem.getModulus().getBytes()))
                : fingerPrint;
    }

    public void setFingerPrint(byte[] fingerPrint) {
        this.fingerPrint = fingerPrint;
    }

    @Override
    public String toString() {
        return MessageFormat.format("Key(pem: {0}, fingerPrint: {1}}", pem, Arrays.toString(fingerPrint));
    }

    public boolean matches(Key key) {
        boolean truth = fingerPrint.length == key.getFingerPrint().length;
        for (int i = 0; i < fingerPrint.length; i++) {
            truth &= fingerPrint[i] == key.getFingerPrint()[i];
        }
        return truth;
    }
}
