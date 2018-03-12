package net.robotictip.numerical;

import com.google.protobuf.ByteString;
import net.robotictip.parser.simple.JACKObject;
import net.robotictip.parser.simple.parser.JACKReader;
import net.robotictip.parser.simple.parser.ParseTrouble;
import net.robotictip.protocols.Comms;

import java.math.BigInteger;

public class RsaPublicKey {
    private BigInteger modulus; // The Rsa public modulus
    private BigInteger e; // The RSA public exponent
    private MontgomeryTimeser mont; // To allow fast encryption with the Montgomery multiplication method

    /**
     * @param modulus
     * @param exponent
     */
    public RsaPublicKey(BigInteger modulus, BigInteger exponent) {
        this.modulus = modulus;
        this.e = exponent;
        this.mont = new MontgomeryTimeser(modulus);
    }

    public Comms.PublicKey serializePublicKey() {
        Comms.PublicKey commsPublicKey = Comms.PublicKey.newBuilder()
                .setE(ByteString.copyFrom(getE().toByteArray()))
                .setModulus(ByteString.copyFrom(getModulus().toByteArray()))
                .build();
        return commsPublicKey;
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

    public JACKObject toJSONObject() {
        JACKObject json = new JACKObject();
        json.put("modulus", modulus.toString());
        json.put("exponent", e.toString());
        return json;
    }

    public static RsaPublicKey fromJson(String jsonString) throws ParseTrouble {
        JACKReader parser = new JACKReader();
        return fromJson((JACKObject)parser.parse(jsonString));
    }

    public static RsaPublicKey fromJson(JACKObject publicKeyJson) {
        BigInteger modulus = new BigInteger((String)publicKeyJson.get("modulus"));
        BigInteger exponent = new BigInteger((String)publicKeyJson.get("exponent"));
        return new RsaPublicKey(modulus, exponent);
    }
}


