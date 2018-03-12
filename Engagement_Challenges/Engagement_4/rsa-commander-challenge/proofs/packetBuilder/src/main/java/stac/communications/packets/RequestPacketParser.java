package stac.communications.packets;

import stac.communications.PACKETS;
import stac.communications.Packet;
import stac.communications.PacketBuffer;
import stac.communications.PacketParser;
import stac.communications.PacketParserException;
import stac.communications.parsers.RequestPacket;
import stac.crypto.DES;
import stac.crypto.PrivateKey;
import stac.crypto.PublicKey;
import stac.crypto.RSA;
import stac.crypto.SHA256;
import stac.crypto.SymmetricCipher;
import stac.parser.OpenSSLRSAPEM;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 8     8     8     8
 * |-----|-----|-----|-----| 32
 * |-----|-----|-----|-----|-----|-----|-----|-----|-----|-----|-----|-----|-----|-----|-----|-----| 128
 * |===============================================================================================|
 * |pType|- Session Content Len -|--------------- sender fingerprint - (256) ----------------------|
 * |-----------------------------------------------------------------------------------------------|
 * |-----------------------------|-------------- receiver fingerprint - (256) ---------------------|
 * |-----------------------------------------------------------------------------------------------|
 * |-----------------------------|---- Nonce Length -----|-- RSA encrypted nonce bytes --- ~~ -----|
 * |--- Counter Length ----|------------------------------- RSA encrypted counter bytes -- ~~ -----|
 * |---- Header Digest (SHA-256) ------------------------------------------------------------------|
 * |-----------------------------------------------------------------------------------------------|
 * |=================================== AES Encryption Boundary ===================================|
 * |- Sender Name Length --|----------------- Sender Name ---------------------- ~~ ---------------|
 * |- Recver Name Length --|----------------- Receiver Name -------------------- ~~ ---------------|
 * |--- Message Length ----|----------------- Message -------------------------- ~~ ---------------|
 * |----------------------------------------- Message Hash (256) ----------------------------------|
 * |-----------------------------------------------------------------------------------------------|
 * |===============================================================================================|
 * <p>
 * If pType is request messages, then the message length should be 0
 * <p>
 * WARNING THIS PARSER CANNOT SET SENDER AND RECEIVER KEYS DURING RECEPTION! This functionality is
 * provided via the session and user objects.
 */
public class RequestPacketParser extends PacketParser {
    private static final SymmetricCipher des = new DES();
    private final RequestPacket owner;

    public RequestPacketParser(RequestPacket requestPacket) {
        owner = requestPacket;
    }

    @Override
    public Packet parse(PacketBuffer packetBuffer) throws PacketParserException {
        RSA rsa = new RSA();
        byte[] buffer = packetBuffer.getBuffer();
        int bpos = 0;

        byte b = buffer[bpos++];

        if (b == PACKETS.REQUEST_MESSAGE.ordinal()) {
            owner.setType(RequestPacket.RequestType.Message);
        } else if (b == PACKETS.REQUEST_MESSAGE_RELAY.ordinal()) {
            owner.setType(RequestPacket.RequestType.MessageRelay);
        } else if (b == PACKETS.RETRIEVE_MESSAGES.ordinal()) {
            owner.setType(RequestPacket.RequestType.RetrieveMessages);
        } else if (b == PACKETS.REQUEST_TERMINATE.ordinal()) {
            owner.setType(RequestPacket.RequestType.Terminate);
        } else {
            throw new PacketParserException("Packet type is not valid");
        }

        SHA256 headerDigest = new SHA256();

        byte[] senderFingerprint = new byte[32];
        byte[] receiverFingerprint = new byte[32];

        int sessionContentLength = OpenSSLRSAPEM.INTEGER.valueOf(buffer, bpos, 4).getInternal();
        bpos += 4;

        System.arraycopy(buffer, bpos, senderFingerprint, 0, senderFingerprint.length);
        headerDigest.update(buffer, bpos, senderFingerprint.length);
        bpos += senderFingerprint.length;
        System.arraycopy(buffer, bpos, receiverFingerprint, 0, receiverFingerprint.length);
        headerDigest.update(buffer, bpos, receiverFingerprint.length);
        bpos += receiverFingerprint.length;

        int nonceBytesLength = OpenSSLRSAPEM.INTEGER.valueOf(buffer, bpos, 4).getInternal();
        headerDigest.update(buffer, bpos, 4);
        bpos += 4;
        OpenSSLRSAPEM.INTEGER nonce = OpenSSLRSAPEM.INTEGER.valueOf(buffer, bpos, nonceBytesLength);
        bpos += nonceBytesLength;
        PrivateKey privateKey = owner.getCommunications().getListenerKey();
        nonce = rsa.decrypt(nonce, privateKey.getPem().getPrivateExponent(), privateKey.getPem().getModulus());
        if (nonce.compareTo(0) < 0) {
            throw new PacketParserException("Failed to perform RSA decryption");
        }
        headerDigest.update(nonce.getBytes());

        int counterBytesLength = OpenSSLRSAPEM.INTEGER.valueOf(buffer, bpos, 4).getInternal();
        headerDigest.update(buffer, bpos, 4);
        bpos += 4;
        OpenSSLRSAPEM.INTEGER counter = OpenSSLRSAPEM.INTEGER.valueOf(buffer, bpos, counterBytesLength);
        bpos += counterBytesLength;
        counter = rsa.decrypt(counter, privateKey.getPem().getPrivateExponent(), privateKey.getPem().getModulus());
        if (!(counter.compareTo(0) >= 0 && counter.compareTo(owner.getReceiverKey().getPem().getPublicExponent()) <= 0)) {
            throw new PacketParserException("Failed to perform RSA decryption");
        }
        headerDigest.update(counter.getBytes());

        byte[] headerDigestBytes = headerDigest.digest();
        for (int i = 0; i < headerDigestBytes.length; i++) {
            if (headerDigestBytes[i] != buffer[bpos++]) {
                throw new PacketParserException("Packet header check failed; possible packet corruption.");
            }
        }

        ByteArrayInputStream encrypted = new ByteArrayInputStream(buffer, bpos, sessionContentLength);

        ByteArrayOutputStream decrypted = new ByteArrayOutputStream();
        try {
            des.encrypt_ctr(counter, nonce.getBytes(8), encrypted, decrypted);
        } catch (IOException e) {
            throw new PacketParserException("Failed to perform RSA decryption");
        }
        byte[] details = decrypted.toByteArray();
        bpos = 0;

        OpenSSLRSAPEM.INTEGER senderNameLength = OpenSSLRSAPEM.INTEGER.valueOf(details, bpos, 4);
        bpos += 4;
        byte[] senderNameBytes = new byte[senderNameLength.getInternal()];
        System.arraycopy(details, bpos, senderNameBytes, 0, senderNameLength.getInternal());
        bpos += senderNameLength.getInternal();

        OpenSSLRSAPEM.INTEGER receiverNameLength = OpenSSLRSAPEM.INTEGER.valueOf(details, bpos, 4);
        bpos += 4;
        byte[] receiverNameBytes = new byte[receiverNameLength.getInternal()];
        System.arraycopy(details, bpos, receiverNameBytes, 0, receiverNameLength.getInternal());
        bpos += receiverNameLength.getInternal();

        OpenSSLRSAPEM.INTEGER messageLength = OpenSSLRSAPEM.INTEGER.valueOf(details, bpos, 4);
        bpos += 4;
        byte[] messageBytes = new byte[messageLength.getInternal()];
        System.arraycopy(details, bpos, messageBytes, 0, messageLength.getInternal());
        bpos += messageLength.getInternal();

        byte[] ourDigest = SHA256.digest(
                senderNameLength.getBytes(4),
                senderNameBytes,
                receiverNameLength.getBytes(4),
                receiverNameBytes,
                messageLength.getBytes(4),
                messageBytes
        );

        byte[] theirDigest = new byte[32];
        System.arraycopy(details, bpos, theirDigest, 0, theirDigest.length);

        for (int i = 0; i < theirDigest.length; i++) {
            if (theirDigest[i] != ourDigest[i]) {
                throw new PacketParserException("Failed decryption of encrypted packet. Checksum failure");
            }
        }

        owner.setMessage(new String(messageBytes));
        owner.setSenderName(new String(senderNameBytes));
        owner.setReceiverName(new String(receiverNameBytes));
        owner.setSenderKey(new PrivateKey(senderFingerprint));
        owner.setReceiverKey(new PublicKey(receiverFingerprint));
        owner.setNonce(nonce); // THIS LINE IS THE BROKEN GUARD

        return owner;
    }

    @Override
    public byte[] serialize() throws PacketParserException {
        return serializeWithCounter(OpenSSLRSAPEM.INTEGER.randomShort().getInternal());
    }

    public byte[] serializeWithCounter(int c) {
        RSA rsa = new RSA();
        byte[] outputPacketBytes;
        try (ByteArrayOutputStream requestPacketOutputStream = new ByteArrayOutputStream()) {

            requestPacketOutputStream.write(owner.getType().toPacketType().ordinal());

            OpenSSLRSAPEM.INTEGER nonce = owner.getNonce();
            OpenSSLRSAPEM.INTEGER counter = OpenSSLRSAPEM.INTEGER.valueOf(c);

            ByteArrayOutputStream sessionInformationOutputStream = encryptSymmetricCipherParameters(rsa, nonce, counter);

            int size = appendRijndaelEncryptedSessionContent(sessionInformationOutputStream, nonce, counter);

            byte[] sessionContentBytes = sessionInformationOutputStream.toByteArray();

            requestPacketOutputStream.write(OpenSSLRSAPEM.INTEGER.valueOf(size).getBytes(4));
            requestPacketOutputStream.write(sessionContentBytes);

            outputPacketBytes = requestPacketOutputStream.toByteArray();
        } catch (IOException e) {
            System.err.println("Failed to serialize a message packet");
            return null;
        }
        return outputPacketBytes;
    }

    /**
     * Symmetric Cipher Parameters:
     * |-------------------------------------- sender fingerprint -------------------------------------|
     * |-----------------------------------------------------------------------------------------------| 256
     * |------------------------------------- receiver fingerprint ------------------------------------|
     * |-----------------------------------------------------------------------------------------------| 256
     * |--- Nonce Length ------|----------- RSA encrypted nonce bytes ---------------------------------| ...
     * |--- Counter Length ----|----------- RSA encrypted counter bytes -------------------------------| ...
     */
    private ByteArrayOutputStream encryptSymmetricCipherParameters(RSA rsa, OpenSSLRSAPEM.INTEGER nonce, OpenSSLRSAPEM.INTEGER counter) throws IOException {
        SHA256 headerDigest = new SHA256();

        byte[] senderFingerprint = owner.getSenderKey().getFingerPrint();
        byte[] receiverFingerprint = owner.getReceiverKey().getFingerPrint();

        headerDigest.update(senderFingerprint);
        headerDigest.update(receiverFingerprint);

        OpenSSLRSAPEM.INTEGER publicExponent = owner.getReceiverKey().getPem().getPublicExponent();
        OpenSSLRSAPEM.INTEGER modulus = owner.getReceiverKey().getPem().getModulus();

        OpenSSLRSAPEM.INTEGER encryptNonce = rsa.encrypt(nonce, publicExponent, modulus);
        OpenSSLRSAPEM.INTEGER encryptCounter = rsa.encrypt(counter, publicExponent, modulus);

        byte[] encryptNonceBytes = encryptNonce.getBytes();
        byte[] encryptNonceBytesLength = OpenSSLRSAPEM.INTEGER.valueOf(encryptNonceBytes.length).getBytes(4);
        headerDigest.update(encryptNonceBytesLength);
        headerDigest.update(nonce.getBytes());

        byte[] encryptCounterBytes = encryptCounter.getBytes();
        byte[] encryptCounterBytesLength = OpenSSLRSAPEM.INTEGER.valueOf(encryptCounterBytes.length).getBytes(4);
        headerDigest.update(encryptCounterBytesLength);
        headerDigest.update(counter.getBytes());

        ByteArrayOutputStream sessionInformationOutputStream = new ByteArrayOutputStream();

        sessionInformationOutputStream.write(senderFingerprint);
        sessionInformationOutputStream.write(receiverFingerprint);
        sessionInformationOutputStream.write(encryptNonceBytesLength);
        sessionInformationOutputStream.write(encryptNonceBytes);
        sessionInformationOutputStream.write(encryptCounterBytesLength);
        sessionInformationOutputStream.write(encryptCounterBytes);
        sessionInformationOutputStream.write(headerDigest.digest());
        return sessionInformationOutputStream;
    }

    /**
     * Symmetrically encrypted session content:
     * |- Sender Name Length --|-------------------------- Sender Name ------------- ~~ ---------------| ...
     * |- Recver Name Length --|------------------------- Receiver Name ------------ ~~ ---------------| ...
     * |--- Message Length ----|---------------------------- Message --------------- ~~ ---------------| ...
     * |--------------------------------------- Message Hash ------------------------------------------|
     * |--------------------------------------- Message Hash ------------------------------------------| 256
     */
    private int appendRijndaelEncryptedSessionContent(ByteArrayOutputStream toStream, OpenSSLRSAPEM.INTEGER nonce, OpenSSLRSAPEM.INTEGER counter) throws IOException {
        byte[] senderName = owner.getSenderName().getBytes(StandardCharsets.UTF_8);
        byte[] receiverName = owner.getReceiverName().getBytes(StandardCharsets.UTF_8);
        byte[] message = new byte[0];
        if (owner.getMessage() != null) {
            message = owner.getMessage().getBytes(StandardCharsets.UTF_8);
        }

        byte[] senderNameLength = OpenSSLRSAPEM.INTEGER.valueOf(senderName.length).getBytes(4);
        byte[] receiverNameLength = OpenSSLRSAPEM.INTEGER.valueOf(receiverName.length).getBytes(4);
        byte[] messageLength = OpenSSLRSAPEM.INTEGER.valueOf(message.length).getBytes(4);

        byte[] digest = SHA256.digest(
                senderNameLength,
                senderName,
                receiverNameLength,
                receiverName,
                messageLength,
                message
        );

        byte[] content = new byte[senderNameLength.length + senderName.length + receiverNameLength.length + receiverName.length + messageLength.length + message.length + digest.length];
        int position;

        position = appendBytes(content, senderNameLength, 0);
        position = appendBytes(content, senderName, position);
        position = appendBytes(content, receiverNameLength, position);
        position = appendBytes(content, receiverName, position);
        position = appendBytes(content, messageLength, position);
        position = appendBytes(content, message, position);
        position = appendBytes(content, digest, position);

        ByteArrayOutputStream tempStream = new ByteArrayOutputStream(position);

        // This encrypts the session content and appends it to the sessionInformation stream.
        des.encrypt_ctr(
                counter,
                nonce.getBytes(8),
                new ByteArrayInputStream(content),
                tempStream
        );

        tempStream.writeTo(toStream);

        return tempStream.size();
    }

    private int appendBytes(byte[] toArray, byte[] fromArray, int position) {
        System.arraycopy(fromArray, 0, toArray, position, fromArray.length);
        position += fromArray.length;
        return position;
    }
}
