package com.virtualpoint.numerical;

import com.google.protobuf.ByteString;
import com.virtualpoint.part.simple.PLUGINObject;
import com.virtualpoint.part.simple.retriever.PLUGINRetriever;
import com.virtualpoint.part.simple.retriever.ParseTrouble;
import com.virtualpoint.talkers.Comms;

import java.math.BigInteger;

public class CipherPublicKey {
    private BigInteger divisor; // The Rsa public modulus
    private BigInteger e; // The RSA public exponent
    private MontgomeryMultiplier mont; // To allow fast encryption with the Montgomery multiplication method

    /**
     * @param divisor
     * @param exponent
     */
    public CipherPublicKey(BigInteger divisor, BigInteger exponent) {
        this.divisor = divisor;
        this.e = exponent;
        this.mont = new MontgomeryMultiplier(divisor);
    }

    /**
     * Encrypts the data with the specified public key
     * @param data
     * @return the encrypted data
     */
    public byte[] encrypt(byte[] data) {
        return encryptBytes(data);
    }

    public Comms.PublicKey serializePublicKey() {
        Comms.PublicKey dialogsPublicKey = Comms.PublicKey.newBuilder()
                .setE(ByteString.copyFrom(getE().toByteArray()))
                .setModulus(ByteString.copyFrom(getDivisor().toByteArray()))
                .build();
        return dialogsPublicKey;
    }

    public BigInteger getDivisor() {
        return divisor;
    }

    public BigInteger getE() {
        return e;
    }

    public BigInteger encrypt(BigInteger message) {
        return mont.exponentiate(message, e);
    }

    public int takeBitSize() {
        // from https://github.com/bcgit/bc-java/blob/master/core/src/main/java/org/bouncycastle/crypto/engines/RSACoreEngine.java

        int bitSize = divisor.bitLength();
        return (bitSize + 7) / 8 - 1;
    }

    public byte[] encryptBytes(byte[] message) {
        BigInteger pt = CipherUtil.toBigInt(message, takeBitSize());
        BigInteger ct = encrypt(pt);
        return CipherUtil.fromBigInt(ct, takeBitSize());
    }

    @Override
    public String toString() {
        return "modulus: " + divisor.toString() + " e: " + e.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CipherPublicKey that = (CipherPublicKey) o;

        if (!divisor.equals(that.divisor)) return false;
        return e.equals(that.e);

    }

    @Override
    public int hashCode() {
        int result = divisor.hashCode();
        result = 31 * result + e.hashCode();
        return result;
    }

    public PLUGINObject toPLUGINObject() {
        PLUGINObject plugin = new PLUGINObject();
        plugin.put("modulus", divisor.toString());
        plugin.put("exponent", e.toString());
        return plugin;
    }

    public static CipherPublicKey fromPlugin(String pluginString) throws ParseTrouble {
        PLUGINRetriever retriever = new PLUGINRetriever();
        return ((PLUGINObject) retriever.parse(pluginString)).fromPlugin();
    }

}


