package stac.communications.parsers;

import stac.communications.Packet;
import stac.communications.PacketParser;
import stac.crypto.Key;
import stac.crypto.RSA;
import stac.crypto.SHA256;
import stac.parser.OpenSSLRSAPEM;

import java.security.SecureRandom;

/**
 *
 */
public class HandshakeChallengePacket extends Packet {
    private static final SecureRandom secureRandom = new SecureRandom();
    private byte[] challenge = new byte[61];
    private static RSA rsa = new RSA();

    public HandshakeChallengePacket(Key userKey, byte[] challenge) {
        if (userKey.getPem().getType() != OpenSSLRSAPEM.DER_TYPE.PRIVATE_KEY) {
            throw new RuntimeException("Non-PrivateKey attempt to sign challenge");
        }
        this.challenge = rsa.encrypt(
                OpenSSLRSAPEM.INTEGER.valueOfUnsigned(SHA256.digest(challenge)),
                userKey.getPem().getPrivateExponent(),
                userKey.getPem().getModulus()
        ).getBytes();
    }

    public HandshakeChallengePacket() {
        secureRandom.nextBytes(challenge);
        challenge[0] &= 0b01111111;
    }

    @Override
    public PacketParser getParser() {
        return new HandshakeChallengePacketParser(this);
    }

    public byte[] getChallenge() {
        return challenge;
    }

    public void setChallenge(byte[] challenge) {
        this.challenge = challenge;
    }

    public boolean verifyChallenge(byte[] serverChallenge, Key verifyKey) {
        OpenSSLRSAPEM.INTEGER serverChallengeDigest = OpenSSLRSAPEM.INTEGER.valueOfUnsigned(SHA256.digest(serverChallenge));
        if (verifyKey.getPem().getPublicExponent().getInternalBig().signum() == 0) throw new RuntimeException("Failed to verify key due to zero public exponent");
        OpenSSLRSAPEM.INTEGER decrypt = rsa.decrypt(OpenSSLRSAPEM.INTEGER.valueOfUnsigned(challenge), verifyKey.getPem().getPublicExponent(), verifyKey.getPem().getModulus());

        return serverChallengeDigest.compareTo(decrypt) == 0;
    }
}
