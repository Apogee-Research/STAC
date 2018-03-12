package stac.communications.handlers;

import stac.communications.CONNECTION_PHASE;
import stac.communications.Handler;
import stac.communications.PACKETS;
import stac.communications.Packet;
import stac.communications.PacketBuffer;
import stac.communications.PacketParserException;
import stac.communications.Session;
import stac.communications.packets.HandshakePacket;
import stac.crypto.Key;
import stac.parser.OpenSSLRSAPEM;

import java.io.IOException;

import static stac.communications.CONNECTION_PHASE.FAILED;
import static stac.communications.CONNECTION_PHASE.HANDSHAKE_ACCEPTED;

/**
 * This handler is designed to handle handshakes and any challenges.
 */
public class HandshakeHandler extends Handler {
    private final static Handler me = new HandshakeHandler();

    public HandshakeHandler() {
    }

    static Handler getInstance() {
        return me;
    }

    @Override
    /**
     *  * 8     8     8     8
     * |-----|-----|-----|-----| 32
     * |-----|-----|-----|-----|-----|-----|-----|-----|-----|-----|-----|-----|-----|-----|-----|-----| 128
     * |pType|- Request Public Key Fingerprint -----------------------SHA256---------------------------|
     * |------- Request Public Key Fingerprint -----------------------SHA256---------------------|-flg-|
     * || Optional ||
     * |-- pSize --|--------- Public Exponent ----- ~~ -------- ~~ -------- ~~ -------- ~~ -------- ~~ |
     * |-- mSize --|--------- Public Key Modulus -- ~~ -------- ~~ -------- ~~ -------- ~~ -------- ~~ |
     *
     * or
     *
     * Challenge packet:
     * |pType|- Length --|----- 256 byte random challenge, or response-------------------------- ~~ ---| 128
     */
    protected boolean isPacketFormed(PacketBuffer packetBuffer) {
        try {
            if (packetBuffer.getOffset() > 0) {
                if (PACKETS.HANDSHAKE_OPEN.ordinal() == packetBuffer.getBuffer()[0]) {
                    if (packetBuffer.getOffset() >= getMinPacketSize()) {
                        HandshakePacket.Flags flags = new HandshakePacket.Flags();
                        flags.fromByte(packetBuffer.getBuffer()[33]);
                        if (flags.isRegistered() && packetBuffer.getOffset() == getMinPacketSize()) {
                            return true;
                        }
                        Integer pSize = OpenSSLRSAPEM.INTEGER.valueOfUnsigned(packetBuffer.getBuffer(), 34, 2).getInternal();
                        if (packetBuffer.getOffset() > 36 + pSize) {
                            Integer mSize = OpenSSLRSAPEM.INTEGER.valueOfUnsigned(packetBuffer.getBuffer(), 36 + pSize, 2).getInternal();
                            return packetBuffer.getOffset() == 38 + pSize + mSize;
                        }
                    }
                } else if (PACKETS.HANDSHAKE_CHALLENGE.ordinal() == packetBuffer.getBuffer()[0]) {
                    if (packetBuffer.getOffset() >= PACKETS.HANDSHAKE_CHALLENGE.minSize()) {
                        Integer len = OpenSSLRSAPEM.INTEGER.valueOfUnsigned(packetBuffer.getBuffer(), 1, 2).getInternal();
                        return packetBuffer.getOffset() == 3 + len;
                    }
                }
            }
            return false;
        } catch (ArrayIndexOutOfBoundsException ex) {
            return false;
        }
    }

    @Override
    protected boolean isPacketStillOK(PacketBuffer packetBuffer) {
        return true;
    }

    @Override
    protected int getMaxPacketSize() {
        return PACKETS.HANDSHAKE_OPEN.maxSize();
    }

    @Override
    protected int getMinPacketSize() {
        return PACKETS.HANDSHAKE_OPEN.minSize();
    }

    /**
     * Runs the packet parser associated with this Handler.
     * <p>
     * This RESETS the packetBuffer.
     *
     * @param packetBuffer The read in packet.
     * @param session
     * @return The Packet produced by the parser.
     */
    @Override
    public Packet runPacketParser(PacketBuffer packetBuffer, Session session, PACKETS expecting) throws PacketParserException {
        if (expecting == PACKETS.HANDSHAKE_OPEN) {
            Packet packet = new HandshakePacket().getParser().parse(packetBuffer, null);
            packetBuffer.reset();
            return packet;
        } else {
            throw new RuntimeException("Handshake Handler: Something is not implemented");
        }
    }

    /**
     * The packet is handled with this method and the next connection state is returned by it.
     *
     * @param packetBuffer The packet buffer containing a packet which is ready to be parsed and acted upon.
     * @param session      The session that this Handshake handler belongs to.
     * @return The next connection state.
     */
    @Override
    public CONNECTION_PHASE handlePacket(PacketBuffer packetBuffer, Session session) {
        try {
            if (session.isExpecting(PACKETS.HANDSHAKE_OPEN)) {
                HandshakePacket handshakePacket = (HandshakePacket) runPacketParser(packetBuffer, null, session.getExpecting());
                HandshakePacket.Flags flags = handshakePacket.getFlags();
                if (flags.isRegistered()) {
                    session.getCommunications().postError("Registered users are not supported");
                    return FAILED;
                }
                Key remoteKey = handshakePacket.getKey();

                if (remoteKey.getPem().getPublicExponent().compareTo(3) < 0 || remoteKey.getPem().getPublicExponent().compareTo(65537) > 0
                        || remoteKey.getPem().getModulus().getInternalBig().bitCount() < 0 || remoteKey.getPem().getModulus().getInternalBig().bitCount() > 512) {
                    session.getCommunications().postError("Invalid key size detected; Terminating Connection");
                    return FAILED;
                } else {
                    session.attachUser(remoteKey);
                }
                if (flags.isRegistered()) {
                    throw new RuntimeException("Registered users are not implemented");
                } else {
                    // Anonymous Communication
                    session.setExpecting(PACKETS.REQUEST);
                    HandshakePacket acceptPacket = new HandshakePacket();
                    acceptPacket.getFlags().setHandshakeAccepted(true);
                    acceptPacket.setKey(session.getServerKey());
                    session.send(acceptPacket);
                    return HANDSHAKE_ACCEPTED;
                }
            } else {
                session.getCommunications().postError("Handshake handler invoked, but session is not expecting a handshake packet. Closing session.");
                return FAILED;
            }
        } catch (PacketParserException | IOException e) {
            session.getCommunications().postError("A(n) " + e.getClass().getName() + " Exception has occurred. Closing session.");
            return FAILED;
        }
    }
}

