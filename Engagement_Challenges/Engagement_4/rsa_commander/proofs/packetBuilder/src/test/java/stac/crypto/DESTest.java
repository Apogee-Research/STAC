package stac.crypto;

import org.junit.Assert;
import org.junit.Test;
import stac.parser.OpenSSLRSAPEM;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;

/**
 *
 */
public class DESTest {
    private static final SymmetricCipher des = new DES();
    private static final String MSG = "This should be encrypted..........";

    @Test
    public void testRijndaelEncrypt() throws Exception {
        byte[] key = {59,31,-7,-54,10,38,-91,-31};

        OpenSSLRSAPEM.INTEGER counter = OpenSSLRSAPEM.INTEGER.randomLong();
        OpenSSLRSAPEM.INTEGER ccopy = new OpenSSLRSAPEM.INTEGER(counter);


        byte[] MSGBytes,CipherBytes, PTBytes;

        MSGBytes = MSG.getBytes(Charset.defaultCharset());

        ByteArrayInputStream is = new ByteArrayInputStream(MSGBytes);
        ByteArrayOutputStream os = new ByteArrayOutputStream(MSGBytes.length);

        des.encrypt_ctr(counter, key, is, os);

        CipherBytes = os.toByteArray();

        is = new ByteArrayInputStream(CipherBytes);
        os = new ByteArrayOutputStream(CipherBytes.length);

        des.encrypt_ctr(ccopy, key, is, os);

        PTBytes = os.toByteArray();

        Assert.assertArrayEquals(MSGBytes, PTBytes);
    }
}