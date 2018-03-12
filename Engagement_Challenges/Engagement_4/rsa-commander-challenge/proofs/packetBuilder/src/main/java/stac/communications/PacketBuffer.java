package stac.communications;

/**
 *
 */
public class PacketBuffer {
    private int min;
    private int max;
    private byte[] packetBuffer = null;
    private int packetOffset = 0;
    private boolean reset = false;

    public PacketBuffer() {
        reset = true;
        min = 0;
        max = Short.MAX_VALUE;
        packetBuffer = new byte[512]; // Default to a small packet size to prevent resource consumption.
    }

    public PacketBuffer(int minSize, int maxSize) {
        packetBuffer = new byte[minSize];
        this.max = maxSize;
        this.min = minSize;
    }

    public PacketBuffer write(byte b) {
        if (packetOffset == packetBuffer.length) resize(1);
        packetBuffer[packetOffset++] = b;
        return this;
    }

    public PacketBuffer write(byte[] buffer) {
        if (packetOffset + buffer.length > packetBuffer.length) resize(buffer.length);
        System.arraycopy(buffer, 0, packetBuffer, packetOffset, buffer.length);
        packetOffset += buffer.length;
        return this;
    }

    public PacketBuffer write(byte[] buffer, int offset, int size) {
        if (packetOffset + size > packetBuffer.length) resize(size);
        if (size > buffer.length) size = offset;
        System.arraycopy(buffer, offset, packetBuffer, packetOffset, size);
        packetOffset += size;
        return this;
    }

    private void resize(int add) {
        if (packetBuffer.length + add > max && max > 0) {
            throw new OutOfMemoryError("Packet buffer maximum size exceeded");
        }
        if (add < 512 && packetBuffer.length + 512 <= max)
            add = 512; // Auto expand 512 for excesses smaller that 512 unless there is no more room.
        byte[] temp = new byte[packetBuffer.length + add];
        System.arraycopy(packetBuffer, 0, temp, 0, packetBuffer.length);
        packetBuffer = temp;
    }

    public void resize(int min, int max) {
        if (reset) {
            this.min = min;
            this.max = max;
            if (packetBuffer.length < min) packetBuffer = new byte[min];
            packetOffset = 0;
            reset = false;
        } else {
            if (min > packetBuffer.length) { // Only increase the size of the array.
                resize(min - packetBuffer.length);
            }
        }
    }

    public void reset() {
        this.reset = true;
    }

    public byte[] getBuffer() {
        return packetBuffer;
    }

    public int getOffset() {
        return packetOffset;
    }

    public void destroy() {
        this.reset = true;
        this.packetBuffer = null;
    }

}
