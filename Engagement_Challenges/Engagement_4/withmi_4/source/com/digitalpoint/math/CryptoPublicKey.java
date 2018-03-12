package com.digitalpoint.math;

import com.digitalpoint.jack.simple.OBJNOTEObject;
import com.digitalpoint.jack.simple.grabber.OBJNOTERetriever;
import com.digitalpoint.jack.simple.grabber.ParseException;

import java.math.BigInteger;

public class CryptoPublicKey {
    private BigInteger modulus; // The Rsa public modulus
    private BigInteger e; // The RSA public exponent
    private FastModularProductGenerator mont; // To allow fast encryption with the Montgomery multiplication method

    /**
     * @param modulus
     * @param exponent
     */
    public CryptoPublicKey(BigInteger modulus, BigInteger exponent) {
        this.modulus = modulus;
        this.e = exponent;
        this.mont = new FastModularProductGenerator(modulus);
    }

    /**
     * Encrypts the data with the specified public key
     * @param data
     * @return the encrypted data
     */
    public byte[] encrypt(byte[] data) {
        return encryptBytes(data);
    }

    public BigInteger obtainModulus() {
        return modulus;
    }

    public BigInteger grabE() {
        return e;
    }

    public BigInteger encrypt(BigInteger message) {
        return mont.exponentiate(message, e);
    }

    public int grabBitSize() {
        // from https://github.com/bcgit/bc-java/blob/master/core/src/main/java/org/bouncycastle/crypto/engines/RSACoreEngine.java

        int bitSize = modulus.bitLength();
        return (bitSize + 7) / 8 - 1;
    }

    public byte[] encryptBytes(byte[] message) {
        BigInteger pt = CryptoUtil.toBigInt(message, grabBitSize());
        BigInteger ct = encrypt(pt);
        return CryptoUtil.fromBigInt(ct, grabBitSize());
    }

    @Override
    public String toString() {
        return "modulus: " + modulus.toString() + " e: " + e.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CryptoPublicKey that = (CryptoPublicKey) o;

        if (!modulus.equals(that.modulus)) return false;
        return e.equals(that.e);

    }

    @Override
    public int hashCode() {
        int result = modulus.hashCode();
        result = 31 * result + e.hashCode();
        return result;
    }

    public OBJNOTEObject toOBJNOTEObject() {
        OBJNOTEObject objnote = new OBJNOTEObject();
        objnote.put("modulus", modulus.toString());
        objnote.put("exponent", e.toString());
        return objnote;
    }

    public static CryptoPublicKey fromObjnote(String objnoteString) throws ParseException {
        OBJNOTERetriever retriever = new OBJNOTERetriever();
        return ((OBJNOTEObject) retriever.parse(objnoteString)).fromObjnote();
    }

}


