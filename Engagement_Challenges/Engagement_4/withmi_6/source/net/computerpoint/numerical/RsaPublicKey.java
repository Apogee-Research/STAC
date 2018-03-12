package net.computerpoint.numerical;

import net.computerpoint.parsing.simple.PARSERObject;
import net.computerpoint.parsing.simple.extractor.PARSERExtractor;
import net.computerpoint.parsing.simple.extractor.ParseDeviation;

import java.math.BigInteger;

public class RsaPublicKey {
    private BigInteger modulus; // The Rsa public modulus
    private BigInteger e; // The RSA public exponent
    private MontgomeryProductGenerator mont; // To allow fast encryption with the Montgomery multiplication method

    /**
     * @param modulus
     * @param exponent
     */
    public RsaPublicKey(BigInteger modulus, BigInteger exponent) {
        this.modulus = modulus;
        this.e = exponent;
        this.mont = new MontgomeryProductGenerator(modulus);
    }

    /**
     * Encrypts the data with the specified public key
     * @param data
     * @return the encrypted data
     */
    public byte[] encrypt(byte[] data) {
        return encryptBytes(data);
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
        BigInteger pt = PrivateCommsUtil.toBigInt(message, getBitSize());
        BigInteger ct = encrypt(pt);
        return PrivateCommsUtil.fromBigInt(ct, getBitSize());
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

    public PARSERObject toJSONObject() {
        PARSERObject json = new PARSERObject();
        json.put("modulus", modulus.toString());
        json.put("exponent", e.toString());
        return json;
    }

    public static RsaPublicKey fromJson(String jsonString) throws ParseDeviation {
        PARSERExtractor parser = new PARSERExtractor();
        return fromJson((PARSERObject)parser.parse(jsonString));
    }

    public static RsaPublicKey fromJson(PARSERObject publicKeyJson) {
        BigInteger modulus = new BigInteger((String)publicKeyJson.get("modulus"));
        BigInteger exponent = new BigInteger((String)publicKeyJson.get("exponent"));
        return new RsaPublicKey(modulus, exponent);
    }
}


