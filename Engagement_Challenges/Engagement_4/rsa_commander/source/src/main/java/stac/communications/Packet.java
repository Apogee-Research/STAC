package stac.communications;

/**
 * Base class of all packets. This class ensures that packets have parsers.
 */
abstract public class Packet {
    abstract public PacketParser getParser();
}
