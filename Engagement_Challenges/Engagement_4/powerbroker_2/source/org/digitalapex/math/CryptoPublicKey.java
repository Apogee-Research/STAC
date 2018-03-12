package org.digitalapex.math;

import com.google.protobuf.ByteString;
import org.digitalapex.json.simple.PARSERObject;
import org.digitalapex.json.simple.grabber.PARSERGrabber;
import org.digitalapex.json.simple.grabber.ParseRaiser;
import org.digitalapex.talkers.Comms;

import java.math.BigInteger;

public class CryptoPublicKey {
    private BigInteger factor; // The Rsa public modulus
    private BigInteger e; // The RSA public exponent
    private MgMultiplier mont; // To allow fast encryption with the Montgomery multiplication method

    /**
     * @param factor
     * @param exponent
     */
    public CryptoPublicKey(BigInteger factor, BigInteger exponent) {
        this.factor = factor;
        this.e = exponent;
        this.mont = new MgMultiplier(factor);
    }

    public Comms.PublicKey serializePublicKey() {
        Comms.PublicKey commsPublicKey = Comms.PublicKey.newBuilder()
                .setE(ByteString.copyFrom(takeE().toByteArray()))
                .setModulus(ByteString.copyFrom(grabFactor().toByteArray()))
                .build();
        return commsPublicKey;
    }

    public BigInteger grabFactor() {
        return factor;
    }

    public BigInteger takeE() {
        return e;
    }

    public BigInteger encrypt(BigInteger message) {
        return mont.exponentiate(message, e);
    }

    public int obtainBitSize() {
        // from https://github.com/bcgit/bc-java/blob/master/core/src/main/java/org/bouncycastle/crypto/engines/RSACoreEngine.java

        int bitSize = factor.bitLength();
        return (bitSize + 7) / 8 - 1;
    }

    public byte[] encryptBytes(byte[] message) {
        BigInteger pt = CryptoUtil.toBigInt(message, obtainBitSize());
        BigInteger ct = encrypt(pt);
        return CryptoUtil.fromBigInt(ct, obtainBitSize());
    }

    @Override
    public String toString() {
        return "modulus: " + factor.toString() + " e: " + e.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CryptoPublicKey that = (CryptoPublicKey) o;

        if (!factor.equals(that.factor)) return false;
        return e.equals(that.e);

    }

    @Override
    public int hashCode() {
        int result = factor.hashCode();
        result = 31 * result + e.hashCode();
        return result;
    }

    public PARSERObject toPARSERObject() {
        PARSERObject parser = new PARSERObject();
        parser.put("modulus", factor.toString());
        parser.put("exponent", e.toString());
        return parser;
    }

    public static CryptoPublicKey fromParser(String parserString) throws ParseRaiser {
        PARSERGrabber grabber = new PARSERGrabber();
        return ((PARSERObject) grabber.parse(parserString)).fromParser();
    }

}


