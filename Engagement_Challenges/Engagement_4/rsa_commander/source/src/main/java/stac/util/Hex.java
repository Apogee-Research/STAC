package stac.util;

/**
 * Should be self-explanatory
 */
public class Hex {
    private final static char[] hexArray = "0123456789ABCDEF".toCharArray();

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 3];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 3] = hexArray[v >>> 4];
            hexChars[j * 3 + 1] = hexArray[v & 0x0F];
            hexChars[j * 3 + 2] = ' ';
        }
        return new String(hexChars, 0, hexChars.length - 1);
    }

    public static String bytesToHex(byte[] bytes, int offset, int length) {
        if (offset + length < bytes.length) {
            char[] hexChars = new char[length * 3];
            for (int j = offset; j < length; j++) {
                int v = bytes[j] & 0xFF;
                hexChars[j * 3] = hexArray[v >>> 4];
                hexChars[j * 3 + 1] = hexArray[v & 0x0F];
                hexChars[j * 3 + 2] = ' ';
            }
            return new String(hexChars, 0, hexChars.length - 1);
        }
        throw new ArrayIndexOutOfBoundsException("Offset + Length exceeds array bounds [bytesToHex]");
    }
}
