package stac.communications;

import org.junit.Before;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 *
 */
public class PacketBufferTest {

    private Random random = new Random();
    private byte[] buffer = new byte[64];

    @Before
    public void setUp() throws Exception {
        random.nextBytes(buffer);
    }

    @Test
    public void testWriteByteArray() throws Exception {
        PacketBuffer packetBuffer = new PacketBuffer(buffer.length, buffer.length);

        packetBuffer.write(buffer);

        assertArrayEquals(buffer, packetBuffer.getBuffer());
        assertEquals(buffer.length, packetBuffer.getOffset());
    }

    @Test
    public void testWriteBytes() throws Exception {
        PacketBuffer packetBuffer = new PacketBuffer(buffer.length, buffer.length);

        for (byte b : buffer) {
            packetBuffer.write(b);
        }

        assertArrayEquals(buffer, packetBuffer.getBuffer());
        assertEquals(buffer.length, packetBuffer.getOffset());
    }

    @Test
    public void testWriteBytesTwoByTwo() throws Exception {
        PacketBuffer packetBuffer = new PacketBuffer(buffer.length, buffer.length);

        byte[] pair = new byte[2];

        for (int i = 0; i < buffer.length; i += 2) {
            pair[0] = buffer[i];
            pair[1] = buffer[i+1];
            packetBuffer.write(pair, 0, 2);
        }

        assertArrayEquals(buffer, packetBuffer.getBuffer());
        assertEquals(buffer.length, packetBuffer.getOffset());
    }

    @Test
    public void testWriteBytesTwoByTwoOffset() throws Exception {
        PacketBuffer packetBuffer = new PacketBuffer(buffer.length, buffer.length);

        byte[] pair = new byte[4];

        for (int i = 0; i < buffer.length; i += 2) {
            pair[1] = buffer[i];
            pair[2] = buffer[i+1];
            packetBuffer.write(pair, 1, 2);
        }

        assertArrayEqualsIgnoreLength(buffer, packetBuffer.getBuffer(), packetBuffer.getOffset());
    }

    @Test
    public void testResizeAndReset() throws Exception {
        PacketBuffer packetBuffer = new PacketBuffer(buffer.length, buffer.length);

        packetBuffer.write(buffer);

        assertArrayEqualsIgnoreLength(buffer, packetBuffer.getBuffer(), packetBuffer.getOffset());

        packetBuffer.resize(64, 64);

        assertArrayEqualsIgnoreLength(buffer, packetBuffer.getBuffer(), packetBuffer.getOffset());

        packetBuffer.resize(64, 1024);

        assertArrayEqualsIgnoreLength(buffer, packetBuffer.getBuffer(), packetBuffer.getOffset());

        packetBuffer.reset();
        packetBuffer.resize(40,40);

        assertArrayEqualsIgnoreLength(new byte[]{0, 0}, packetBuffer.getBuffer(), packetBuffer.getOffset());

    }

    void assertArrayEqualsIgnoreLength(byte[] a, byte[] b, int size) {
        for (int i = 0; i < size; i++) {
            assertEquals(a[i], b[i]);
        }
    }
}