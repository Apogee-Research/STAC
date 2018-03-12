package stac.communications.parsers;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import stac.communications.PACKETS;
import stac.communications.PacketBuffer;
import stac.communications.PacketParser;
import stac.communications.PacketParserException;
import stac.crypto.PublicKey;
import stac.parser.OpenSSLRSAPEM;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 *
 */
public class HandshakeBeginPacketParserTest {
    @Test
    public void testParserCanBeAcquired() throws Exception {
        PacketParser parser = new HandshakeBeginPacket().getParser();
        assertNotNull(parser);
    }

    @Test
    public void testConstruction() throws Exception, PacketParserException {
        PacketParser parser = new HandshakeBeginPacket().getParser();
        HandshakeBeginPacket.Flags flags = new HandshakeBeginPacket.Flags();
        flags.setRegistered(true);
        flags.setHandshakeRequest(true);
        flags.setRequestsReturnService(true);

        byte[] buffer = new byte[] {
                (byte) PACKETS.HANDSHAKE_OPEN.ordinal(), 0,2,4,8,16,32,64,2,4,8,16,32,64,2,4,8,16,32,64,2,4,8,16,32,64,2,4,8,16,32,64,0, flags.toByte()
        };

        HandshakeBeginPacket packet = (HandshakeBeginPacket) parser.parse(new PacketBuffer().write(buffer));

        assertArrayEquals(new byte[]{0,2,4,8,16,32,64,2,4,8,16,32,64,2,4,8,16,32,64,2,4,8,16,32,64,2,4,8,16,32,64,0}, packet.getKey().getFingerPrint());

        assertTrue(packet.getFlags().isRegistered());
        assertTrue(packet.getFlags().isHandshakeRequest());
        assertTrue(packet.getFlags().isRequestsReturnService());

    }

    @Test
    public void testSerialize() throws Exception, PacketParserException {
        PacketParser parser = new HandshakeBeginPacket().getParser();
        HandshakeBeginPacket.Flags flags = new HandshakeBeginPacket.Flags();
        flags.setRegistered(true);
        flags.setHandshakeRequest(true);
        flags.setRequestsReturnService(true);

        byte[] buffer = new byte[] {
                (byte) PACKETS.HANDSHAKE_OPEN.ordinal(), 0,2,4,8,16,32,64,2,4,8,16,32,64,2,4,8,16,32,64,2,4,8,16,32,64,2,4,8,16,32,64,0, flags.toByte()
        };

        HandshakeBeginPacket packet = (HandshakeBeginPacket) parser.parse(new PacketBuffer().write(buffer));

        byte[] bytes = packet.getParser().serialize();

        assertArrayEquals(buffer, bytes);
    }

    @Test
    public void testNewNonRegistered() throws Exception, PacketParserException {
        HandshakeBeginPacket packet = new HandshakeBeginPacket();
        packet.getFlags().setHandshakeRequest(true);
        packet.setKey(new PublicKey(new OpenSSLRSAPEM(OpenSSLRSAPEM.INTEGER.randomInt(), OpenSSLRSAPEM.INTEGER.randomINTEGER(256))));

        byte[] serialized = packet.getParser().serialize();

        HandshakeBeginPacket parse = (HandshakeBeginPacket) new HandshakeBeginPacket().getParser().parse(new PacketBuffer().write(serialized));

        assertTrue(parse.getFlags().isHandshakeRequest());

        OpenSSLRSAPEM pem = parse.getKey().getPem();
        assertNotNull(pem);
    }

    @Rule
    public ExpectedException expect = ExpectedException.none();

    @Test
    public void testConstructionInvalidOrd() throws Exception, PacketParserException {
        PacketParser parser = new HandshakeBeginPacket().getParser();
        HandshakeBeginPacket.Flags flags = new HandshakeBeginPacket.Flags();
        flags.setRegistered(true);
        flags.setHandshakeRequest(true);
        flags.setRequestsReturnService(true);

        byte[] buffer = new byte[] {
                (byte) (PACKETS.HANDSHAKE_OPEN.ordinal() + 16), 0,2,4,8,16,32,64,2,4,8,16,32,64,2,4,8,16,32,64,2,4,8,16,32,64,2,4,8,16,32,64,0, flags.toByte()
        };

        expect.expectMessage("Malformed Handshake Packet has incorrect packet type/version.");

        parser.parse(new PacketBuffer().write(buffer));
    }
}