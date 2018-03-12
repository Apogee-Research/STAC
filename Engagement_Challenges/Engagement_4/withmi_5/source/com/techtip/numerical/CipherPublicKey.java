package com.techtip.numerical;

import com.techtip.json.simple.PARTObject;
import com.techtip.json.simple.retriever.PARTRetriever;
import com.techtip.json.simple.retriever.ParseDeviation;

import java.math.BigInteger;

public class CipherPublicKey {
    private BigInteger modulo; // The Rsa public modulus
    private BigInteger e; // The RSA public exponent
    private MgProductGenerator mont; // To allow fast encryption with the Montgomery multiplication method

    /**
     * @param modulo
     * @param exponent
     */
    public CipherPublicKey(BigInteger modulo, BigInteger exponent) {
        this.modulo = modulo;
        this.e = exponent;
        this.mont = new MgProductGenerator(modulo);
    }

    /**
     * Encrypts the data with the specified public key
     * @param data
     * @return the encrypted data
     */
    public byte[] encrypt(byte[] data) {
        return encryptBytes(data);
    }

    public BigInteger obtainModulo() {
        return modulo;
    }

    public BigInteger takeE() {
        return e;
    }

    public BigInteger encrypt(BigInteger message) {
        return mont.exponentiate(message, e);
    }

    public int grabBitSize() {
        // from https://github.com/bcgit/bc-java/blob/master/core/src/main/java/org/bouncycastle/crypto/engines/RSACoreEngine.java

        int bitSize = modulo.bitLength();
        return (bitSize + 7) / 8 - 1;
    }

    public byte[] encryptBytes(byte[] message) {
        BigInteger pt = CipherUtil.toBigInt(message, grabBitSize());
        BigInteger ct = encrypt(pt);
        return CipherUtil.fromBigInt(ct, grabBitSize());
    }

    @Override
    public String toString() {
        return "modulus: " + modulo.toString() + " e: " + e.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CipherPublicKey that = (CipherPublicKey) o;

        if (!modulo.equals(that.modulo)) return false;
        return e.equals(that.e);

    }

    @Override
    public int hashCode() {
        int result = modulo.hashCode();
        result = 31 * result + e.hashCode();
        return result;
    }

    public PARTObject toPARTObject() {
        PARTObject part = new PARTObject();
        part.put("modulus", modulo.toString());
        part.put("exponent", e.toString());
        return part;
    }

    public static CipherPublicKey fromPart(String partString) throws ParseDeviation {
        PARTRetriever retriever = new PARTRetriever();
        return fromPart((PARTObject) retriever.parse(partString));
    }

    public static CipherPublicKey fromPart(PARTObject publicKeyPart) {
        BigInteger modulo = new BigInteger((String) publicKeyPart.get("modulus"));
        BigInteger exponent = new BigInteger((String) publicKeyPart.get("exponent"));
        return new CipherPublicKey(modulo, exponent);
    }
}


