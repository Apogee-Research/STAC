package stac.communications;

import stac.crypto.Key;
import stac.crypto.PublicKey;
import stac.parser.CommandLine;
import stac.permissions.AnonUser;
import stac.permissions.User;
import stac.server.UserStore;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import static stac.communications.CONNECTION_STATE.FAILED;
import static stac.communications.CONNECTION_STATE.HANDSHAKE_REQUEST;
import static stac.communications.CONNECTION_STATE.TERMINATING;

/**
 *
 */
public class Session {
    private final CommandLine.Options options;
    private final Communications communications;
    private final UserStore userStore;
    private final Handler handshakeRequestHandler;
    private final Handler requestHandler;
    private CONNECTION_STATE state = CONNECTION_STATE.OPEN;
    private PacketBuffer packetBuffer = new PacketBuffer();

    private PACKETS expecting = null;
    private SocketChannel socketChannel;

    private User user;
    private SocketAddress remoteAddress;
    private byte[] challenge;
    private PublicKey challengedKey;

    public Session(Communications communications, CommandLine.Options options, UserStore userStore, Handler handshakeRequestHandler, Handler requestHandler) {
        this.communications = communications;
        this.options = options;
        this.userStore = userStore;
        this.handshakeRequestHandler = handshakeRequestHandler;
        this.requestHandler = requestHandler;
    }

    /**
     * Handle activity on socket channels. This will run packet readers.
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
            switch (state) {
                case OPEN:
                    expecting = PACKETS.HANDSHAKE_OPEN;
                    state = HANDSHAKE_REQUEST; // We are going to accept immediately.
                case HANDSHAKE_REQUEST: // The first thing we do is begin waiting on a HANDSHAKE_REQUEST packet.
                    handshakeRequestHandler.initPacketBuffer(packetBuffer);
                    if (readPacket(handshakeRequestHandler)) state = TERMINATING;
                    break;
                case HANDSHAKE_ACCEPTED:
                    requestHandler.initPacketBuffer(packetBuffer);
                    if (readPacket(requestHandler)) state = TERMINATING;
                    break;
                case HANDSHAKE_CHALLENGE:
                    handshakeRequestHandler.initPacketBuffer(packetBuffer);
                    if (readPacket(handshakeRequestHandler)) state = TERMINATING;
                    break;
                case FAILED:
                    break;
            }
        } catch (IOException e) {
            System.err.println("Something really screwed up happened, destroy session.");
            e.printStackTrace();
            state = TERMINATING;
        }

        if (state == FAILED) {
            System.err.println("Session has entered a failed state.");
            state = TERMINATING;
        }

        return state == TERMINATING;
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
                System.err.println("Failed to read a packet.");
                return true;
            case WAITING:
                break; // This is the case when a packet is not fully read yet. DO NOTHING
            case DONE:
                this.state = handler.handlePacket(packetBuffer, this); // Update the state.
                if (this.state == TERMINATING) return true;
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

    synchronized public void send(Packet packet) throws PacketParserException, IOException {
        socketChannel.write(ByteBuffer.wrap(packet.getParser().serialize()));
    }

    public void attachUser(Key publicKey) {
        if (userStore != null) {
            user = userStore.findUser(publicKey);
        } else {
            user = null;
        }
        if (user == null) {
            user = new AnonUser(getRemoteAddress());
        }
    }

    public Key getServerKey() {
        return communications.getListenerKey();
    }

    public User getUser() {
        return user;
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
        user.setKey(getChallengedKey());
    }

    public PublicKey getChallengedKey() {
        return challengedKey;
    }

    public void setChallengedKey(PublicKey challengedKey) {
        this.challengedKey = challengedKey;
    }

    synchronized public void setState(CONNECTION_STATE state) {
        this.state = state;
    }

    public Communications getCommunications() {
        return communications;
    }

    public PACKETS getExpecting() {
        return expecting;
    }
}
