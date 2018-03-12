package stac.communications.parsers;

import stac.communications.PACKETS;
import stac.communications.Packet;
import stac.communications.PacketBuffer;
import stac.communications.PacketParser;
import stac.communications.PacketParserException;
import stac.parser.OpenSSLRSAPEM;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * 8     8     8     8
 * |-----|-----|-----|-----| 32
 * |-----|-----|-----|-----|-----|-----|-----|-----|-----|-----|-----|-----|-----|-----|-----|-----| 128
 * |pType|- Request Public Key Fingerprint -----------------------SHA256---------------------------|
 * |------- Request Public Key Fingerprint -----------------------SHA256---------------------|-flg-|
 * || Optional ||
 * |-- pSize --|--------- Public Exponent ----- ~~ -------- ~~ -------- ~~ -------- ~~ -------- ~~ |
 * |-- mSize --|--------- Public Key Modulus -- ~~ -------- ~~ -------- ~~ -------- ~~ -------- ~~ |
 * <p>
 * <p>
 * pType is for determining the current packet's type/version.
 * Request Public Key Fingerprint is a SHA256 hash of the public key components of the key. (public exp, modulus)
 * -- This is used to determine which public key to use for Authentication. But this means that the user is in the
 * -- servers cache.
 * flg:
 * 1) Registered Public Key. This is used to determine if the fingerprint can be used to look up the key or not.
 * Disables:
 * pSize (public exponent size)
 * Public Exponent
 * mSize (modulus size / key size)
 * Public Key Modulus
 * 2) Server Should Store. This is used to determine if an update to a key is being performed.
 * In this case the Fingerprint still refers to the old key in order to update it by fingerprint.
 * ** The server will challenge the update by requesting a challenge to be signed and returned **
 * 3) Requests Return Service. This is used to ask for a public key from the other party.
 * ** The client will challenge this new public key to ensure that the other user can sign things with it **
 * 4) Handshake Request. This is set to request an ongoing communication.
 * 5) Handshake Accepted. This is set to confirm an open ongoing communication.
 * 6) Handshake Stalled. This is set to allow more key passing and verification without a restart.
 * 7) Handshake Failed. This is set to tell the alternate party of a failure state.
 */
public class HandshakeBeginPacketParser extends PacketParser {

    private HandshakeBeginPacket owner;

    HandshakeBeginPacketParser(HandshakeBeginPacket owner) {
        this.owner = owner;
    }

    @Override
    public Packet parse(PacketBuffer packetBuffer) throws PacketParserException {
        byte[] buffer = packetBuffer.getBuffer();
        int size = packetBuffer.getOffset();
        int bpos = 0;

        byte packetType = buffer[bpos++];

        if (PACKETS.HANDSHAKE_OPEN.ordinal() != packetType)
            throw new PacketParserException("Malformed Handshake Packet has incorrect packet type/version.");

        if (size >= 32 + 1 + 1 /* 32 bytes for the fingerprint, one for the packet type, and one for the flg field*/) {
            owner.getKey().setFingerPrint(parseFingerprint(buffer, bpos));
            bpos += 32;

            owner.getFlags().fromByte(buffer[bpos++]);

            if (!owner.getFlags().isRegistered()) {
                if (bpos + 1 >= size) throw new PacketParserException("Malformed Handshake Packet has invalid size.");

                short publicExponentSizeBytes = (short) ((0xff & buffer[bpos++]) << 8 | (0xff & buffer[bpos++]));

                if (bpos + publicExponentSizeBytes >= size)
                    throw new PacketParserException("Malformed Handshake Packet has invalid size.");

                OpenSSLRSAPEM.INTEGER pubExp = OpenSSLRSAPEM.INTEGER.valueOf(buffer, bpos, publicExponentSizeBytes);

                bpos += publicExponentSizeBytes;

                if (bpos + 1 >= size) throw new PacketParserException("Malformed Handshake Packet has invalid size.");
                short modulusSize = (short) (((0xff & buffer[bpos++]) << 8) | (0xff & buffer[bpos++]));

                if (bpos + modulusSize > size)
                    throw new PacketParserException("Malformed Handshake Packet has invalid size.");

                OpenSSLRSAPEM.INTEGER modulus = OpenSSLRSAPEM.INTEGER.valueOf(buffer, bpos, modulusSize);

                bpos += modulusSize;

                owner.getKey().setPem(new OpenSSLRSAPEM(pubExp, modulus));
            }

            return owner;
        } else throw new PacketParserException("Malformed Handshake Packet has invalid size.");
    }

    private byte[] parseFingerprint(byte[] buffer, int offset) {
        byte[] fp = null;
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream(32);
             DataOutputStream out = new DataOutputStream(bos)) {

            out.write(buffer, offset, 32);

            fp = bos.toByteArray();
        } catch (IOException ignored) {
        }
        return fp;
    }

    @Override
    public byte[] serialize() throws PacketParserException {
        byte[] outputArray = null;
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             DataOutputStream out = new DataOutputStream(bos)) {

            out.writeByte(PACKETS.HANDSHAKE_OPEN.ordinal()); // Write Packet Type

            out.write(owner.getKey().getFingerPrint()); // Write Key Fingerprint

            out.write(owner.getFlags().toByte()); // Write Flags

            if (!owner.getFlags().isRegistered()) {
                byte[] pubExp = owner.getKey().getPem().getPublicExponent().getBytes();
                if (pubExp.length > 0x0000ffff) throw new PacketParserException("Invalid Handshake Packet has unsupported public exponent size.");
                out.write((byte) ((pubExp.length & 0xff00) >> 8));
                out.write((byte) (pubExp.length & 0x00ff));
                out.write(pubExp);

                byte[] modulus = owner.getKey().getPem().getModulus().getBytes();
                if (modulus.length > 0x0000ffff) throw new PacketParserException("Invalid Handshake Packet has unsupported modulus size.");
                out.write((byte) ((modulus.length & 0xff00) >> 8));
                out.write((byte) (modulus.length & 0x00ff));
                out.write(modulus);
            }

            outputArray = bos.toByteArray();
        } catch (IOException ignored) {
        }
        return outputArray;
    }
}
