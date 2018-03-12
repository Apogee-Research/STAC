package stac.crypto;

import org.junit.Assert;
import org.junit.Test;
import stac.parser.OpenSSLRSAPEM;

import java.math.BigInteger;

/**
 *
 */
public class INTEGERTest {
    @Test
    public void testSetInteger() throws Exception {
        final OpenSSLRSAPEM.INTEGER integer = new OpenSSLRSAPEM.INTEGER();
        final OpenSSLRSAPEM.INTEGER integer2 = new OpenSSLRSAPEM.INTEGER();
        integer.set(BigInteger.valueOf(Integer.MAX_VALUE).multiply(BigInteger.valueOf(2)));
        integer2.set(BigInteger.valueOf(Integer.MAX_VALUE).multiply(BigInteger.valueOf(2)).toByteArray());
        Assert.assertEquals(0, integer.compareTo(integer2));
    }
}