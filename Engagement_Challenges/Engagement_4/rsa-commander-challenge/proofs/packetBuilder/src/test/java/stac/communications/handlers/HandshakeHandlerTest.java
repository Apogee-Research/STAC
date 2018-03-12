package stac.communications.handlers;

import org.easymock.Capture;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.annotation.MockNice;
import org.powermock.modules.junit4.PowerMockRunner;
import stac.communications.CONNECTION_STATE;
import stac.communications.Communications;
import stac.communications.PACKETS;
import stac.communications.Packet;
import stac.communications.PacketBuffer;
import stac.communications.PacketParserException;
import stac.communications.Session;
import stac.communications.parsers.HandshakeBeginPacket;
import stac.communications.parsers.HandshakeChallengePacket;
import stac.crypto.PrivateKey;
import stac.crypto.PublicKey;
import stac.parser.CommandLine;
import stac.parser.OpenSSLRSAPEM;
import stac.permissions.User;
import stac.server.MessageStore;
import stac.server.UserStore;

import javax.util.ChannelWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import static org.easymock.EasyMock.and;
import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isA;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.powermock.api.easymock.PowerMock.createNiceMock;
import static org.powermock.api.easymock.PowerMock.expectLastCall;
import static org.powermock.api.easymock.PowerMock.replay;
import static org.powermock.api.easymock.PowerMock.verify;

/**
 *
 */
@RunWith(PowerMockRunner.class)
public class HandshakeHandlerTest {
    private HandshakeHandler handler;
    private Communications communications;
    private CommandLine.Options options;
    private UserStore userStore;

    @MockNice
    private MessageStore messageStore;

    @Before
    public void setUp() throws Exception {
        CommandLine commandLine = new CommandLine("");
        String userStore = ClassLoader.getSystemResource("userStore").getPath();
        String keyStore = ClassLoader.getSystemResource("test.pem").getPath();
        keyStore = keyStore.substring(0, keyStore.length() - "test.pem".length());
        commandLine.newOption().longOption("user-store").hasValue(true, userStore).done();
        commandLine.newOption().longOption("key-store").hasValue(true, keyStore).done();
        options = commandLine.parse(new String[]{});
        this.userStore = new UserStore(options);

        communications = new Communications(options, this.userStore, messageStore);
        PrivateKey listenerKey = new PrivateKey(new OpenSSLRSAPEM(OpenSSLRSAPEM.INTEGER.valueOf(65537), OpenSSLRSAPEM.INTEGER.randomINTEGER(128), OpenSSLRSAPEM.INTEGER.randomINTEGER(64)));
        communications.setListenerKey(listenerKey);
        handler = (HandshakeHandler) HandshakeHandler.getInstance();
    }

    @After
    public void tearDown() throws Exception {
        handler = null;
        communications = null;
        options = null;
        userStore = null;
    }

    @Test
    public void testGetMinPacketSize() throws Exception {
        assertEquals(PACKETS.HANDSHAKE_OPEN.minSize(), handler.getMinPacketSize());
    }

    @Test
    public void testGetMaxPacketSize() throws Exception {
        assertEquals(PACKETS.HANDSHAKE_OPEN.maxSize(), handler.getMaxPacketSize());
    }

    @Test
    public void testRunPacketParser() throws Exception {
        byte[] buffer = new byte[]{
                (byte) PACKETS.HANDSHAKE_OPEN.ordinal(), 0, 2, 4, 8, 16, 32, 64, 2, 4, 8, 16, 32, 64, 2, 4, 8, 16, 32, 64, 2, 4, 8, 16, 32, 64, 2, 4, 8, 16, 32, 64, 0, 1
        };

        PacketBuffer packetBuffer = new PacketBuffer();
        handler.initPacketBuffer(packetBuffer);

        packetBuffer.write(buffer);

        Session niceMock = EasyMock.createNiceMock(Session.class);
        expect(niceMock.getExpecting()).andReturn(PACKETS.HANDSHAKE_OPEN);
        replay(niceMock);

        Packet packet = handler.runPacketParser(packetBuffer, null, PACKETS.HANDSHAKE_OPEN);

        packetBuffer.resize(0, 500);
        assertEquals(0, packetBuffer.getOffset());
    }

    @Test
    public void testNotExpected() throws Exception {
        Session session = new Session(communications, options, userStore, new HandshakeHandler(), new RequestHandler(messageStore));
        session.setExpecting(PACKETS.HANDSHAKE_CHALLENGE);

        HandshakeBeginPacket handshakeBeginPacket = new HandshakeBeginPacket();
        handshakeBeginPacket.getFlags().setHandshakeRequest(true).setRegistered(true);
        PublicKey key = new PublicKey();
        key.setFingerPrint(new byte[32]);
        handshakeBeginPacket.setKey(key);

        PacketBuffer packetBuffer = new PacketBuffer();

        handler.initPacketBuffer(packetBuffer);

        packetBuffer.write(handshakeBeginPacket.getParser().serialize());

        assertEquals(CONNECTION_STATE.FAILED, handler.handlePacket(packetBuffer, session));
    }

    @Test
    public void testAcceptHandshake() throws Exception, PacketParserException {
        Session session = new Session(communications, options, userStore, new HandshakeHandler(), new RequestHandler(messageStore));

        SocketChannel channel = createNiceMock(SocketChannel.class);
        SocketAddress addr = createNiceMock(SocketAddress.class);
        Socket socket = createNiceMock(Socket.class);

        channel.getRemoteAddress();
        expectLastCall().andReturn(addr);

        channel.socket();
        expectLastCall().andReturn(socket);

        socket.getInputStream();

        expectLastCall().andReturn(new ByteArrayInputStream("".getBytes())); // We are going to ignore this part anyway.

        replay(channel, addr, socket);

        session.handle(channel);
        session.setExpecting(PACKETS.HANDSHAKE_OPEN);

        HandshakeBeginPacket handshakeBeginPacket = new HandshakeBeginPacket();
        handshakeBeginPacket.getFlags().setHandshakeRequest(true).setRegistered(true);
        handshakeBeginPacket.setKey(new PublicKey(new OpenSSLRSAPEM(OpenSSLRSAPEM.INTEGER.valueOf(65537), OpenSSLRSAPEM.INTEGER.randomINTEGER(1024))));

        PacketBuffer packetBuffer = new PacketBuffer();

        handler.initPacketBuffer(packetBuffer);

        packetBuffer.write(handshakeBeginPacket.getParser().serialize());

        assertEquals(CONNECTION_STATE.HANDSHAKE_ACCEPTED, handler.handlePacket(packetBuffer, session));
    }

    @Test
    public void testFullHandleOpenToAcceptHandshake() throws Exception, PacketParserException {
        Session session = new Session(communications, options, userStore, new HandshakeHandler(), new RequestHandler(messageStore));

        SocketChannel channel = createNiceMock(SocketChannel.class);
        SocketAddress addr = createNiceMock(SocketAddress.class);

        channel.getRemoteAddress();
        expectLastCall().andReturn(addr);

        HandshakeBeginPacket handshakeBeginPacket = new HandshakeBeginPacket();
        handshakeBeginPacket.getFlags().setHandshakeRequest(true).setRegistered(true);
        handshakeBeginPacket.setKey(new PublicKey(new OpenSSLRSAPEM(OpenSSLRSAPEM.INTEGER.valueOf(65537), OpenSSLRSAPEM.INTEGER.randomINTEGER(1024))));

        byte[] serialize = handshakeBeginPacket.getParser().serialize();
        ChannelWriter delegateTo = new ChannelWriter(serialize);

        expect(channel.read(anyObject(ByteBuffer.class))).andDelegateTo(delegateTo);

        Capture<ByteBuffer> objectCapture = EasyMock.newCapture();

        expect(channel.write(and(isA(ByteBuffer.class), capture(objectCapture)))).andReturn(1500);

        replay(channel, addr);

        boolean handleResult = session.handle(channel);

        assertEquals(false, handleResult);
        assertTrue(objectCapture.hasCaptured());

        ByteBuffer objectCaptureValue = objectCapture.getValue();

        PacketBuffer bu = new PacketBuffer(32, 1500).write(objectCaptureValue.array());
        HandshakeBeginPacket parse = (HandshakeBeginPacket) new HandshakeBeginPacket().getParser().parse(bu);


        assertTrue(parse.getFlags().isHandshakeAccepted());
        assertTrue(parse.getFlags().isRegistered());

        assertEquals(34, objectCaptureValue.array().length); // Type(1) + fingerprint(32) + Flgs(1)

        verify(channel, addr);
    }

    @Test
    public void testFullHandleOpenToAcceptHandshakeRegistered() throws Exception, PacketParserException {
        Session session = new Session(communications, options, userStore, new HandshakeHandler(), new RequestHandler(messageStore));

        SocketChannel channel = createNiceMock(SocketChannel.class);
        SocketAddress addr = createNiceMock(SocketAddress.class);

        channel.getRemoteAddress();
        expectLastCall().andReturn(addr);

        HandshakeBeginPacket handshakeBeginPacket = new HandshakeBeginPacket();
        handshakeBeginPacket.getFlags().setHandshakeRequest(true).setRegistered(true).setRequestsReturnService(true);
        handshakeBeginPacket.setKey(new PublicKey(new OpenSSLRSAPEM(OpenSSLRSAPEM.INTEGER.valueOf(65537), OpenSSLRSAPEM.INTEGER.randomINTEGER(1024))));

        byte[] serialize = handshakeBeginPacket.getParser().serialize();
        ChannelWriter delegateTo = new ChannelWriter(serialize);

        expect(channel.read(anyObject(ByteBuffer.class))).andDelegateTo(delegateTo);

        Capture<ByteBuffer> objectCapture = EasyMock.newCapture();

        expect(channel.write(and(isA(ByteBuffer.class), capture(objectCapture)))).andReturn(1500);

        replay(channel, addr);

        boolean handleResult = session.handle(channel);

        assertTrue(objectCapture.hasCaptured());

        ByteBuffer objectCaptureValue = objectCapture.getValue();

        PacketBuffer bu = new PacketBuffer(32, 1500).write(objectCaptureValue.array());
        HandshakeBeginPacket parse = (HandshakeBeginPacket) new HandshakeBeginPacket().getParser().parse(bu);

        assertEquals(false, handleResult);

        assertTrue(parse.getFlags().isHandshakeAccepted());
        assertFalse(parse.getFlags().isRegistered());

        assertArrayEquals(session.getServerKey().getFingerPrint(), parse.getKey().getFingerPrint());

        assertEquals(1 + // Type
                        session.getServerKey().getFingerPrint().length + // Fingerprint
                        1 + // Flags
                        2 + // Length Specifier for next
                        session.getServerKey().getPem().getPublicExponent().getBytes().length + // Public exponent
                        2 + // Length Specifier for next
                        session.getServerKey().getPem().getModulus().getBytes().length, // Modulus,

                objectCaptureValue.array().length);

        verify(channel, addr);
    }

    @Test
    public void testRequestSaveKey() throws Exception {
        Session session = new Session(communications, options, userStore, new HandshakeHandler(), new RequestHandler(messageStore));

        assertTrue(session.isExpecting(null));

        User user1 = userStore.findUser("username2");
        PublicKey newKey = new PublicKey(new OpenSSLRSAPEM(OpenSSLRSAPEM.INTEGER.valueOf(65537), OpenSSLRSAPEM.INTEGER.randomINTEGER(1024)));

        SocketChannel channel = createNiceMock(SocketChannel.class);
        SocketAddress addr = createNiceMock(SocketAddress.class);

        channel.getRemoteAddress();
        expectLastCall().andReturn(addr).anyTimes();

        HandshakeBeginPacket handshakeBeginPacket = new HandshakeBeginPacket();
        handshakeBeginPacket.getFlags().setHandshakeRequest(true).setRegistered(true).setShouldStore(true);
        handshakeBeginPacket.setKey(newKey).setFingerPrint(user1.getKey().getFingerPrint());

        byte[] serialize = handshakeBeginPacket.getParser().serialize();
        ChannelWriter delegateTo = new ChannelWriter(serialize);
        expect(channel.read(anyObject(ByteBuffer.class))).andDelegateTo(delegateTo);
        expect(channel.read(anyObject(ByteBuffer.class))).andDelegateTo(new ChannelWriter(new byte[]{}));
        expect(channel.read(anyObject(ByteBuffer.class))).andDelegateTo(delegateTo);

        Capture<ByteBuffer> objectCapture = EasyMock.newCapture();

        expect(channel.write(and(isA(ByteBuffer.class), capture(objectCapture)))).andReturn(1500);

        replay(channel, addr);

        boolean handleResult = session.handle(channel);
        assertFalse(handleResult);
        assertTrue(session.isExpecting(PACKETS.HANDSHAKE_CHALLENGE));
        assertTrue(objectCapture.hasCaptured());

        ByteBuffer objectCaptureValue = objectCapture.getValue();

        PacketBuffer bu = new PacketBuffer(257, 5000).write(objectCaptureValue.array());
        HandshakeChallengePacket parse = (HandshakeChallengePacket) new HandshakeChallengePacket().getParser().parse(bu);

        assertArrayEquals(session.getChallenge(), parse.getChallenge());
        assertArrayEquals(session.getChallengedKey().getFingerPrint(), user1.getKey().getFingerPrint());

        PrivateKey privateKey = new PrivateKey(new File(ClassLoader.getSystemResource("test.pem").getFile()));
        HandshakeChallengePacket handshakeChallengePacket = new HandshakeChallengePacket(privateKey, parse.getChallenge());

        byte[] challengeResponse = handshakeChallengePacket.getParser().serialize();

        delegateTo.substitute(challengeResponse);

        handleResult = session.handle(channel);

        assertFalse(handleResult);
        assertTrue(session.isExpecting(PACKETS.REQUEST));

        verify(channel, addr);
    }

    @Test
    public void testRequestSaveKeyUnknownUser() throws Exception {
        UserStore userStore = createNiceMock(UserStore.class);

        Session session = new Session(communications, options, userStore, new HandshakeHandler(), new RequestHandler(messageStore));

        PublicKey newKey = new PublicKey(new OpenSSLRSAPEM(OpenSSLRSAPEM.INTEGER.valueOf(65537), OpenSSLRSAPEM.INTEGER.randomINTEGER(1024)));
        PublicKey newKey2 = new PublicKey(new OpenSSLRSAPEM(OpenSSLRSAPEM.INTEGER.valueOf(65537), OpenSSLRSAPEM.INTEGER.randomINTEGER(1024)));

        SocketChannel channel = createNiceMock(SocketChannel.class);
        SocketAddress addr = createNiceMock(SocketAddress.class);

        channel.getRemoteAddress();
        expectLastCall().andReturn(addr).anyTimes();

        HandshakeBeginPacket handshakeBeginPacket = new HandshakeBeginPacket();
        handshakeBeginPacket.getFlags().setHandshakeRequest(true).setRegistered(true).setShouldStore(true);
        handshakeBeginPacket.setKey(newKey2).setFingerPrint(newKey.getFingerPrint());

        ChannelWriter delegateTo = new ChannelWriter(handshakeBeginPacket.getParser().serialize());
        expect(channel.read(anyObject(ByteBuffer.class))).andDelegateTo(delegateTo);

        Capture<ByteBuffer> objectCapture = EasyMock.newCapture();

        expect(channel.write(and(isA(ByteBuffer.class), capture(objectCapture)))).andReturn(1500);

        replay(channel, addr);

        boolean handleResult = session.handle(channel);

        assertTrue(handleResult);
    }

    @Test
    public void testAnonRead() throws Exception {
        Session session = new Session(communications, options, userStore, new HandshakeHandler(), new RequestHandler(messageStore));

        SocketChannel channel = createNiceMock(SocketChannel.class);
        SocketAddress addr = createNiceMock(SocketAddress.class);

        channel.getRemoteAddress();
        expectLastCall().andReturn(addr);

        HandshakeBeginPacket handshakeBeginPacket = new HandshakeBeginPacket();
        handshakeBeginPacket.getFlags().setHandshakeRequest(true).setRequestsReturnService(true);
        handshakeBeginPacket.setKey(new PublicKey(new OpenSSLRSAPEM(OpenSSLRSAPEM.INTEGER.valueOf(65537), OpenSSLRSAPEM.INTEGER.randomINTEGER(1024))));

        byte[] serialize = handshakeBeginPacket.getParser().serialize();
        ChannelWriter delegateTo = new ChannelWriter(serialize);

        expect(channel.read(anyObject(ByteBuffer.class))).andDelegateTo(delegateTo);

        Capture<ByteBuffer> objectCapture = EasyMock.newCapture();

        expect(channel.write(and(isA(ByteBuffer.class), capture(objectCapture)))).andReturn(1500);

        replay(channel, addr);

        boolean handleResult = session.handle(channel);

        assertTrue(objectCapture.hasCaptured());

        ByteBuffer objectCaptureValue = objectCapture.getValue();

        PacketBuffer bu = new PacketBuffer(32, 1500).write(objectCaptureValue.array());
        HandshakeBeginPacket parse = (HandshakeBeginPacket) new HandshakeBeginPacket().getParser().parse(bu);

        assertEquals(false, handleResult);

        assertTrue(parse.getFlags().isHandshakeAccepted());
        assertFalse(parse.getFlags().isRegistered());

        assertArrayEquals(session.getServerKey().getFingerPrint(), parse.getKey().getFingerPrint());

        assertEquals(1 + // Type
                        session.getServerKey().getFingerPrint().length + // Fingerprint
                        1 + // Flags
                        2 + // Length Specifier for next
                        session.getServerKey().getPem().getPublicExponent().getBytes().length + // Public exponent
                        2 + // Length Specifier for next
                        session.getServerKey().getPem().getModulus().getBytes().length, // Modulus,

                objectCaptureValue.array().length);

        verify(channel, addr);
    }

    @Test
    public void testRequestSaveKeyFailedChallenge() throws Exception, PacketParserException {
        Session session = new Session(communications, options, userStore, new HandshakeHandler(), new RequestHandler(messageStore));

        assertTrue(session.isExpecting(null));

        User user1 = userStore.findUser("username2");
        PublicKey newKey = new PublicKey(new OpenSSLRSAPEM(OpenSSLRSAPEM.INTEGER.valueOf(65537), OpenSSLRSAPEM.INTEGER.randomINTEGER(1024)));

        SocketChannel channel = createNiceMock(SocketChannel.class);
        SocketAddress addr = createNiceMock(SocketAddress.class);

        channel.getRemoteAddress();
        expectLastCall().andReturn(addr).anyTimes();

        HandshakeBeginPacket handshakeBeginPacket = new HandshakeBeginPacket();
        handshakeBeginPacket.getFlags().setHandshakeRequest(true).setRegistered(true).setShouldStore(true);
        handshakeBeginPacket.setKey(newKey).setFingerPrint(user1.getKey().getFingerPrint());

        byte[] serialize = handshakeBeginPacket.getParser().serialize();
        ChannelWriter delegateTo = new ChannelWriter(serialize);
        expect(channel.read(anyObject(ByteBuffer.class))).andDelegateTo(delegateTo);
        expect(channel.read(anyObject(ByteBuffer.class))).andDelegateTo(new ChannelWriter(new byte[]{}));
        expect(channel.read(anyObject(ByteBuffer.class))).andDelegateTo(delegateTo);

        Capture<ByteBuffer> objectCapture = EasyMock.newCapture();

        expect(channel.write(and(isA(ByteBuffer.class), capture(objectCapture)))).andReturn(1500);

        replay(channel, addr);

        boolean handleResult = session.handle(channel);
        assertFalse(handleResult);
        assertTrue(session.isExpecting(PACKETS.HANDSHAKE_CHALLENGE));
        assertTrue(objectCapture.hasCaptured());

        ByteBuffer objectCaptureValue = objectCapture.getValue();

        PacketBuffer bu = new PacketBuffer(257, 5000).write(objectCaptureValue.array());
        HandshakeChallengePacket parse = (HandshakeChallengePacket) new HandshakeChallengePacket().getParser().parse(bu);
        parse.setChallenge(new byte[256]);

        assertArrayEquals(session.getChallengedKey().getFingerPrint(), user1.getKey().getFingerPrint());

        PrivateKey privateKey = new PrivateKey(new File(ClassLoader.getSystemResource("test.pem").getFile()));
        HandshakeChallengePacket handshakeChallengePacket = new HandshakeChallengePacket(privateKey, parse.getChallenge());

        byte[] challengeResponse = handshakeChallengePacket.getParser().serialize();

        delegateTo.substitute(challengeResponse);

        handleResult = session.handle(channel);

        assertTrue(handleResult);

        verify(channel, addr);
    }

}