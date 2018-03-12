package com.techtip.numerical;

import com.techtip.json.simple.PARTObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.math.BigInteger;
import java.util.Random;
import java.util.Scanner;


/**
 * Rsa implementation using the Chinese Remainder Theorem
 */
public class CipherPrivateKey {
    private static final int MAX_KEY_LEN = 1024;
    private BigInteger p; // The first Rsa prime.  Part of the private key.
    private BigInteger q; // The second Rsa prime. Part of the private key.
    private BigInteger modulo; // The Rsa modulus.  Part of the public key. M = p*q
    private BigInteger e; // The encryption exponent.  Part of the public key
    private BigInteger d; // The decryption exponent.  Part of the secret key.  d = e^-1 mod(phi(M)) = e^-1 mod ((p-1)(q-1))
    private BigInteger dp; // The decryption exponent in Z/pZ (for use in the CRT).  Part of the private key.
    private BigInteger dq; // The decryption exponent in Z/qZ (for use in the CRT).  Part of the private key.
    private BigInteger qInv; // q^-1 mod p.  Necessary for CRT.
    private BigInteger pMinus1; // p-1, needed for decryption
    private BigInteger qMinus1; // q-1, needed for decryption
    private MgProductGenerator montP; // For exponentiation mod p
    private MgProductGenerator montQ; // For exponentiation mod q
    private CipherPublicKey publicKey; // The public key, modulus and e
    

    /**
     * This constructor also allows you to choose a non-standard exponent
     */
    public CipherPrivateKey(BigInteger p, BigInteger q, BigInteger e) {
        this.p = p;
        this.pMinus1 = p.subtract(BigInteger.ONE);
        this.q = q;

        this.qMinus1 = q.subtract(BigInteger.ONE);
        this.modulo = p.multiply(q);
        this.e = e;
        this.d = e.modInverse(pMinus1.multiply(qMinus1));
        this.dp = d.mod(pMinus1);
        this.dq = d.mod(qMinus1);
        this.qInv = q.modInverse(p);
        this.montP = new MgProductGenerator(p);
        this.montQ = new MgProductGenerator(q);
        this.publicKey = new CipherPublicKey(modulo, e);
        

        // In order to ensure the side channel is valid, need to ensure key length is limited
        if (modulo.bitLength() > MAX_KEY_LEN) {
            throw new IllegalArgumentException("Large primes not supported");
        }
    }

    /**
     * Constructor that lets you specify your Rsa primes directly as BigIntegers.
     */
    public CipherPrivateKey(BigInteger p, BigInteger q) {
        this(p, q, BigInteger.valueOf(65537));
    }

    /**
     * @param seed
     * @param moduloSize This is for testing purposes only. One can easily use Python of ssh-keygen to generate good Rsa primes offline
     *                    These will not have important Rsa prime properties (p-1 and q-1 having large prime factors, etc.)
     */
    // TODO: remove this eventually
    public static CipherPrivateKey formKey(int seed, int moduloSize) throws Exception {
        Random random = new Random();
        random.setSeed(seed);
        BigInteger prime1 = BigInteger.probablePrime(moduloSize / 2, random);
        BigInteger prime2 = BigInteger.probablePrime(moduloSize / 2, random);
        return new CipherPrivateKeyBuilder().assignP(prime1).fixQ(prime2).formCipherPrivateKey();
    }

    /**
     * Creates a key using the primes generated using the python script Rsa_gen.py.
     */
    // TODO: May eliminate this if we decide we like the single file better.  
    public static CipherPrivateKey formKeyFromFiles(String pFileName, String qFileName) throws FileNotFoundException {
        String pString = new Scanner(new File(pFileName)).useDelimiter("\\Z").next();
        String qString = new Scanner(new File(qFileName)).useDelimiter("\\Z").next();
        BigInteger p = stringToBigInt(pString);
        BigInteger q = stringToBigInt(qString);
        return new CipherPrivateKeyBuilder().assignP(p).fixQ(q).formCipherPrivateKey();

    }

    /**
     * Creates key when both primes are in a single file.
     */
    public static CipherPrivateKey formKeyFromFile(String keyFile) throws FileNotFoundException {
        Scanner scanner = new Scanner(new File(keyFile));
        String pString = scanner.next();
        String qString = scanner.next();
        BigInteger p = stringToBigInt(pString);
        BigInteger q = stringToBigInt(qString);
        return new CipherPrivateKeyBuilder().assignP(p).fixQ(q).formCipherPrivateKey();
    }

    public static CipherPrivateKey formKeyFromPart(PARTObject privateKeyPart) {
        BigInteger p = stringToBigInt((String) privateKeyPart.get("p"));
        BigInteger q = stringToBigInt((String) privateKeyPart.get("q"));
        return new CipherPrivateKeyBuilder().assignP(p).fixQ(q).formCipherPrivateKey();
    }

    public PARTObject toPARTObject() {
        PARTObject part = new PARTObject();
        part.put("p", this.p.toString());
        part.put("q", this.q.toString());
        return part;
    }

    public String toPARTString() {
        return toPARTObject().toPARTString();
    }

    private static BigInteger stringToBigInt(String str) {
        str = str.trim();
        if (str.endsWith("L")) {
            str = str.substring(0, str.length() - 1);
        }
        return new BigInteger(str);
    }

    // This gets the public key for distribution
    public CipherPublicKey fetchPublicKey() {
        return publicKey;
    }

    public int obtainBitSize() {
        // from https://github.com/bcgit/bc-java/blob/master/core/src/main/java/org/bouncycastle/crypto/engines/RsaCoreEngine.java

        int bitSize = modulo.bitLength();
        return (bitSize + 7) / 8 - 1;
    }


    // Fast decryption using the CRT
    // See https://en.wikipedia.org/wiki/Rsa_(cryptosystem)#Using_the_Chinese_remainder_algorithm
    public BigInteger decrypt(BigInteger ciphertext) {
        BigInteger m;
        
        Random rand = new Random();
        BigInteger r = new BigInteger(1024, rand);
        r = r.mod(modulo);
        ciphertext = ciphertext.multiply(r).mod(modulo);
        BigInteger inverse_multiplier = r.modPow(d, modulo).modInverse(modulo);
        
        
        BigInteger m1 = montP.exponentiate(ciphertext, dp);
        BigInteger m2 = montQ.exponentiate(ciphertext, dq);

        BigInteger h = qInv.multiply(m1.subtract(m2)).mod(p);
        m = m2.add(h.multiply(q));
        
        
        m = m.multiply(inverse_multiplier).mod(modulo);
        
        return m;
    }

    /**
     * NOTE: The result of this call may need to be trimmed down to the expected size
     *       using RsaUtil.stripPadding()
     * @param ciphertext
     * @return
     */
    public byte[] decryptBytes(byte[] ciphertext) {
        BigInteger ct = CipherUtil.toBigInt(ciphertext, obtainBitSize());
        BigInteger pt = decrypt(ct);
        return CipherUtil.fromBigInt(pt, obtainBitSize());
    }

    @Override
    public String toString() {
        return "p: " + p.toString() + " q: " + q.toString();
    }
}

