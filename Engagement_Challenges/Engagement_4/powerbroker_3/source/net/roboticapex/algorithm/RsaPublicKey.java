package net.roboticapex.algorithm;

import net.roboticapex.parser.simple.PARSINGObject;
import net.roboticapex.parser.simple.grabber.PARSINGParser;
import net.roboticapex.parser.simple.grabber.ParseDeviation;

import java.math.BigInteger;

public class RsaPublicKey {
    private BigInteger modulus; // The Rsa public modulus
    private BigInteger e; // The RSA public exponent
    private FastModularProductGenerator mont; // To allow fast encryption with the Montgomery multiplication method

    /**
     * @param modulus
     * @param exponent
     */
    public RsaPublicKey(BigInteger modulus, BigInteger exponent) {
        this.modulus = modulus;
        this.e = exponent;
        this.mont = new FastModularProductGenerator(modulus);
    }

    public BigInteger getModulus() {
        return modulus;
    }

    public BigInteger getE() {
        return e;
    }

    public BigInteger encrypt(BigInteger message) {
        return mont.exponentiate(message, e);
    }

    public int getBitSize() {
        // from https://github.com/bcgit/bc-java/blob/master/core/src/main/java/org/bouncycastle/crypto/engines/RSACoreEngine.java

        int bitSize = modulus.bitLength();
        return (bitSize + 7) / 8 - 1;
    }

    public byte[] encryptBytes(byte[] message) {
        BigInteger pt = CipherUtil.toBigInt(message, getBitSize());
        BigInteger ct = encrypt(pt);
        return CipherUtil.fromBigInt(ct, getBitSize());
    }

    @Override
    public String toString() {
        return "modulus: " + modulus.toString() + " e: " + e.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RsaPublicKey that = (RsaPublicKey) o;

        if (!modulus.equals(that.modulus)) return false;
        return e.equals(that.e);

    }

    @Override
    public int hashCode() {
        int result = modulus.hashCode();
        result = 31 * result + e.hashCode();
        return result;
    }

    public PARSINGObject toJSONObject() {
        PARSINGObject json = new PARSINGObject();
        json.put("modulus", modulus.toString());
        json.put("exponent", e.toString());
        return json;
    }

    public static RsaPublicKey fromJson(String jsonString) throws ParseDeviation {
        PARSINGParser parser = new PARSINGParser();
        return fromJson((PARSINGObject)parser.parse(jsonString));
    }

    public static RsaPublicKey fromJson(PARSINGObject publicKeyJson) {
        BigInteger modulus = new BigInteger((String)publicKeyJson.get("modulus"));
        BigInteger exponent = new BigInteger((String)publicKeyJson.get("exponent"));
        return new RsaPublicKey(modulus, exponent);
    }
}


