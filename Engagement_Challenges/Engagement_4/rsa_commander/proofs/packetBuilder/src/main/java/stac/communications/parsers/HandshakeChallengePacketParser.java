package stac.communications.parsers;

import stac.communications.PACKETS;
import stac.communications.Packet;
import stac.communications.PacketBuffer;
import stac.communications.PacketParser;
import stac.communications.PacketParserException;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * 8     8     8     8
 * |-----|-----|-----|-----| 32
 * |-----|-----|-----|-----|-----|-----|-----|-----|-----|-----|-----|-----|-----|-----|-----|-----| 128
 * Challenge packet:
 * |pType|- Length --|----- 256 byte random challenge, or response-------------------------- ~~ ---| 128
 */
public class HandshakeChallengePacketParser extends PacketParser {

    private final HandshakeChallengePacket owner;

    HandshakeChallengePacketParser(HandshakeChallengePacket handshakeChallengePacket) {
        this.owner = handshakeChallengePacket;
    }

    @Override
    public Packet parse(PacketBuffer packetBuffer) throws PacketParserException {
        byte[] buffer = packetBuffer.getBuffer();
        int size = packetBuffer.getOffset();
        int bpos = 0;

        byte packetType = buffer[bpos++];

        if (PACKETS.HANDSHAKE_CHALLENGE.ordinal() != packetType)
            throw new PacketParserException("Malformed Handshake Challenge Packet has incorrect packet type/version.");

        int incomingSize = ((0xff & buffer[bpos++]) << 8 | (0xff & buffer[bpos++]));

        if (incomingSize != size - 3) {
            throw new PacketParserException("Malformed Handshake Packet has invalid size.");
        }

        if (size <= PACKETS.HANDSHAKE_CHALLENGE.maxSize() || size < PACKETS.HANDSHAKE_CHALLENGE.minSize()) {
            owner.setChallenge(new byte[incomingSize]);
            System.arraycopy(buffer, bpos, owner.getChallenge(), 0, incomingSize);
            return owner;
        } else throw new PacketParserException("Malformed Handshake Packet has invalid size.");
    }

    @Override
    public byte[] serialize() throws PacketParserException {
        byte[] outputArray = null;
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             DataOutputStream out = new DataOutputStream(bos)) {

            out.writeByte(PACKETS.HANDSHAKE_CHALLENGE.ordinal()); // Write Packet Type

            if (owner.getChallenge().length > 0x0000ffff) throw new PacketParserException("Invalid Handshake Challenge has unsupported challenge size.");
            out.write((byte) ((owner.getChallenge().length & 0xff00) >> 8));
            out.write((byte) (owner.getChallenge().length & 0x00ff));
            out.write(owner.getChallenge());

            outputArray = bos.toByteArray();
        } catch (IOException ignored) {
        }
        return outputArray;
    }
}
