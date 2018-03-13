package stac.communications.handlers;

import stac.communications.CONNECTION_STATE;
import stac.communications.Handler;
import stac.communications.PACKETS;
import stac.communications.Packet;
import stac.communications.PacketBuffer;
import stac.communications.PacketParserException;
import stac.communications.Session;
import stac.communications.parsers.HandshakeBeginPacket;
import stac.communications.parsers.HandshakeChallengePacket;
import stac.crypto.PublicKey;
import stac.parser.OpenSSLRSAPEM;
import stac.permissions.AnonUser;
import stac.permissions.User;

import java.io.IOException;

import static stac.communications.CONNECTION_STATE.FAILED;
import static stac.communications.CONNECTION_STATE.HANDSHAKE_ACCEPTED;
import static stac.communications.CONNECTION_STATE.HANDSHAKE_CHALLENGE;

/**
 *
 */
public class HandshakeHandler extends Handler {
    private final static Handler me = new HandshakeHandler();

    public HandshakeHandler() {
    }

    public static Handler getInstance() {
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
                        HandshakeBeginPacket.Flags flags = new HandshakeBeginPacket.Flags();
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
            Packet packet = new HandshakeBeginPacket().getParser().parse(packetBuffer);
            packetBuffer.reset();
            return packet;
        } else if (expecting == PACKETS.HANDSHAKE_CHALLENGE) {
            Packet packet = new HandshakeChallengePacket().getParser().parse(packetBuffer);
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
    public CONNECTION_STATE handlePacket(PacketBuffer packetBuffer, Session session) {
        try {
            if (session.isExpecting(PACKETS.HANDSHAKE_OPEN)) {
                HandshakeBeginPacket handshakePacket = (HandshakeBeginPacket) runPacketParser(packetBuffer, null, session.getExpecting());
                HandshakeBeginPacket.Flags flags = handshakePacket.getFlags();
                session.attachUser(handshakePacket.getKey());
                if (flags.isRegistered()) {
                    if (flags.isShouldStore()) {
                        if (session.getUser() instanceof AnonUser)
                            throw new IOException("Attempt to install certificate for missing user.");

                        HandshakeChallengePacket challenge = new HandshakeChallengePacket();
                        session.setChallenge(challenge.getChallenge());
                        session.setChallengedKey((PublicKey) handshakePacket.getKey());
                        session.setExpecting(PACKETS.HANDSHAKE_CHALLENGE);
                        session.send(challenge);
                        return HANDSHAKE_CHALLENGE;
                    } else {
                        HandshakeBeginPacket acceptPacket = new HandshakeBeginPacket();
                        acceptPacket.getFlags().setHandshakeAccepted(true).setRegistered(!flags.isRequestsReturnService());
                        acceptPacket.setKey(session.getServerKey());
                        session.setExpecting(PACKETS.REQUEST);
                        session.send(acceptPacket);
                        return HANDSHAKE_ACCEPTED;
                    }
                } else {
                    // Anonymous Communication
                    session.setExpecting(PACKETS.REQUEST);
                    HandshakeBeginPacket acceptPacket = new HandshakeBeginPacket();
                    acceptPacket.getFlags().setHandshakeAccepted(true);
                    acceptPacket.setKey(session.getServerKey());
                    session.send(acceptPacket);
                    return HANDSHAKE_ACCEPTED;
                }
            } else if (session.isExpecting(PACKETS.HANDSHAKE_CHALLENGE)) {
                HandshakeChallengePacket handshakePacket = (HandshakeChallengePacket) runPacketParser(packetBuffer, null, session.getExpecting());
                User user = session.getUser();

                if (handshakePacket.verifyChallenge(session.getChallenge(), user.getKey())) {
                    session.updateKey();
                    HandshakeBeginPacket acceptPacket = new HandshakeBeginPacket();
                    acceptPacket.getFlags().setHandshakeAccepted(true).setRegistered(true);
                    acceptPacket.setKey(session.getServerKey());
                    session.setExpecting(PACKETS.REQUEST);
                    session.send(acceptPacket);
                    return HANDSHAKE_ACCEPTED;
                } else {
                    System.err.println("Challenge verification failed. Closing session.");
                    return FAILED;
                }
            } else {
                System.err.println("Handshake handler invoked, but session is not expecting a handshake packet. Closing session.");
                return FAILED;
            }
        } catch (PacketParserException | IOException e) {
            System.err.println("A(n) " + e.getClass().getName() + " Exception has occurred. Closing session.");
            return FAILED;
        }
    }
}

