package stac.crypto;

import stac.parser.OpenSSLRSAPEM;

/**
 *
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
}
