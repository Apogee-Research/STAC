package org.techpoint.mathematic;

import com.google.protobuf.ByteString;
import org.techpoint.communications.Comms;
import org.techpoint.parsing.simple.PARTObject;
import org.techpoint.parsing.simple.reader.PARTReader;
import org.techpoint.parsing.simple.reader.ParseRaiser;

import java.math.BigInteger;

public class CryptoSystemPublicKey {
    private BigInteger modulo; // The Rsa public modulus
    private BigInteger e; // The RSA public exponent
    private MontgomeryProductGenerator mont; // To allow fast encryption with the Montgomery multiplication method

    /**
     * @param modulo
     * @param exponent
     */
    public CryptoSystemPublicKey(BigInteger modulo, BigInteger exponent) {
        this.modulo = modulo;
        this.e = exponent;
        this.mont = new MontgomeryProductGenerator(modulo);
    }

    public Comms.PublicKey serializePublicKey() {
        Comms.PublicKey commsPublicKey = Comms.PublicKey.newBuilder()
                .setE(ByteString.copyFrom(takeE().toByteArray()))
                .setModulus(ByteString.copyFrom(takeModulo().toByteArray()))
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

    public BigInteger takeModulo() {
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
        BigInteger pt = CryptoSystemUtil.toBigInt(message, grabBitSize());
        BigInteger ct = encrypt(pt);
        return CryptoSystemUtil.fromBigInt(ct, grabBitSize());
    }

    @Override
    public String toString() {
        return "modulus: " + modulo.toString() + " e: " + e.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CryptoSystemPublicKey that = (CryptoSystemPublicKey) o;

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

    public static CryptoSystemPublicKey fromPart(String partString) throws ParseRaiser {
        PARTReader reader = new PARTReader();
        return ((PARTObject) reader.parse(partString)).fromPart();
    }

}


