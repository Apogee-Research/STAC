package stac.communications;

/**
 *
 */
abstract public class PacketParser {
    public abstract Packet parse(PacketBuffer packetBuffer) throws PacketParserException;
    public abstract byte[] serialize() throws PacketParserException;
}
