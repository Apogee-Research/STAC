package stac.communications.handlers;

import org.easymock.Capture;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.annotation.MockNice;
import org.powermock.modules.junit4.PowerMockRunner;
import stac.client.Screen;
import stac.communications.CONNECTION_PHASE;
import stac.communications.Communications;
import stac.communications.PACKETS;
import stac.communications.Packet;
import stac.communications.PacketBuffer;
import stac.communications.Session;
import stac.communications.packets.HandshakePacket;
import stac.crypto.PrivateKey;
import stac.crypto.PublicKey;
import stac.parser.CommandLine;
import stac.parser.OpenSSLRSAPEM;

import javax.util.ChannelWriter;
import java.io.ByteArrayInputStream;
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

    @MockNice
    private Screen screen;

    @Before
    public void setUp() throws Exception {
        CommandLine commandLine = new CommandLine("");
        String userStore = ClassLoader.getSystemResource("userStore").getPath();
        String keyStore = ClassLoader.getSystemResource("test.pem").getPath();
        keyStore = keyStore.substring(0, keyStore.length() - "test.pem".length());
        commandLine.newOption().longOption("user-store").hasValue(true, userStore).done();
        commandLine.newOption().longOption("key-store").hasValue(true, keyStore).done();
        options = commandLine.parse(new String[]{});

        communications = new Communications(options, screen);
        PrivateKey listenerKey = new PrivateKey(new OpenSSLRSAPEM(OpenSSLRSAPEM.INTEGER.valueOf(65537), OpenSSLRSAPEM.INTEGER.randomINTEGER(64), OpenSSLRSAPEM.INTEGER.randomINTEGER(32)));
        communications.setListenerKey(listenerKey);
        handler = (HandshakeHandler) HandshakeHandler.getInstance();
    }

    @After
    public void tearDown() throws Exception {
        handler = null;
        communications = null;
        options = null;
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
        Session session = new Session(communications, options, new HandshakeHandler(), new RequestHandler(screen));
        session.setExpecting(PACKETS.HANDSHAKE_CHALLENGE);

        HandshakePacket handshakePacket = new HandshakePacket();
        handshakePacket.getFlags().setHandshakeRequest(true).setRegistered(true);
        PublicKey key = new PublicKey();
        key.setFingerPrint(new byte[32]);
        handshakePacket.setKey(key);

        PacketBuffer packetBuffer = new PacketBuffer();

        handler.initPacketBuffer(packetBuffer);

        packetBuffer.write(handshakePacket.getParser().serialize());

        assertEquals(CONNECTION_PHASE.FAILED, handler.handlePacket(packetBuffer, session));
    }

    @Test
    public void testAcceptHandshake() throws Exception {
        Session session = new Session(communications, options, new HandshakeHandler(), new RequestHandler(screen));

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

        HandshakePacket handshakePacket = new HandshakePacket();
        handshakePacket.getFlags().setHandshakeRequest(true).setRegistered(false);
        handshakePacket.setKey(new PublicKey(new OpenSSLRSAPEM(OpenSSLRSAPEM.INTEGER.valueOf(65537), OpenSSLRSAPEM.INTEGER.randomINTEGER(64))));

        PacketBuffer packetBuffer = new PacketBuffer();

        handler.initPacketBuffer(packetBuffer);

        packetBuffer.write(handshakePacket.getParser().serialize());

        assertEquals(CONNECTION_PHASE.HANDSHAKE_ACCEPTED, handler.handlePacket(packetBuffer, session));
    }

    @Test
    public void testFullHandleOpenToAcceptHandshake() throws Exception {
        Session session = new Session(communications, options, new HandshakeHandler(), new RequestHandler(screen));

        SocketChannel channel = createNiceMock(SocketChannel.class);
        SocketAddress addr = createNiceMock(SocketAddress.class);

        channel.getRemoteAddress();
        expectLastCall().andReturn(addr);

        HandshakePacket handshakePacket = new HandshakePacket();
        handshakePacket.getFlags().setHandshakeRequest(true).setRegistered(false);
        handshakePacket.setKey(new PublicKey(new OpenSSLRSAPEM(OpenSSLRSAPEM.INTEGER.valueOf(65537), OpenSSLRSAPEM.INTEGER.randomINTEGER(64))));

        byte[] serialize = handshakePacket.getParser().serialize();
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
        HandshakePacket parse = (HandshakePacket) new HandshakePacket().getParser().parse(bu, null);


        assertTrue(parse.getFlags().isHandshakeAccepted());
        assertFalse(parse.getFlags().isRegistered());

        assertEquals(105, objectCaptureValue.array().length); // Type(1) + fingerprint(32) + Flgs(1)

        verify(channel, addr);
    }

    @Test
    public void testAnonRead() throws Exception {
        Session session = new Session(communications, options, new HandshakeHandler(), new RequestHandler(screen));

        SocketChannel channel = createNiceMock(SocketChannel.class);
        SocketAddress addr = createNiceMock(SocketAddress.class);

        channel.getRemoteAddress();
        expectLastCall().andReturn(addr);

        HandshakePacket handshakePacket = new HandshakePacket();
        handshakePacket.getFlags().setHandshakeRequest(true).setRequestsReturnService(true);
        handshakePacket.setKey(new PublicKey(new OpenSSLRSAPEM(OpenSSLRSAPEM.INTEGER.valueOf(65537), OpenSSLRSAPEM.INTEGER.randomINTEGER(64))));

        byte[] serialize = handshakePacket.getParser().serialize();
        ChannelWriter delegateTo = new ChannelWriter(serialize);

        expect(channel.read(anyObject(ByteBuffer.class))).andDelegateTo(delegateTo);

        Capture<ByteBuffer> objectCapture = EasyMock.newCapture();

        expect(channel.write(and(isA(ByteBuffer.class), capture(objectCapture)))).andReturn(1500);

        replay(channel, addr);

        boolean handleResult = session.handle(channel);

        assertTrue(objectCapture.hasCaptured());

        ByteBuffer objectCaptureValue = objectCapture.getValue();

        PacketBuffer bu = new PacketBuffer(32, 1500).write(objectCaptureValue.array());
        HandshakePacket parse = (HandshakePacket) new HandshakePacket().getParser().parse(bu, null);

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

}