package stac.communications;

import stac.crypto.Key;
import stac.crypto.PublicKey;
import stac.parser.CommandLine;
import stac.client.Remote;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import static stac.communications.CONNECTION_PHASE.FAILED;
import static stac.communications.CONNECTION_PHASE.HANDSHAKE_REQUEST;
import static stac.communications.CONNECTION_PHASE.TERMINATING;

/**
 * Sessions are started by the listener for new TCP connections and store the packet buffers
 * for the incoming packets and other metadata required to facilitate communications with the
 * remote.
 */
public class Session {
    private final CommandLine.Options options;
    private final Communications communications;
    private final Handler handshakeRequestHandler;
    private final Handler requestHandler;
    private CONNECTION_PHASE phase = CONNECTION_PHASE.OPEN;
    private PacketBuffer packetBuffer = new PacketBuffer();

    private PACKETS expecting = null;
    private SocketChannel socketChannel;

    private Remote remote;
    private SocketAddress remoteAddress;
    private byte[] challenge;
    private PublicKey challengedKey;

    public Session(Communications communications, CommandLine.Options options, Handler handshakeRequestHandler, Handler requestHandler) {
        this.communications = communications;
        this.options = options;
        this.handshakeRequestHandler = handshakeRequestHandler;
        this.requestHandler = requestHandler;
    }

    /**
     * Handle activity on socket channels. This will run frame readers.
     *
     * @param sc The SocketChannel given by the Selector.
     * @return true if the handler is done and the session should be destroyed.
     */
    synchronized public boolean handle(SocketChannel sc) throws IOException {
        if (socketChannel == null) {
            socketChannel = sc;
        }
        if (remoteAddress == null) {
            remoteAddress = sc.getRemoteAddress();
        }
        try {
            switch (phase) {
                case OPEN:
                    expecting = PACKETS.HANDSHAKE_OPEN;
                    phase = HANDSHAKE_REQUEST; // We are going to accept immediately.
                case HANDSHAKE_REQUEST: // The first thing we do is begin waiting on a HANDSHAKE_REQUEST packet.
                    handshakeRequestHandler.initPacketBuffer(packetBuffer);
                    if (readPacket(handshakeRequestHandler)) phase = TERMINATING;
                    break;
                case HANDSHAKE_ACCEPTED:
                    requestHandler.initPacketBuffer(packetBuffer);
                    if (readPacket(requestHandler)) phase = TERMINATING;
                    break;
                case HANDSHAKE_CHALLENGE:
                    handshakeRequestHandler.initPacketBuffer(packetBuffer);
                    if (readPacket(handshakeRequestHandler)) phase = TERMINATING;
                    break;
                case FAILED:
                    break;
            }
        } catch (IOException e) {
            this.communications.postError("Something really screwed up happened, destroy session.");
            e.printStackTrace();
            phase = TERMINATING;
        }

        if (phase == FAILED) {
            this.communications.postError("Session has entered a failed state.");
            phase = TERMINATING;
        }

        return phase == TERMINATING;
    }

    /**
     * readPacket reads in packets and adjusts the packet reading / system states.
     * @param handler The handler for the current packet reading state.
     * @return true if the handler terminates the session.
     * @throws IOException
     */
    private boolean readPacket(Handler handler) throws IOException {
        switch (handler.handle(socketChannel, packetBuffer)) {
            case FAILED:
                this.communications.postError("Failed to read a packet.");
                return true;
            case WAITING:
                break; // This is the case when a packet is not fully read yet. DO NOTHING
            case DONE:
                this.phase = handler.handlePacket(packetBuffer, this); // Update the state.
                if (this.phase == TERMINATING) return true;
                break;
            case CLOSE:
                return true; // This state closes out the connection so we return true from here to indicate STOP.
        }
        return false;
    }

    synchronized void destroy() {
        packetBuffer.destroy();
    }

    synchronized public boolean isExpecting(PACKETS packet) {
        return expecting == packet;
    }

    synchronized public void setExpecting(PACKETS packet) {
        expecting = packet;
    }

    /**
     * This sends data along the socket channel attached to this session. This is not used in the client.
     * @param packet The packet to send.
     * @throws PacketParserException If the parser is unable to create a serialized copy of the packet.
     * @throws IOException If the connection suffers errors that it cannot recover from.
     */
    synchronized public void send(Packet packet) throws PacketParserException, IOException {
        socketChannel.write(ByteBuffer.wrap(packet.getParser().serialize()));
    }

    public void attachUser(Key publicKey) {
        remote = new Remote();
        remote.setKey((PublicKey) publicKey);
    }

    public Key getServerKey() {
        return communications.getListenerKey();
    }

    public Remote getRemote() {
        return remote;
    }

    private synchronized SocketAddress getRemoteAddress() {
        return remoteAddress;
    }

    public byte[] getChallenge() {
        return challenge;
    }

    public void setChallenge(byte[] challenge) {
        this.challenge = challenge;
    }

    public void updateKey() {
        remote.setKey(getChallengedKey());
    }

    public PublicKey getChallengedKey() {
        return challengedKey;
    }

    public void setChallengedKey(PublicKey challengedKey) {
        this.challengedKey = challengedKey;
    }

    synchronized public void setPhase(CONNECTION_PHASE phase) {
        this.phase = phase;
    }

    public Communications getCommunications() {
        return communications;
    }

    public PACKETS getExpecting() {
        return expecting;
    }
}
