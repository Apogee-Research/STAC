package stac.communications.packets;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.annotation.MockNice;
import org.powermock.modules.junit4.PowerMockRunner;
import stac.client.Remote;
import stac.client.Screen;
import stac.communications.Communications;
import stac.communications.PacketBuffer;
import stac.communications.PacketParser;
import stac.crypto.PrivateKey;
import stac.crypto.PublicKey;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.powermock.api.easymock.PowerMock.expectLastCall;
import static org.powermock.api.easymock.PowerMock.replay;

/**
 *
 */
@RunWith(PowerMockRunner.class)
public class RequestPacketParserTest {

    @MockNice
    private Communications communications;

    @MockNice
    private Screen screen;

    @Test
    public void testSerialize() throws Exception {
        PublicKey recieverKey = new PublicKey(new File(ClassLoader.getSystemResource("test.pub").getFile()));
        PrivateKey senderKey = new PrivateKey(new File(ClassLoader.getSystemResource("test.pem").getFile()));

        String senderName = "Frankie";
        String receiverName = "Johnny";
        String message = "Hello There";

        RequestPacket requestPacket = RequestPacket.newMessage(senderKey, recieverKey, senderName, receiverName)
                .setMessage(message);

        requestPacket.getNonce();

        PacketParser parser = requestPacket.getParser();
        assertNotNull(parser);

        byte[] serialize = parser.serialize();

        PacketBuffer packetBuffer = new PacketBuffer(0, 100000);
        packetBuffer.write(serialize);

        communications.getListenerKey();
        expectLastCall().andReturn(senderKey);

        replay(communications);

        RequestPacket parse = new RequestPacket();
        parse.setReceiverKey(recieverKey);
        parse.setCommunications(communications);
        parse.getParser().parse(packetBuffer, new Remote(senderKey));

        assertEquals(message, parse.getMessage());
        assertEquals(senderName, parse.getSenderName());
        assertEquals(receiverName, parse.getReceiverName());
    }
}