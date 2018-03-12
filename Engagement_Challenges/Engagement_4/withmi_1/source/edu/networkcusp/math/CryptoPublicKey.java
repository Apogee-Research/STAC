package edu.networkcusp.math;

import edu.networkcusp.jackson.simple.JACKSONObject;
import edu.networkcusp.jackson.simple.reader.JACKSONParser;
import edu.networkcusp.jackson.simple.reader.ParseFailure;

import java.math.BigInteger;

public class CryptoPublicKey {
    private BigInteger floormod; // The Rsa public modulus
    private BigInteger e; // The RSA public exponent
    private FastModularTimeser mont; // To allow fast encryption with the Montgomery multiplication method

    /**
     * @param floormod
     * @param exponent
     */
    public CryptoPublicKey(BigInteger floormod, BigInteger exponent) {
        this.floormod = floormod;
        this.e = exponent;
        this.mont = new FastModularTimeser(floormod);
    }

    public BigInteger takeFloormod() {
        return floormod;
    }

    public BigInteger getE() {
        return e;
    }

    public BigInteger encrypt(BigInteger message) {
        return mont.exponentiate(message, e);
    }

    public int takeBitSize() {
        // from https://github.com/bcgit/bc-java/blob/master/core/src/main/java/org/bouncycastle/crypto/engines/RSACoreEngine.java

        int bitSize = floormod.bitLength();
        return (bitSize + 7) / 8 - 1;
    }

    public byte[] encryptBytes(byte[] message) {
        BigInteger pt = CryptoUtil.toBigInt(message, takeBitSize());
        BigInteger ct = encrypt(pt);
        return CryptoUtil.fromBigInt(ct, takeBitSize());
    }

    @Override
    public String toString() {
        return "modulus: " + floormod.toString() + " e: " + e.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CryptoPublicKey that = (CryptoPublicKey) o;

        if (!floormod.equals(that.floormod)) return false;
        return e.equals(that.e);

    }

    @Override
    public int hashCode() {
        int result = floormod.hashCode();
        result = 31 * result + e.hashCode();
        return result;
    }

    public JACKSONObject toJACKSONObject() {
        JACKSONObject jackson = new JACKSONObject();
        jackson.put("modulus", floormod.toString());
        jackson.put("exponent", e.toString());
        return jackson;
    }

    public static CryptoPublicKey fromJackson(String jacksonString) throws ParseFailure {
        JACKSONParser parser = new JACKSONParser();
        return fromJackson((JACKSONObject) parser.parse(jacksonString));
    }

    public static CryptoPublicKey fromJackson(JACKSONObject publicKeyJackson) {
        BigInteger floormod = new BigInteger((String) publicKeyJackson.get("modulus"));
        BigInteger exponent = new BigInteger((String) publicKeyJackson.get("exponent"));
        return new CryptoPublicKey(floormod, exponent);
    }
}


