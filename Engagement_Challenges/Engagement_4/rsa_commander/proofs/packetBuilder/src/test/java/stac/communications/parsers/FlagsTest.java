package stac.communications.parsers;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * flg:
 *   1) Registered Public Key. This is used to determine if the fingerprint can be used to look up the key or not.
 *     Enables:
 *       pSize (public exponent size)
 *       Public Exponent
 *       mSize (modulus size / key size)
 *       Public Key Modulus
 *   2) Server Should Store. This is used to determine if an update to a key is being performed.
 *       In this case the Fingerprint still refers to the old key in order to update it by fingerprint.
 *       ** The server will challenge the update by requesting a challenge to be signed and returned **
 *   3) Requests Return Service. This is used to ask for a public key from the other party.
 *       ** The client will challenge this new public key to ensure that the other user can sign things with it **
 *   4) Handshake Request. This is set to request an ongoing communication.
 *   5) Handshake Accepted. This is set to confirm an open ongoing communication.
 *   6) Handshake Stalled. This is set to allow more key passing and verification without a restart.
 *   7) Handshake Failed. This is set to tell the alternate party of a failure state.
 */
public class FlagsTest {

    @Test
    public void testConstruction() throws Exception {
        HandshakeBeginPacket.Flags flags = new HandshakeBeginPacket.Flags();
        assertFalse(flags.isRegistered());
        assertFalse(flags.isShouldStore());
        assertFalse(flags.isRequestsReturnService());
        assertFalse(flags.isHandshakeRequest());
        assertFalse(flags.isHandshakeAccepted());
        assertFalse(flags.isHandshakeStalled());
        assertFalse(flags.isHandshakeFailed());
    }

    @Test
    public void testFlagsParseOutCorrectly() throws Exception {
        HandshakeBeginPacket.Flags flags = new HandshakeBeginPacket.Flags();
        flags.setHandshakeRequest(true);
        flags.setRequestsReturnService(true);

        assertEquals(1 << 2 | 1 << 3, flags.toByte());

        flags.setHandshakeFailed(true);

        assertEquals(1 << 2 | 1 << 3 | 1 << 6, flags.toByte());

        flags.setRegistered(true);
        flags.setHandshakeAccepted(true);
        flags.setHandshakeStalled(true);
        flags.setShouldStore(true);

        assertTrue(flags.isRegistered());
        assertTrue(flags.isShouldStore());
        assertTrue(flags.isRequestsReturnService());
        assertTrue(flags.isHandshakeRequest());
        assertTrue(flags.isHandshakeAccepted());
        assertTrue(flags.isHandshakeStalled());
        assertTrue(flags.isHandshakeFailed());

        assertEquals(1 | 1 << 1 | 1 << 2 | 1 << 3 | 1 << 4 | 1 << 5 | 1 << 6, flags.toByte());

        HandshakeBeginPacket.Flags nextFlags = new HandshakeBeginPacket.Flags();

        nextFlags.fromByte((byte) (1 | 1 << 1 | 1 << 2 | 1 << 3 | 1 << 4 | 1 << 5 | 1 << 6));

        assertEquals(flags, nextFlags);
        assertEquals(flags.hashCode(), nextFlags.hashCode());
    }


    @Test
    public void testFlagsSetTheCorrectBits() throws Exception {
        HandshakeBeginPacket.Flags flags1 = new HandshakeBeginPacket.Flags();
        HandshakeBeginPacket.Flags flags2 = new HandshakeBeginPacket.Flags();

        assertEquals(0, flags1.toByte());
        flags2.fromByte(flags1.toByte());
        assertEquals(flags1, flags2);

        assertFalse(flags2.isRegistered());
        assertFalse(flags2.isShouldStore());
        assertFalse(flags2.isRequestsReturnService());
        assertFalse(flags2.isHandshakeRequest());
        assertFalse(flags2.isHandshakeAccepted());
        assertFalse(flags2.isHandshakeStalled());
        assertFalse(flags2.isHandshakeFailed());

        assertEquals(flags1.hashCode(), flags2.hashCode());

        flags1.setRegistered(true);
        assertEquals(1, flags1.toByte());
        flags2.fromByte(flags1.toByte());
        assertEquals(flags1, flags2);

        assertTrue(flags2.isRegistered());
        assertFalse(flags2.isShouldStore());
        assertFalse(flags2.isRequestsReturnService());
        assertFalse(flags2.isHandshakeRequest());
        assertFalse(flags2.isHandshakeAccepted());
        assertFalse(flags2.isHandshakeStalled());
        assertFalse(flags2.isHandshakeFailed());

        assertEquals(flags1.hashCode(), flags2.hashCode());

        flags1.setRegistered(false);
        flags1.setShouldStore(true);
        assertEquals(2, flags1.toByte());
        flags2.fromByte(flags1.toByte());
        assertEquals(flags1, flags2);

        assertFalse(flags2.isRegistered());
        assertTrue(flags2.isShouldStore());
        assertFalse(flags2.isRequestsReturnService());
        assertFalse(flags2.isHandshakeRequest());
        assertFalse(flags2.isHandshakeAccepted());
        assertFalse(flags2.isHandshakeStalled());
        assertFalse(flags2.isHandshakeFailed());

        assertEquals(flags1.hashCode(), flags2.hashCode());

        flags1.setShouldStore(false);
        flags1.setRequestsReturnService(true);
        assertEquals(4, flags1.toByte());
        flags2.fromByte(flags1.toByte());
        assertEquals(flags1, flags2);

        assertFalse(flags2.isRegistered());
        assertFalse(flags2.isShouldStore());
        assertTrue(flags2.isRequestsReturnService());
        assertFalse(flags2.isHandshakeRequest());
        assertFalse(flags2.isHandshakeAccepted());
        assertFalse(flags2.isHandshakeStalled());
        assertFalse(flags2.isHandshakeFailed());

        assertEquals(flags1.hashCode(), flags2.hashCode());

        flags1.setRequestsReturnService(false);
        flags1.setHandshakeRequest(true);
        assertEquals(8, flags1.toByte());
        flags2.fromByte(flags1.toByte());
        assertEquals(flags1, flags2);

        assertFalse(flags2.isRegistered());
        assertFalse(flags2.isShouldStore());
        assertFalse(flags2.isRequestsReturnService());
        assertTrue(flags2.isHandshakeRequest());
        assertFalse(flags2.isHandshakeAccepted());
        assertFalse(flags2.isHandshakeStalled());
        assertFalse(flags2.isHandshakeFailed());

        assertEquals(flags1.hashCode(), flags2.hashCode());

        flags1.setHandshakeRequest(false);
        flags1.setHandshakeAccepted(true);
        assertEquals(16, flags1.toByte());
        flags2.fromByte(flags1.toByte());
        assertEquals(flags1, flags2);

        assertFalse(flags2.isRegistered());
        assertFalse(flags2.isShouldStore());
        assertFalse(flags2.isRequestsReturnService());
        assertFalse(flags2.isHandshakeRequest());
        assertTrue(flags2.isHandshakeAccepted());
        assertFalse(flags2.isHandshakeStalled());
        assertFalse(flags2.isHandshakeFailed());

        assertEquals(flags1.hashCode(), flags2.hashCode());

        flags1.setHandshakeAccepted(false);
        flags1.setHandshakeStalled(true);
        assertEquals(32, flags1.toByte());
        flags2.fromByte(flags1.toByte());
        assertEquals(flags1, flags2);

        assertFalse(flags2.isRegistered());
        assertFalse(flags2.isShouldStore());
        assertFalse(flags2.isRequestsReturnService());
        assertFalse(flags2.isHandshakeRequest());
        assertFalse(flags2.isHandshakeAccepted());
        assertTrue(flags2.isHandshakeStalled());
        assertFalse(flags2.isHandshakeFailed());

        assertEquals(flags1.hashCode(), flags2.hashCode());

        flags1.setHandshakeStalled(false);
        flags1.setHandshakeFailed(true);
        assertEquals(64, flags1.toByte());
        flags2.fromByte(flags1.toByte());
        assertEquals(flags1, flags2);

        assertFalse(flags2.isRegistered());
        assertFalse(flags2.isShouldStore());
        assertFalse(flags2.isRequestsReturnService());
        assertFalse(flags2.isHandshakeRequest());
        assertFalse(flags2.isHandshakeAccepted());
        assertFalse(flags2.isHandshakeStalled());
        assertTrue(flags2.isHandshakeFailed());

        assertEquals(flags1.hashCode(), flags2.hashCode());
    }


}