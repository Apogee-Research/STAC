package stac.communications;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * Root class for all handlers
 */
public abstract class Handler {
    /**
     * This is a basic handler which can be overridden, but should work in most if not all reception cases.
     *
     * @param ch     The SocketChannel.
     * @param packetBuffer The packet buffer.
     * @return The HANDLER_STATE that describes the current packet handling state.
     * @throws IOException
     */
    public HANDLER_STATE handle(SocketChannel ch, PacketBuffer packetBuffer) throws IOException {
        ByteBuffer allocate = ByteBuffer.allocate(16384);

        while (ch.read(allocate) > 0) {
            packetBuffer.write(allocate.array(), 0, allocate.position());
            allocate.rewind();
        }

        if (isPacketFormed(packetBuffer)) {
            return HANDLER_STATE.DONE;
        } else if (isPacketStillOK(packetBuffer)) {
            return HANDLER_STATE.WAITING;
        }
        return HANDLER_STATE.FAILED;
    }

    /**
     * This is a basic handler which can be overridden, but should work in most if not all reception cases.
     * It's job is to buffer data from the socket into the session object.
     *
     * @param is           The SocketChannel's InputStream.
     * @param packetBuffer The packet buffer.
     * @return The HANDLER_STATE that describes the current packet handling state.
     * @throws IOException
     */
    public HANDLER_STATE handle(InputStream is, PacketBuffer packetBuffer) throws IOException {
        int read;
        while (!isPacketFormed(packetBuffer) && is.available() > 0 && (read = is.read()) != -1) {
            packetBuffer.write((byte) read);
        }

        if (isPacketFormed(packetBuffer)) {
            return HANDLER_STATE.DONE;
        } else if (isPacketStillOK(packetBuffer)) {
            return HANDLER_STATE.WAITING;
        }
        return HANDLER_STATE.FAILED;
    }

    /**
     * This checks for a complete packet.
     *
     * @param packetBuffer The packet buffer.
     * @return true when the packet is complete and the handler should inform the Session to continue to the next
     * processing phase, whatever that is.
     */
    abstract protected boolean isPacketFormed(PacketBuffer packetBuffer);

    /**
     * This must check the current buffer for errors going forward.
     *
     * @param packetBuffer The buffer the packet is stored in.
     * @return true when the packet contains no errors to state that further reception is OK.
     */
    abstract protected boolean isPacketStillOK(PacketBuffer packetBuffer);

    /**
     * getMaxPacketSize returns the maximum packet size that the buffer should be allocated for.
     *
     * @return The max packet size or -1
     */
    abstract protected int getMaxPacketSize();

    /**
     * getMinPacketSize returns the minimum packet size that the buffer should be allocated for.
     *
     * @return the min packet size or -1;
     */
    abstract protected int getMinPacketSize();

    /**
     * Makes sure the packetBuffer has enough room to read in the next packet.
     */
    public void initPacketBuffer(PacketBuffer packetBuffer) {
        packetBuffer.resize(getMinPacketSize(), getMaxPacketSize());
    }

    /**
     * Runs the packet parser associated with this Handler.
     * @param packetBuffer The read in packet.
     * @param session
     * @return The packet as parsed by the handler.
     */
    public abstract Packet runPacketParser(PacketBuffer packetBuffer, Session session, PACKETS expecting) throws PacketParserException;

    /**
     * The packet is handled with this method and the next connection state is returned by it.
     * @param packetBuffer The packet buffer containing a packet which is ready to be parsed and acted upon.
     * @return The next connection state.
     */
    public abstract CONNECTION_PHASE handlePacket(PacketBuffer packetBuffer, Session session);
}
