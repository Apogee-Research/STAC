package edu.computerapex.math;

import com.google.protobuf.ByteString;
import edu.computerapex.dialogs.Comms;
import edu.computerapex.json.simple.JSONObject;
import edu.computerapex.json.simple.parser.JSONRetriever;
import edu.computerapex.json.simple.parser.ParseDeviation;

import java.math.BigInteger;

public class EncryptionPublicKey {
    private BigInteger floormod; // The Rsa public modulus
    private BigInteger e; // The RSA public exponent
    private MgProductGenerator mont; // To allow fast encryption with the Montgomery multiplication method

    /**
     * @param floormod
     * @param exponent
     */
    public EncryptionPublicKey(BigInteger floormod, BigInteger exponent) {
        this.floormod = floormod;
        this.e = exponent;
        this.mont = new MgProductGenerator(floormod);
    }

    public Comms.PublicKey serializePublicKey() {
        Comms.PublicKey commsPublicKey = Comms.PublicKey.newBuilder()
                .setE(ByteString.copyFrom(grabE().toByteArray()))
                .setModulus(ByteString.copyFrom(obtainFloormod().toByteArray()))
                .build();
        return commsPublicKey;
    }

    /**
     * Encrypts the data with the specified public key
     * @param data
     * @return the encrypted data
     */
    public byte[] encrypt(byte[] data) {
        return encryptBytes(data);
    }

    public BigInteger obtainFloormod() {
        return floormod;
    }

    public BigInteger grabE() {
        return e;
    }

    public BigInteger encrypt(BigInteger message) {
        return mont.exponentiate(message, e);
    }

    public int getBitSize() {
        // from https://github.com/bcgit/bc-java/blob/master/core/src/main/java/org/bouncycastle/crypto/engines/RSACoreEngine.java

        int bitSize = floormod.bitLength();
        return (bitSize + 7) / 8 - 1;
    }

    public byte[] encryptBytes(byte[] message) {
        BigInteger pt = EncryptionUtil.toBigInt(message, getBitSize());
        BigInteger ct = encrypt(pt);
        return EncryptionUtil.fromBigInt(ct, getBitSize());
    }

    @Override
    public String toString() {
        return "modulus: " + floormod.toString() + " e: " + e.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EncryptionPublicKey that = (EncryptionPublicKey) o;

        if (!floormod.equals(that.floormod)) return false;
        return e.equals(that.e);

    }

    @Override
    public int hashCode() {
        int result = floormod.hashCode();
        result = 31 * result + e.hashCode();
        return result;
    }

    public JSONObject toJSONObject() {
        JSONObject json = new JSONObject();
        json.put("modulus", floormod.toString());
        json.put("exponent", e.toString());
        return json;
    }

    public static EncryptionPublicKey fromJson(String jsonString) throws ParseDeviation {
        JSONRetriever retriever = new JSONRetriever();
        return fromJson((JSONObject) retriever.parse(jsonString));
    }

    public static EncryptionPublicKey fromJson(JSONObject publicKeyJson) {
        BigInteger floormod = new BigInteger((String)publicKeyJson.get("modulus"));
        BigInteger exponent = new BigInteger((String)publicKeyJson.get("exponent"));
        return new EncryptionPublicKey(floormod, exponent);
    }
}


