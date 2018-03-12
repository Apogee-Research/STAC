package stac.util;

import org.junit.Assert;
import org.junit.Test;

import java.nio.charset.Charset;
import java.util.Scanner;

/**
 *
 */
public class HexTest {
    @Test
    public void testEightAs() throws Exception {
        final byte[] EightA = "AAAAAAAA".getBytes(Charset.defaultCharset());
        String s = Hex.bytesToHex(EightA);
        Assert.assertEquals("41 41 41 41 41 41 41 41", s);
    }

    @Test
    public void testAllValues() throws Exception {
        final byte[] all = new byte[256];
        final char[] val = {'0', '1', '2', '3', '4', '5', '6', '7',
                            '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
        for (int i = 0; i < 256; i++) {
            all[i] = (byte) i;
        }

        String s = Hex.bytesToHex(all);

        int countSpace = 0;
        for (int i = 0; i < s.length(); i++) {
            countSpace += (s.charAt(i) == ' ' ) ? 1 : 0;
        }
        Assert.assertEquals(255, countSpace);
        Scanner scanner = new Scanner(s);

        for (char aVal : val) {
            for (char aVal1 : val) {
                Assert.assertEquals(String.format("%c%c", aVal, aVal1), scanner.next());
            }
        }

    }

}