package stac.communications;

import stac.client.Screen;
import stac.communications.handlers.HandshakeHandler;
import stac.communications.handlers.RequestHandler;
import stac.communications.packets.HandshakePacket;
import stac.communications.packets.RequestPacket;
import stac.crypto.PrivateKey;
import stac.crypto.PublicKey;
import stac.parser.CommandLine;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * This class is responsible for sending messages, listening for connections,
 * attaching sessions, and delegating data reads and reactions into Sessions
 * and lower into handlers and parsers.
 */
public class Communications {
    private CommandLine.Options options;
    private PrivateKey listenerKey;

    private final Handler handshakeRequestHandler;
    private final Handler requestHandler;

    private boolean stopRequested = false;
    private Selector selector;
    private String name = NameGenerator.randomName();
    private final Screen screen;

    public Communications(CommandLine.Options options, Screen screen) {
        handshakeRequestHandler = new HandshakeHandler();
        requestHandler = new RequestHandler(screen);
        this.screen = screen;
        this.options = options;
    }

    public void postError(String s, boolean redraw) {
        if (screen == null) {
            System.err.println(s);
        } else {
            screen.postError(s, redraw);
        }
    }

    public int listen() { // Listen on a port for messages.
        if (listenerKey == null) {
            postError("Missing private key", false);
            return 1;
        }
        try {
            CommandLine.Option bindAddr = options.findByLongOption("bind-address");
            CommandLine.Option bindPort = options.findByLongOption("bind-port");

            ServerSocketChannel channel = ServerSocketChannel.open();
            selector = Selector.open();
            channel.configureBlocking(false);
            channel.bind(new InetSocketAddress(bindAddr.getValue(), Integer.parseInt(bindPort.getValue())));
            channel.register(selector, SelectionKey.OP_ACCEPT);

            for (;;) {
                selector.select();
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = selectionKeys.iterator();
                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    if (key.isAcceptable()) { // New connection; attach new session.
                        SocketChannel accept = channel.accept();
                        if (accept != null) {
                            accept.configureBlocking(false);
                            accept.register(selector, SelectionKey.OP_READ, new Session(this, options, handshakeRequestHandler, requestHandler));
                        } else {
                            postError("Connection Accept Failed.");
                        }
                    } else if (key.isReadable()) { // Incoming data; push it into the session code.
                        Session session = (Session) key.attachment();
                        if (session != null) {
                            SocketChannel sc = (SocketChannel) key.channel();
                            if (session.handle(sc)) {
                                // Session requests the destruction of the session, the socket, and the registration to the selector.
                                key.attach(null);
                                session.destroy();
                                sc.close();
                                key.cancel();
                            }
                        } else {
                            postError("Failed to get session.");
                        }
                    }
                    iterator.remove();
                }

                if (stopRequested) {
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (CommandLine.InvalidOptionException e) {
            postError("Critical failure. Configuration of the server component failed.", false);
            return 1;
        }

        return 0;
    }

    public void postError(String s) {
        postError(s, true);
    }

    /**
     * The listener key is set when the server begins listening.
     *
     * @return The key.
     */
    public PrivateKey getListenerKey() {
        return listenerKey;
    }

    public void setListenerKey(PrivateKey listenerKey) {
        this.listenerKey = listenerKey;
    }

    public void stop() {
        stopRequested = true;
        selector.wakeup();
    }

    /**
     * This sends the message to the remote including the handshake and termination packets.
     * @param address The address to send the message to.
     * @param port The port on the remote to send the message to.
     * @param message The message itself.
     * @param receiverName The name of the user that the sender expects is sitting on the remote.
     * @return null always.
     * @throws IOException if the socket is damaged or terminated for any reason.
     * @throws PacketParserException if the parsers cannot serialize any of the
     *                               packets that would have been sent.
     */
    public void sendMessage(String address, String port, String message, String receiverName) throws IOException, PacketParserException {
        if (screen == null) {
            throw new RuntimeException("sendMessage should never be called in server side communications");
        }
        InetAddress inetAddress = InetAddress.getByName(address);
        Socket socket = new Socket(inetAddress, Integer.parseInt(port));
        OutputStream outputStream = socket.getOutputStream();

        InputStream inputStream = socket.getInputStream();

        HandshakePacket handshakePacket = doHandshake(outputStream, inputStream, false);

        if (handshakePacket != null) {

            RequestPacket requestPacket = RequestPacket.newMessage(getListenerKey(), (PublicKey) handshakePacket.getKey(), name, receiverName);
            requestPacket.setMessage(message);

            outputStream.write(requestPacket.getParser().serialize());

            RequestPacket termination = RequestPacket.newTermination(getListenerKey(), requestPacket, (PublicKey) handshakePacket.getKey());
            outputStream.write(termination.getParser().serialize());

            RequestHandler requestHandler = new RequestHandler(screen);
            PacketBuffer packetBuffer = new PacketBuffer();
            HANDLER_STATE handlerstate;
            while ((handlerstate = requestHandler.handle(inputStream, packetBuffer)) == HANDLER_STATE.WAITING);
            if (handlerstate == HANDLER_STATE.DONE) {
                Session session = new Session(this, options, null, null);
                session.attachUser(handshakePacket.getKey());
                RequestPacket packet = (RequestPacket) requestHandler.runPacketParser(packetBuffer, session, PACKETS.REQUEST);
                if (packet.getType() == RequestPacket.RequestType.Terminate) {
                    screen.postOutput("Message Sent successfully.");
                } else {
                    screen.postError("Message failed to send successfully. Server responded unexpectedly.");
                }
            } else {
                screen.postError("Acknowledgement failed. Remote user may not have received your message.");
            }
        }

        socket.close();
    }

    /**
     * This sets up a communication socket with a remote RSA commander instance.
     * @param outputStream The stream that is connected to the remote.
     * @param inputStream The stream from the connected remote.
     * @param registeredWithRemote True if the remote should look up the public key instead
     *                             of relying on the one provided.
     * @return The handshake response from the remote. (should contain their public key)
     * @throws IOException If the socket becomes damaged.
     * @throws PacketParserException If the parsers cannot serialize packets to send them to the remote.
     */
    private HandshakePacket doHandshake(OutputStream outputStream, InputStream inputStream, boolean registeredWithRemote) throws IOException, PacketParserException {
        HandshakePacket handshakePacket = null;
        HandshakePacket handshakeBeginPacket = new HandshakePacket();

        HandshakePacket.Flags flags = handshakeBeginPacket.getFlags();

        // I'm probably registered
        flags.setRegistered(registeredWithRemote);
        flags.setHandshakeRequest(true);
        flags.setRequestsReturnService(true);

        handshakeBeginPacket.setKey(listenerKey);

        PacketParser parser = handshakeBeginPacket.getParser();

        outputStream.write(parser.serialize());

        HandshakeHandler handshakeHandler = new HandshakeHandler();
        PacketBuffer packetBuffer = new PacketBuffer(PACKETS.HANDSHAKE_OPEN.minSize(), PACKETS.HANDSHAKE_OPEN.maxSize());
        HANDLER_STATE state;
        do {
            state = handshakeHandler.handle(inputStream, packetBuffer);
        } while (state == HANDLER_STATE.WAITING);

        if (state == HANDLER_STATE.DONE) {
            Session session = new Session(this, null, handshakeHandler, null);
            session.setExpecting(PACKETS.HANDSHAKE_OPEN);
            HandshakePacket packet = (HandshakePacket) handshakeHandler.runPacketParser(packetBuffer, null, PACKETS.HANDSHAKE_OPEN);

            if (packet.getFlags().isHandshakeAccepted()) {
                handshakePacket = packet;
            }
        } else if (state == HANDLER_STATE.FAILED) {
            screen.postError("Failed to complete handshake");
        }
        return handshakePacket;
    }

}

