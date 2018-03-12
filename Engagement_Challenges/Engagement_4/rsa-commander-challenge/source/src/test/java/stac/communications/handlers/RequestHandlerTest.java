package stac.communications.handlers;

import org.easymock.Capture;
import org.easymock.EasyMock;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.annotation.MockNice;
import org.powermock.modules.junit4.PowerMockRunner;
import stac.client.Screen;
import stac.communications.Communications;
import stac.communications.PacketBuffer;
import stac.communications.Session;
import stac.communications.packets.HandshakePacket;
import stac.communications.packets.RequestPacket;
import stac.crypto.PrivateKey;
import stac.crypto.PublicKey;
import stac.parser.CommandLine;

import javax.util.ChannelWriter;
import javax.util.ModifiableByteArrayInputStream;
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
import static org.powermock.api.easymock.PowerMock.expectLastCall;
import static org.powermock.api.easymock.PowerMock.replay;
import static org.powermock.api.easymock.PowerMock.verify;

/**
 *
 */
@RunWith(PowerMockRunner.class)
public class RequestHandlerTest {

    private Communications communications;
    private CommandLine.Options options;
    private Session session;
    private ModifiableByteArrayInputStream txStream = new ModifiableByteArrayInputStream();

    @MockNice
    private SocketChannel channel;
    @MockNice
    private SocketAddress addr;
    @MockNice
    private Socket socket;

    @MockNice
    private Screen screen;

    @Test
    public void testBasicHandle() throws Exception {
        PublicKey pubkey = new PublicKey(new File(ClassLoader.getSystemResource("test.pub").getFile()));
        PrivateKey privkey = new PrivateKey(new File(ClassLoader.getSystemResource("test.pem").getFile()));
        pubkey.getPem().getPublicExponent().getInternalBig();
        privkey.getPem().getPublicExponent().getInternalBig();

        CommandLine commandLine = new CommandLine("");
        String userStore = ClassLoader.getSystemResource("userStore").getPath();
        String keyStore = ClassLoader.getSystemResource("test.pem").getPath();
        keyStore = keyStore.substring(0, keyStore.length() - "test.pem".length());
        commandLine.newOption().longOption("user-store").hasValue(true, userStore).done();
        commandLine.newOption().longOption("key-store").hasValue(true, keyStore).done();
        options = commandLine.parse(new String[]{});

        communications = new Communications(options, screen);
        communications.setListenerKey(privkey);

        session = new Session(communications, options, new HandshakeHandler(), new RequestHandler(screen));

        channel.getRemoteAddress();
        expectLastCall().andReturn(addr).anyTimes();

        HandshakePacket handshakePacket = new HandshakePacket();
        handshakePacket.getFlags().setHandshakeRequest(true).setRegistered(false).setRequestsReturnService(true);
        handshakePacket.setKey(pubkey);

        ChannelWriter delegateTo = new ChannelWriter(handshakePacket.getParser().serialize());
        expect(channel.read(anyObject(ByteBuffer.class))).andDelegateTo(delegateTo);

        Capture<ByteBuffer> objectCapture = EasyMock.newCapture();

        expect(channel.write(and(isA(ByteBuffer.class), capture(objectCapture)))).andReturn(1500);

        replay(channel, addr, socket);

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

        PrivateKey privateKey = new PrivateKey(new File(ClassLoader.getSystemResource("test.pem").getFile()));
        PublicKey publicKey = new PublicKey(new File(ClassLoader.getSystemResource("test.pub").getFile()));

        RequestPacket requestPacket = RequestPacket.newRelay(privateKey, publicKey);
        requestPacket.setMessage("Hey there");
        requestPacket.setSenderName("Unknown Remote");
        requestPacket.setReceiverName("Unknown Remote");
        requestPacket.setCommunications(communications);

        byte[] serialized = requestPacket.getParser().serialize();

        txStream.substitute(serialized);

        boolean handle = session.handle(channel);

        assertFalse(handle);

        verify(channel, addr, socket);
    }

    @Test
    public void testHandleClient() throws Exception {
        PublicKey pubkey = new PublicKey(new File(ClassLoader.getSystemResource("test.pub").getFile()));
        PrivateKey privkey = new PrivateKey(new File(ClassLoader.getSystemResource("test.pem").getFile()));
        pubkey.getPem().getPublicExponent().getInternalBig();
        privkey.getPem().getPublicExponent().getInternalBig();

        CommandLine commandLine = new CommandLine("");
        String userStore = ClassLoader.getSystemResource("userStore").getPath();
        String keyStore = ClassLoader.getSystemResource("test.pem").getPath();
        keyStore = keyStore.substring(0, keyStore.length() - "test.pem".length());
        commandLine.newOption().longOption("user-store").hasValue(true, userStore).done();
        commandLine.newOption().longOption("key-store").hasValue(true, keyStore).done();
        options = commandLine.parse(new String[]{});

        communications = new Communications(options, screen);
        communications.setListenerKey(privkey);

        session = new Session(communications, options, new HandshakeHandler(), new RequestHandler(screen));

        channel.getRemoteAddress();
        expectLastCall().andReturn(addr).anyTimes();

        channel.socket();
        expectLastCall().andReturn(socket).anyTimes();

        HandshakePacket handshakePacket = new HandshakePacket();
        handshakePacket.getFlags().setHandshakeRequest(true).setRegistered(false).setRequestsReturnService(true);
        handshakePacket.setKey(pubkey);

        ChannelWriter sere = new ChannelWriter(handshakePacket.getParser().serialize());
        expect(channel.read(anyObject(ByteBuffer.class))).andDelegateTo(sere);

        Capture<ByteBuffer> objectCapture = EasyMock.newCapture();

        expect(channel.write(and(isA(ByteBuffer.class), capture(objectCapture)))).andReturn(1500);

        expect(screen.postMessage((RequestPacket) anyObject())).andReturn("This is a reply");

        replay(channel, addr, socket);

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

        PrivateKey privateKey = new PrivateKey(new File(ClassLoader.getSystemResource("test.pem").getFile()));
        PublicKey publicKey = new PublicKey(new File(ClassLoader.getSystemResource("test.pub").getFile()));

        RequestPacket requestPacket = RequestPacket.newMessage(privateKey, publicKey, "Unknown Remote", "Unknown Remote");
        requestPacket.setMessage("Hey there");
        requestPacket.setCommunications(communications);

        byte[] serialized = requestPacket.getParser().serialize();

        sere.substitute(serialized);

        boolean handle = session.handle(channel);

        assertFalse(handle);

        verify(channel, addr, socket);
    }
}