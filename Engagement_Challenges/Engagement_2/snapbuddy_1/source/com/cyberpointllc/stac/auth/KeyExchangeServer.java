package com.cyberpointllc.stac.auth;

import java.math.BigInteger;
import java.security.SecureRandom;
import com.cyberpointllc.stac.math.ModPow;

/*
 * This is the class that performs the most basic DH operations.  
 * Namely it has a secret exponent x which acts as a key.
 * Our public key is A = g^x mod m, where g and m are agreed upon a priori.
 * To agree on a master secert, a client sends his public key B = g^y mod m for some secret y.
 * Server computes a shared master secret B^x mod m = (g^y)^x mod m = g^xy mod m.
 * Client computes shared secret by A^y mod m = (g^x)^y mod m = g^xy mod m.
 * In a real host program, these values should be hashed to give a symmertric key, and possibly a HMAC key.
 */
public class KeyExchangeServer {

    private BigInteger secretKey;

    private BigInteger modulus;

    private BigInteger generator;

    private BigInteger publicKey;

    // Should be used rarely
    public KeyExchangeServer(String secretKey, String modulus, String generator) {
        if (secretKey.startsWith("0x")) {
            this.secretKey = new  BigInteger(secretKey.substring(2), 16);
        } else {
            this.secretKey = new  BigInteger(secretKey);
            if (modulus.startsWith("0x")) {
                this.modulus = new  BigInteger(modulus.substring(2), 16);
            } else {
                this.modulus = new  BigInteger(modulus);
            }
            if (generator.startsWith("0x")) {
                this.generator = new  BigInteger(generator.substring(2), 16);
            } else {
                this.generator = new  BigInteger(generator);
            }
            this.publicKey = this.generator.modPow(this.secretKey, this.modulus);
        }
    }

    // Constructor that uses the modp1536 group and a random secret exponent
    public KeyExchangeServer() {
        String modp1536 = "FFFFFFFFFFFFFFFFC90FDAA22168C234C4C6628B80DC1CD129024E088A67CC74020BBEA63B139B22514A08798E3404DDEF9519B3CD3A431B302B0A6DF25F14374FE1356D6D51C245E485B576625E7EC6F44C42E9A637ED6B0BFF5CB6F406B7EDEE386BFB5A899FA5AE9F24117C4B1FE649286651ECE45B3DC2007CB8A163BF0598DA48361C55D39A69163FA8FD24CF5F83655D23DCA3AD961C62F356208552BB9ED529077096966D670C354E4ABC9804F1746C08CA237327FFFFFFFFFFFFFFFF";
        modulus = new  BigInteger(modp1536, 16);
        generator = BigInteger.valueOf(2);
        SecureRandom r = new  SecureRandom();
        byte randbytes[] = new byte[8];
        r.nextBytes(randbytes);
        BigInteger secretKey = new  BigInteger(randbytes);
        publicKey = this.generator.modPow(secretKey, modulus);
    }

    public KeyExchangeServer(String secretKey) {
        String modp1536 = "FFFFFFFFFFFFFFFFC90FDAA22168C234C4C6628B80DC1CD129024E088A67CC74020BBEA63B139B22514A08798E3404DDEF9519B3CD3A431B302B0A6DF25F14374FE1356D6D51C245E485B576625E7EC6F44C42E9A637ED6B0BFF5CB6F406B7EDEE386BFB5A899FA5AE9F24117C4B1FE649286651ECE45B3DC2007CB8A163BF0598DA48361C55D39A69163FA8FD24CF5F83655D23DCA3AD961C62F356208552BB9ED529077096966D670C354E4ABC9804F1746C08CA237327FFFFFFFFFFFFFFFF";
        modulus = new  BigInteger(modp1536, 16);
        generator = BigInteger.valueOf(2);
        if (secretKey.startsWith("0x")) {
            this.secretKey = new  BigInteger(secretKey.substring(2), 16);
        } else {
            this.secretKey = new  BigInteger(secretKey);
        }
        publicKey = this.generator.modPow(this.secretKey, modulus);
    }

    public BigInteger getModulus() {
        return modulus;
    }

    public BigInteger getGenerator() {
        return generator;
    }

    public BigInteger getPublicKey() {
        return publicKey;
    }

    // Fundamental operation in finite field Diffie-Hellman key exchanges.  
    // Raises the submitted public key to our secret exponent.
    // Just using the default Java modPow which is not designed for cryptography.
    public BigInteger generateMasterSecret(BigInteger clientPublic) {
        return ModPow.modPow(clientPublic, secretKey, modulus);
    }
}
