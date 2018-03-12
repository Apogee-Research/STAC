package stac.communications.packets;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import stac.communications.PACKETS;
import stac.communications.PacketBuffer;
import stac.communications.PacketParser;
import stac.crypto.PublicKey;
import stac.parser.OpenSSLRSAPEM;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 *
 */
public class HandshakePacketParserTest {
    @Test
    public void testParserCanBeAcquired() throws Exception {
        PacketParser parser = new HandshakePacket().getParser();
        assertNotNull(parser);
    }

    @Test
    public void testConstruction() throws Exception {
        PacketParser parser = new HandshakePacket().getParser();
        HandshakePacket.Flags flags = new HandshakePacket.Flags();
        flags.setRegistered(true);
        flags.setHandshakeRequest(true);
        flags.setRequestsReturnService(true);

        byte[] buffer = new byte[] {
                (byte) PACKETS.HANDSHAKE_OPEN.ordinal(), 0,2,4,8,16,32,64,2,4,8,16,32,64,2,4,8,16,32,64,2,4,8,16,32,64,2,4,8,16,32,64,0, flags.toByte()
        };

        HandshakePacket packet = (HandshakePacket) parser.parse(new PacketBuffer().write(buffer), null);

        assertArrayEquals(new byte[]{0,2,4,8,16,32,64,2,4,8,16,32,64,2,4,8,16,32,64,2,4,8,16,32,64,2,4,8,16,32,64,0}, packet.getKey().getFingerPrint());

        assertTrue(packet.getFlags().isRegistered());
        assertTrue(packet.getFlags().isHandshakeRequest());
        assertTrue(packet.getFlags().isRequestsReturnService());

    }

    @Test
    public void testSerialize() throws Exception {
        PacketParser parser = new HandshakePacket().getParser();
        HandshakePacket.Flags flags = new HandshakePacket.Flags();
        flags.setRegistered(true);
        flags.setHandshakeRequest(true);
        flags.setRequestsReturnService(true);

        byte[] buffer = new byte[] {
                (byte) PACKETS.HANDSHAKE_OPEN.ordinal(), 0,2,4,8,16,32,64,2,4,8,16,32,64,2,4,8,16,32,64,2,4,8,16,32,64,2,4,8,16,32,64,0, flags.toByte()
        };

        HandshakePacket packet = (HandshakePacket) parser.parse(new PacketBuffer().write(buffer), null);

        byte[] bytes = packet.getParser().serialize();

        assertArrayEquals(buffer, bytes);
    }

    @Test
    public void testNewNonRegistered() throws Exception {
        HandshakePacket packet = new HandshakePacket();
        packet.getFlags().setHandshakeRequest(true);
        packet.setKey(new PublicKey(new OpenSSLRSAPEM(OpenSSLRSAPEM.INTEGER.randomINTEGER(4), OpenSSLRSAPEM.INTEGER.randomINTEGER(64))));

        byte[] serialized = packet.getParser().serialize();

        HandshakePacket parse = (HandshakePacket) new HandshakePacket().getParser().parse(new PacketBuffer().write(serialized), null);

        assertTrue(parse.getFlags().isHandshakeRequest());

        OpenSSLRSAPEM pem = parse.getKey().getPem();
        assertNotNull(pem);
    }

    @Rule
    public ExpectedException expect = ExpectedException.none();

    @Test
    public void testConstructionInvalidOrd() throws Exception {
        PacketParser parser = new HandshakePacket().getParser();
        HandshakePacket.Flags flags = new HandshakePacket.Flags();
        flags.setRegistered(true);
        flags.setHandshakeRequest(true);
        flags.setRequestsReturnService(true);

        byte[] buffer = new byte[] {
                (byte) (PACKETS.HANDSHAKE_OPEN.ordinal() + 16), 0,2,4,8,16,32,64,2,4,8,16,32,64,2,4,8,16,32,64,2,4,8,16,32,64,2,4,8,16,32,64,0, flags.toByte()
        };

        expect.expectMessage("Malformed Handshake Packet has incorrect packet type/version.");

        parser.parse(new PacketBuffer().write(buffer), null);
    }
}