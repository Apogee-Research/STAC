package stac.parser;

import org.junit.Assert;
import org.junit.Test;

import java.math.BigInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static stac.parser.OpenSSLRSAPEM.INTEGER.INT_MAX;
import static stac.parser.OpenSSLRSAPEM.INTEGER.INT_MIN;

/**
 *
 */
public class INTEGERTest {
    @Test
    public void testAdd() throws Exception {
        assertEquals(0, OpenSSLRSAPEM.INTEGER.valueOf(3).compareTo(OpenSSLRSAPEM.INTEGER.valueOf(1).add(OpenSSLRSAPEM.INTEGER.valueOf(2))));
        assertEquals(0, OpenSSLRSAPEM.INTEGER.valueOf(3).compareTo(OpenSSLRSAPEM.INTEGER.valueOf(1).add(2)));
        OpenSSLRSAPEM.INTEGER int_max = new OpenSSLRSAPEM.INTEGER(INT_MAX);
        int_max.add(OpenSSLRSAPEM.INTEGER.randomInt().modPow(OpenSSLRSAPEM.INTEGER.valueOf(1), new OpenSSLRSAPEM.INTEGER(INT_MAX)));
        assertEquals(-1, new OpenSSLRSAPEM.INTEGER(INT_MAX).compareTo(int_max));
    }


    @Test
    public void testSub() throws Exception {
        assertEquals(0, OpenSSLRSAPEM.INTEGER.valueOf(-1).compareTo(OpenSSLRSAPEM.INTEGER.valueOf(1).sub(OpenSSLRSAPEM.INTEGER.valueOf(2))));
        assertEquals(0, OpenSSLRSAPEM.INTEGER.valueOf(-1).compareTo(OpenSSLRSAPEM.INTEGER.valueOf(1).sub(2)));
        OpenSSLRSAPEM.INTEGER int_min = new OpenSSLRSAPEM.INTEGER(INT_MIN);
        int_min.sub(OpenSSLRSAPEM.INTEGER.randomInt().modPow(OpenSSLRSAPEM.INTEGER.valueOf(1), new OpenSSLRSAPEM.INTEGER(INT_MAX)));
        assertEquals(1, new OpenSSLRSAPEM.INTEGER(INT_MIN).compareTo(int_min));
    }

    @Test
    public void testGetBytes() throws Exception {
        Assert.assertArrayEquals(new byte[]{5}, OpenSSLRSAPEM.INTEGER.valueOf(5).getBytes());
        Assert.assertArrayEquals(new byte[]{1, 1}, OpenSSLRSAPEM.INTEGER.valueOf(257).getBytes());
        Assert.assertArrayEquals(new byte[]{127, -1, -1, -1}, new OpenSSLRSAPEM.INTEGER(INT_MAX).getBytes());
        Assert.assertArrayEquals(new byte[]{1, 127, -1, -1, -3}, new OpenSSLRSAPEM.INTEGER(INT_MAX).add(new OpenSSLRSAPEM.INTEGER(INT_MAX)).add(new OpenSSLRSAPEM.INTEGER(INT_MAX)).getBytes());
        Assert.assertArrayEquals(new byte[]{-1, 127, -1, -1, -1}, new OpenSSLRSAPEM.INTEGER(INT_MAX).add(new OpenSSLRSAPEM.INTEGER(INT_MIN)).add(new OpenSSLRSAPEM.INTEGER(INT_MIN)).getBytes());
        Assert.assertArrayEquals(new byte[]{-24, -73, -119, 24, 0}, OpenSSLRSAPEM.INTEGER.valueOf("-100000000000").getBytes());
    }

    @Test
    public void testGetBytesExt() throws Exception {
        Assert.assertArrayEquals(new byte[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 5}, OpenSSLRSAPEM.INTEGER.valueOf(5).getBytes(16));
        Assert.assertArrayEquals(new byte[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1}, OpenSSLRSAPEM.INTEGER.valueOf(257).getBytes(16));
        Assert.assertArrayEquals(new byte[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 127, -1, -1, -1}, new OpenSSLRSAPEM.INTEGER(INT_MAX).getBytes(16));
        Assert.assertArrayEquals(new byte[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 127, -1, -1, -3}, new OpenSSLRSAPEM.INTEGER(INT_MAX).add(new OpenSSLRSAPEM.INTEGER(INT_MAX)).add(new OpenSSLRSAPEM.INTEGER(INT_MAX)).getBytes(16));
        Assert.assertArrayEquals(new byte[]{-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -24, -73, -119, 24, 0}, OpenSSLRSAPEM.INTEGER.valueOf("-100000000000").getBytes(16));
        Assert.assertArrayEquals(new byte[]{-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 127, -1, -1, -1}, new OpenSSLRSAPEM.INTEGER(INT_MAX).add(new OpenSSLRSAPEM.INTEGER(INT_MIN)).add(new OpenSSLRSAPEM.INTEGER(INT_MIN)).getBytes(16));

        for (int j = 8; j < 1024; j *= 2) {
            for (int i = 0; i < 1000; i++) {
                compareRep(OpenSSLRSAPEM.INTEGER.randomInt().toString(), j);
            }
        }
    }

    private void compareRep(String s, int size) {
        OpenSSLRSAPEM.INTEGER rep1000 = OpenSSLRSAPEM.INTEGER.valueOf(OpenSSLRSAPEM.INTEGER.valueOf(s).getBytes(size));
        assertTrue(rep1000.compareTo(OpenSSLRSAPEM.INTEGER.valueOf(s)) == 0);
    }

    @Test
    public void testCompareTo() throws Exception {
        assertEquals(0, OpenSSLRSAPEM.INTEGER.valueOf(1).compareTo(OpenSSLRSAPEM.INTEGER.valueOf(1)));
        assertEquals(0, OpenSSLRSAPEM.INTEGER.valueOf(1).compareTo(1));
        assertEquals(0, OpenSSLRSAPEM.INTEGER.valueOf(1).compareTo(new BigInteger("1")));

        assertTrue(new OpenSSLRSAPEM.INTEGER(INT_MAX).compareTo(new OpenSSLRSAPEM.INTEGER(INT_MAX)) == 0);
        assertTrue(new OpenSSLRSAPEM.INTEGER(INT_MIN).compareTo(new OpenSSLRSAPEM.INTEGER(INT_MAX)) == -1);
        assertTrue(new OpenSSLRSAPEM.INTEGER(INT_MAX).compareTo(new OpenSSLRSAPEM.INTEGER(INT_MIN)) == 1);

        assertTrue(new OpenSSLRSAPEM.INTEGER(INT_MAX).compareTo(new BigInteger(String.valueOf(Integer.MAX_VALUE))) == 0);
        assertTrue(new OpenSSLRSAPEM.INTEGER(INT_MIN).compareTo(new BigInteger(String.valueOf(Integer.MAX_VALUE))) < 0);
        assertTrue(new OpenSSLRSAPEM.INTEGER(INT_MAX).compareTo(new BigInteger(String.valueOf(Integer.MIN_VALUE))) > 0);

        assertTrue(new OpenSSLRSAPEM.INTEGER(INT_MIN).compareTo(new BigInteger(String.valueOf(Integer.MAX_VALUE) + "1")) < 0);
        assertTrue(new OpenSSLRSAPEM.INTEGER(INT_MAX).compareTo(new BigInteger(String.valueOf(Integer.MIN_VALUE) + "1")) > 0);

        assertTrue(new OpenSSLRSAPEM.INTEGER(INT_MAX).add(new OpenSSLRSAPEM.INTEGER(INT_MAX)).add(new OpenSSLRSAPEM.INTEGER(INT_MAX)).compareTo(0) > 0);
        assertTrue(OpenSSLRSAPEM.INTEGER.valueOf("10000000000000000000000000000000000000").compareTo(0) > 0);
        assertTrue(new OpenSSLRSAPEM.INTEGER(INT_MAX).add(new OpenSSLRSAPEM.INTEGER(INT_MAX)).add(new OpenSSLRSAPEM.INTEGER(INT_MAX)).compareTo(0) > 0);

        Assert.assertNotSame(OpenSSLRSAPEM.INTEGER.valueOf(1), OpenSSLRSAPEM.INTEGER.valueOf(1));
    }

    @Test
    public void testValueOf() throws Exception {
        assertEquals(0, OpenSSLRSAPEM.INTEGER.valueOf("0").compareTo(0));
        assertEquals(0, OpenSSLRSAPEM.INTEGER.valueOf("5").compareTo(5));
        assertEquals(0, OpenSSLRSAPEM.INTEGER.valueOf("0").compareTo(OpenSSLRSAPEM.INTEGER.valueOf(new byte[]{0})));
        assertEquals(0, new OpenSSLRSAPEM.INTEGER(INT_MAX).add(new OpenSSLRSAPEM.INTEGER(INT_MAX)).add(new OpenSSLRSAPEM.INTEGER(INT_MAX))
                .compareTo(OpenSSLRSAPEM.INTEGER.valueOf(new byte[]{1, 127, -1, -1, -3})));
    }
}