package stac.communications;

import stac.client.Remote;

/**
 * This is the base class of all parsers and ensures that each parser implements
 * the two major parts (parsing and serializing data)
 */
abstract public class PacketParser {
    public abstract Packet parse(PacketBuffer packetBuffer, Remote remote) throws PacketParserException;
    public abstract byte[] serialize() throws PacketParserException;
}
