package edu.networkcusp.math;

import com.google.protobuf.ByteString;
import edu.networkcusp.jackson.simple.JACKObject;
import edu.networkcusp.jackson.simple.parser.JACKParser;
import edu.networkcusp.jackson.simple.parser.ParseRaiser;
import edu.networkcusp.senderReceivers.Comms;

import java.math.BigInteger;

public class PrivateCommsPublicKey {
    private BigInteger modulo; // The Rsa public modulus
    private BigInteger e; // The RSA public exponent
    private FastModularProductGenerator mont; // To allow fast encryption with the Montgomery multiplication method

    /**
     * @param modulo
     * @param exponent
     */
    public PrivateCommsPublicKey(BigInteger modulo, BigInteger exponent) {
        this.modulo = modulo;
        this.e = exponent;
        this.mont = new FastModularProductGenerator(modulo);
    }

    public Comms.PublicKey serializePublicKey() {
        Comms.PublicKey commsPublicKey = Comms.PublicKey.newBuilder()
                .setE(ByteString.copyFrom(grabE().toByteArray()))
                .setModulus(ByteString.copyFrom(pullModulo().toByteArray()))
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

    public BigInteger pullModulo() {
        return modulo;
    }

    public BigInteger grabE() {
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
        BigInteger pt = PrivateCommsUtil.toBigInt(message, grabBitSize());
        BigInteger ct = encrypt(pt);
        return PrivateCommsUtil.fromBigInt(ct, grabBitSize());
    }

    @Override
    public String toString() {
        return "modulus: " + modulo.toString() + " e: " + e.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PrivateCommsPublicKey that = (PrivateCommsPublicKey) o;

        if (!modulo.equals(that.modulo)) return false;
        return e.equals(that.e);

    }

    @Override
    public int hashCode() {
        int result = modulo.hashCode();
        result = 31 * result + e.hashCode();
        return result;
    }

    public JACKObject toJACKObject() {
        JACKObject jack = new JACKObject();
        jack.put("modulus", modulo.toString());
        jack.put("exponent", e.toString());
        return jack;
    }

    public static PrivateCommsPublicKey fromJack(String jackString) throws ParseRaiser {
        JACKParser parser = new JACKParser();
        return fromJack((JACKObject) parser.parse(jackString));
    }

    public static PrivateCommsPublicKey fromJack(JACKObject publicKeyJack) {
        BigInteger modulo = new BigInteger((String) publicKeyJack.get("modulus"));
        BigInteger exponent = new BigInteger((String) publicKeyJack.get("exponent"));
        return new PrivateCommsPublicKey(modulo, exponent);
    }
}


