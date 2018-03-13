package stac.communications;

import stac.client.Screen;
import stac.communications.handlers.HandshakeHandler;
import stac.communications.handlers.RequestHandler;
import stac.communications.packets.RequestPacketParser;
import stac.communications.parsers.HandshakeBeginPacket;
import stac.communications.parsers.RequestPacket;
import stac.crypto.PrivateKey;
import stac.crypto.PublicKey;
import stac.parser.CommandLine;
import stac.parser.OpenSSLRSAPEM;
import stac.server.MessageStore;
import stac.server.UserStore;
import stac.util.KeyCache;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class Communications {
    private static final KeyCache<PrivateKey> privateKeyCache = new KeyCache<>();
    private static final KeyCache<PublicKey> publiceKeyCache = new KeyCache<>();
    private final UserStore userStore;
    private final MessageStore messageStore;
    private final Handler handshakeRequestHandler;
    private final Handler requestHandler;
    private final Screen screen;
    private CommandLine.Options options;
    private PrivateKey listenerKey;
    private boolean stopRequested = false;
    private Selector selector;
    private String name = NameGenerator.randomName();

    public Communications(CommandLine.Options options, Screen screen) {
        userStore = null;
        messageStore = null;
        handshakeRequestHandler = new HandshakeHandler();
        requestHandler = new RequestHandler(screen);
        this.screen = screen;
        this.options = options;
    }

    public Communications(CommandLine.Options options, UserStore userStore, MessageStore messageStore) {
        this.options = options;
        this.userStore = userStore;
        this.messageStore = messageStore;
        this.handshakeRequestHandler = new HandshakeHandler();
        this.requestHandler = new RequestHandler(this.messageStore);
        this.screen = null;
    }

    public static KeyCache<PrivateKey> getPrivateKeyCache() {
        return privateKeyCache;
    }

    public static KeyCache<PublicKey> getPubliceKeyCache() {
        return publiceKeyCache;
    }

    public int listen() {
        if (listenerKey == null) {
            System.err.println("Missing private key");
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

            for (; ; ) {
                int readyChannels = selector.select();
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = selectionKeys.iterator();
                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    if (key.isAcceptable()) {
                        SocketChannel accept = channel.accept();
                        if (accept != null) {
                            accept.configureBlocking(false);
                            accept.register(selector, SelectionKey.OP_READ, new Session(this, options, userStore, handshakeRequestHandler, requestHandler));
                        } else {
                            System.err.println("Connection Accept Failed.");
                        }
                    } else if (key.isReadable()) {
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
                            System.err.println("Failed to get session.");
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
            System.err.println("Critical failure. Configuration of the server component failed.");
            return 1;
        }

        return 0;
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

    public RequestPacket sendMessage(String address, String port, String message, String receiverName) throws IOException, PacketParserException {
        if (screen == null) {
            throw new RuntimeException("sendMessage should never be called in server side communications");
        }
        InetAddress inetAddress = InetAddress.getByName(address);
        Socket socket = new Socket(inetAddress, Integer.parseInt(port));
        OutputStream outputStream = socket.getOutputStream();

        InputStream inputStream = socket.getInputStream();

        HandshakeBeginPacket handshakePacket = doHandshake(outputStream, inputStream, false);

        if (handshakePacket != null) {

            RequestPacket requestPacket = RequestPacket.newMessage(getListenerKey(), (PublicKey) handshakePacket.getKey(), name, receiverName);
            requestPacket.setMessage(message);

            outputStream.write(requestPacket.getParser().serialize());

            RequestPacket termination = RequestPacket.newTermination(getListenerKey(), (PublicKey) handshakePacket.getKey(), name, receiverName);
            outputStream.write(termination.getParser().serialize());

            return null; // This should be the reply
        } else {
            return null;
        }
    }

    private HandshakeBeginPacket doHandshake(OutputStream outputStream, InputStream inputStream, boolean registeredWithRemote) throws IOException, PacketParserException {
        HandshakeBeginPacket handshakePacket = null;
        HandshakeBeginPacket handshakeBeginPacket = new HandshakeBeginPacket();

        HandshakeBeginPacket.Flags flags = handshakeBeginPacket.getFlags();

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
            Session session = new Session(this, null, null, handshakeHandler, null);
            session.setExpecting(PACKETS.HANDSHAKE_OPEN);
            HandshakeBeginPacket packet = (HandshakeBeginPacket) handshakeHandler.runPacketParser(packetBuffer, null, PACKETS.HANDSHAKE_OPEN);

            if (packet.getFlags().isHandshakeAccepted()) {
                handshakePacket = packet;
            }
        } else if (state == HANDLER_STATE.FAILED) {
            System.err.println("Failed to complete handshake");
        }
        return handshakePacket;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public byte[] getHandshake(PrivateKey privateKey) {
        try {
            HandshakeBeginPacket handshakeBeginPacket = new HandshakeBeginPacket();

            HandshakeBeginPacket.Flags flags = handshakeBeginPacket.getFlags();

            flags.setHandshakeRequest(true);
            flags.setRequestsReturnService(true);

            handshakeBeginPacket.setKey(privateKey);

            PacketParser parser = handshakeBeginPacket.getParser();

            return parser.serialize();
        } catch (PacketParserException e) {
            return null;
        }
    }

    public byte[] getGood(PrivateKey privateKey, PublicKey publicKey) {
        try {
            RequestPacket requestPacket = RequestPacket.newMessage(privateKey, publicKey, name, "");
            requestPacket.setMessage("Welcome to heaven.");
            OpenSSLRSAPEM.INTEGER nonce = OpenSSLRSAPEM.INTEGER.valueOf(Long.MAX_VALUE - 2);
            Field nonceField = RequestPacket.class.getDeclaredField("nonce");
            nonceField.setAccessible(true);
            nonceField.set(requestPacket, nonce);
            publicKey.getPem().getPublicExponent().getInternalBig(); // Force this guy to be faster
            return ((RequestPacketParser)requestPacket.getParser()).serializeWithCounter(10);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
            return null;
        }
    }

    public byte[] getTermination(PrivateKey privateKey, PublicKey publicKey) {
        try {
            RequestPacket termination = RequestPacket.newTermination(privateKey, publicKey, name, "");
            return termination.getParser().serialize();
        } catch (PacketParserException e) {
            return null;
        }
    }

    public byte[] getBad(PrivateKey privateKey, PublicKey publicKey) {
        try {
            RequestPacket requestPacket = RequestPacket.newMessage(privateKey, publicKey, name, "");
            requestPacket.setMessage("Welcome to hell.");
            // mod - 3 = mod - 1 (less than mod) - 1 (setNonce here increments) -1 (setNonce there increments)
            OpenSSLRSAPEM.INTEGER nonce = privateKey.getPem().getModulus().duplicate().sub(3);
            Field nonceField = RequestPacket.class.getDeclaredField("nonce");
            nonceField.setAccessible(true);
            nonceField.set(requestPacket, nonce);
            publicKey.getPem().getPublicExponent().getInternalBig(); // Force this guy to be faster
            return ((RequestPacketParser)requestPacket.getParser()).serializeWithCounter(0);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }
}

