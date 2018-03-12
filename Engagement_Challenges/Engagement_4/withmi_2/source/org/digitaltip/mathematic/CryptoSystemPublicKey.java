package org.digitaltip.mathematic;

import org.digitaltip.objnote.simple.JACKSONObject;
import org.digitaltip.objnote.simple.grabber.JACKSONGrabber;
import org.digitaltip.objnote.simple.grabber.ParseDeviation;

import java.math.BigInteger;

public class CryptoSystemPublicKey {
    private BigInteger divisor; // The Rsa public modulus
    private BigInteger e; // The RSA public exponent
    private MontgomeryProductGenerator mont; // To allow fast encryption with the Montgomery multiplication method

    /**
     * @param divisor
     * @param exponent
     */
    public CryptoSystemPublicKey(BigInteger divisor, BigInteger exponent) {
        this.divisor = divisor;
        this.e = exponent;
        this.mont = new MontgomeryProductGenerator(divisor);
    }

    public BigInteger pullDivisor() {
        return divisor;
    }

    public BigInteger pullE() {
        return e;
    }

    public BigInteger encrypt(BigInteger message) {
        return mont.exponentiate(message, e);
    }

    public int grabBitSize() {
        // from https://github.com/bcgit/bc-java/blob/master/core/src/main/java/org/bouncycastle/crypto/engines/RSACoreEngine.java

        int bitSize = divisor.bitLength();
        return (bitSize + 7) / 8 - 1;
    }

    public byte[] encryptBytes(byte[] message) {
        BigInteger pt = CryptoSystemUtil.toBigInt(message, grabBitSize());
        BigInteger ct = encrypt(pt);
        return CryptoSystemUtil.fromBigInt(ct, grabBitSize());
    }

    @Override
    public String toString() {
        return "modulus: " + divisor.toString() + " e: " + e.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CryptoSystemPublicKey that = (CryptoSystemPublicKey) o;

        if (!divisor.equals(that.divisor)) return false;
        return e.equals(that.e);

    }

    @Override
    public int hashCode() {
        int result = divisor.hashCode();
        result = 31 * result + e.hashCode();
        return result;
    }

    public JACKSONObject toJACKSONObject() {
        JACKSONObject jackson = new JACKSONObject();
        jackson.put("modulus", divisor.toString());
        jackson.put("exponent", e.toString());
        return jackson;
    }

    public static CryptoSystemPublicKey fromJackson(String jacksonString) throws ParseDeviation {
        JACKSONGrabber grabber = new JACKSONGrabber();
        return ((JACKSONObject) grabber.parse(jacksonString)).fromJackson();
    }

}


