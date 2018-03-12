package com.roboticcusp.authorize;

import com.roboticcusp.numerical.CircularPow;

import java.math.BigInteger;
import java.security.SecureRandom;

/*
 * This is the class that performs the most basic DH operations.  
 * Namely it has a secret exponent x which acts as a key.
 * Our public key is A = g^x mod m, where g and m are agreed upon a priori.
 * To agree on a master secret, a client sends his public key B = g^y mod m for some secret y.
 * Server computes a shared master secret B^x mod m = (g^y)^x mod m = g^xy mod m.
 * Client computes shared secret by A^y mod m = (g^x)^y mod m = g^xy mod m.
 * In a real host program, these values should be hashed to give a symmertric key, and possibly a HMAC key.
 */
public class KeyExchangeServer {
    private BigInteger secretKey;
    private BigInteger modulo;
    private BigInteger producer;
    private BigInteger publicKey;
    private static final String DEFAULT_MODULUS = "0xFFFFFFFFFFFFFFFFC90FDAA22168C234C4C6628B80DC1CD129024E088A67CC74020BBEA63B139B22514A08798E3404DDEF9519B3CD3A431B302B0A6DF25F14374FE1356D6D51C245E485B576625E7EC6F44C42E9A637ED6B0BFF5CB6F406B7EDEE386BFB5A899FA5AE9F24117C4B1FE649286651ECE45B3DC2007CB8A163BF0598DA48361C55D39A69163FA8FD24CF5F83655D23DCA3AD961C62F356208552BB9ED529077096966D670C354E4ABC9804F1746C08CA237327FFFFFFFFFFFFFFFF";
    private static final BigInteger DEFAULT_GENERATOR = BigInteger.valueOf(2);

    public KeyExchangeServer(BigInteger secretKey, BigInteger modulo, BigInteger producer) {
        this.secretKey = secretKey;
        this.modulo = modulo;
        this.producer = producer;
        this.publicKey = this.producer.modPow(this.secretKey, this.modulo);
    }

    // Constructor for user specified key exponent, modulus and generator
    // Should be used rarely
    public KeyExchangeServer(String secretKey, String modulo, String producer) {
        this(stringToBigInt(secretKey), stringToBigInt(modulo), stringToBigInt(producer));
    }

    // Constructor that uses the modp1536 group and a passed in secret exponent.
    // This will be the one used in most of our attacks
    public KeyExchangeServer(String secretKey) {
        this(stringToBigInt(secretKey), stringToBigInt(DEFAULT_MODULUS), DEFAULT_GENERATOR);
    }

    // Constructor that uses the modp1536 group and a random secret exponent
    public KeyExchangeServer() {
        this(composeRandomSecret(), stringToBigInt(DEFAULT_MODULUS), DEFAULT_GENERATOR);
    }
    
    public BigInteger pullModulo() {
        return modulo;
    }

    public BigInteger grabProducer() {
        return producer;
    }

    public BigInteger fetchPublicKey() {
        return publicKey;
    }

    // Fundamental operation in finite field Diffie-Hellman key exchanges.  
    // Raises the submitted public key to our secret exponent.
    // Just using the default Java modPow which is not designed for cryptography.
    public BigInteger generateMasterSecret(BigInteger clientPublic) {
        return CircularPow.circularPow(clientPublic, secretKey, modulo);
    }


    private static BigInteger stringToBigInt(String number) {
        if (number.startsWith("0x")) {
            return new BigInteger(number.substring(2), 16);
        }
        return new BigInteger(number);
    }

    private static BigInteger composeRandomSecret() {
        SecureRandom r = new SecureRandom();
        byte[] randbytes = new byte[8];
        r.nextBytes(randbytes);
        // we want the BigInteger to be positive
        return new BigInteger(1, randbytes);
    }

}
