package stac.crypto;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import stac.parser.OpenSSLRSAPEM;

import java.io.File;
import java.io.InputStream;
import java.net.URL;

import static org.junit.Assert.assertEquals;

/**
 *
 */
public class OpenSSLRSAPEMTest {

    private RSA rsa = new RSA();

    @Test
    public void testExternalPrivate() throws Exception {
        final InputStream systemResourceAsStream = ClassLoader.getSystemResourceAsStream("test.pem");
        final OpenSSLRSAPEM openSSLRSAPEM = new OpenSSLRSAPEM(systemResourceAsStream);

        assertEquals(OpenSSLRSAPEM.DER_TYPE.PRIVATE_KEY, openSSLRSAPEM.getType());
        assertEquals("0", openSSLRSAPEM.getVersion().toString());
        assertEquals("9594086423184888700101204868028978689761528050684967942870353100400096741965478560819712664893580379549602639699682612731515562339039161369038488848480921", openSSLRSAPEM.getModulus().toString());
        assertEquals("65537", openSSLRSAPEM.getPublicExponent().toString());
        assertEquals("925196853602217016107536426231642359572346266694065907791638793269887413357654161209379201173231189031244625381248291041114802926301090535869009979212993", openSSLRSAPEM.getPrivateExponent().toString());
        assertEquals("107774441894641194554269648058938388336586769562954930270693778876359045905043", openSSLRSAPEM.getPrime1().toString());
        assertEquals("89020052013481406296294151436262448866714259177158139623757076040720465917347", openSSLRSAPEM.getPrime2().toString());
        assertEquals("84209009355003916101446295499550758373920055983798970266741174221000620468735", openSSLRSAPEM.getExponent1().toString());
        assertEquals("12487012499197927401038072144796995474795980661544086204147257275162781988467", openSSLRSAPEM.getExponent2().toString());
        assertEquals("50397756061555897817040451740381065828050553521791436566754955283746426700019", openSSLRSAPEM.getCoefficient().toString());

        assertEquals("-----BEGIN RSA PRIVATE KEY-----", openSSLRSAPEM.getType().getHeader());
        assertEquals("-----END RSA PRIVATE KEY-----", openSSLRSAPEM.getType().getFooter());
    }

    @Test
    public void testExternalPublic() throws Exception {
        final InputStream systemResourceAsStream = ClassLoader.getSystemResourceAsStream("test.pub");
        final OpenSSLRSAPEM openSSLRSAPEM = new OpenSSLRSAPEM(systemResourceAsStream);

        assertEquals(OpenSSLRSAPEM.DER_TYPE.PUBLIC_KEY, openSSLRSAPEM.getType());
        assertEquals("9594086423184888700101204868028978689761528050684967942870353100400096741965478560819712664893580379549602639699682612731515562339039161369038488848480921", openSSLRSAPEM.getModulus().toString());
        assertEquals(65537, (int) openSSLRSAPEM.getPublicExponent().getInternal());

        assertEquals("-----BEGIN PUBLIC KEY-----", openSSLRSAPEM.getType().getHeader());
        assertEquals("-----END PUBLIC KEY-----", openSSLRSAPEM.getType().getFooter());
    }

    @Test
    public void testTypeOf() throws Exception {
        assertEquals(OpenSSLRSAPEM.DER_TYPE.PUBLIC_KEY, OpenSSLRSAPEM.DER_TYPE.typeOf("-----BEGIN PUBLIC KEY-----"));
        assertEquals(OpenSSLRSAPEM.DER_TYPE.PUBLIC_KEY, OpenSSLRSAPEM.DER_TYPE.typeOf("-----END PUBLIC KEY-----"));

        assertEquals(OpenSSLRSAPEM.DER_TYPE.PRIVATE_KEY, OpenSSLRSAPEM.DER_TYPE.typeOf("-----BEGIN RSA PRIVATE KEY-----"));
        assertEquals(OpenSSLRSAPEM.DER_TYPE.PRIVATE_KEY, OpenSSLRSAPEM.DER_TYPE.typeOf("-----END RSA PRIVATE KEY-----"));

        assertEquals(null, OpenSSLRSAPEM.DER_TYPE.typeOf("fuzz"));
    }

    /* Throws section */

    @Rule
    public ExpectedException expect = ExpectedException.none();

    @Test
    public void testBadExternalPrivate() throws Exception {
        expect.expectMessage("Bad Private Key");
        final InputStream systemResourceAsStream = ClassLoader.getSystemResourceAsStream("bad.pem");
        final OpenSSLRSAPEM openSSLRSAPEM = new OpenSSLRSAPEM(systemResourceAsStream);
    }

    @Test
    public void testBadExternalPublic() throws Exception {
        expect.expectMessage("Bad Public Key");
        final InputStream systemResourceAsStream = ClassLoader.getSystemResourceAsStream("bad.pub");
        final OpenSSLRSAPEM openSSLRSAPEM = new OpenSSLRSAPEM(systemResourceAsStream);
    }

    @Test
    public void testBadExternalWhat() throws Exception {
        expect.expectMessage("Unknown Key Type");
        final InputStream systemResourceAsStream = ClassLoader.getSystemResourceAsStream("bad.what");
        final OpenSSLRSAPEM openSSLRSAPEM = new OpenSSLRSAPEM(systemResourceAsStream);
    }

    @Test
    public void testBadExternalWhatFile() throws Exception {
        final URL systemResourceAsStream = ClassLoader.getSystemResource("test.pem");
        final OpenSSLRSAPEM openSSLRSAPEM = new OpenSSLRSAPEM(new File(systemResourceAsStream.getFile()));
    }

    @Test
    public void testRSASmall() throws Exception {
        final OpenSSLRSAPEM privateKey = new OpenSSLRSAPEM(ClassLoader.getSystemResourceAsStream("test.pem"));
        privateKey.getPublicExponent().getInternalBig();

        for (int i = 0; i < 5; i++) {
            OpenSSLRSAPEM.INTEGER message = OpenSSLRSAPEM.INTEGER.randomInt().abs();

            OpenSSLRSAPEM.INTEGER encrypt = rsa.encrypt(message, privateKey.getPublicExponent(), privateKey.getModulus());
            OpenSSLRSAPEM.INTEGER decrypt = rsa.decrypt(encrypt, privateKey.getPrivateExponent(), privateKey.getModulus());
            Assert.assertEquals(0, message.compareTo(decrypt));
        }
    }

    @Test
    public void testRSADecryptSizeException() throws Exception {
        expect.expect(RSA.RSAMessageSizeException.class);
        rsa.decrypt(OpenSSLRSAPEM.INTEGER.valueOf(6), OpenSSLRSAPEM.INTEGER.valueOf(2), OpenSSLRSAPEM.INTEGER.valueOf(4));
    }

    @Test
    public void testRSADecryptSizeExceptionBig() throws Exception {
        expect.expect(RSA.RSAMessageSizeException.class);

        rsa.decrypt(
                OpenSSLRSAPEM.INTEGER.valueOf("6000000000000000"),
                OpenSSLRSAPEM.INTEGER.valueOf("2000000000000000"),
                OpenSSLRSAPEM.INTEGER.valueOf("4000000000000000")
        );
    }

    @Test
    public void testRSAEncryptSizeException() throws Exception {
        expect.expect(RSA.RSAMessageSizeException.class);
        rsa.encrypt(OpenSSLRSAPEM.INTEGER.valueOf(6), OpenSSLRSAPEM.INTEGER.valueOf(2), OpenSSLRSAPEM.INTEGER.valueOf(4));
    }

    @Test
    public void testRSAEncryptSizeExceptionBig() throws Exception {
        expect.expect(RSA.RSAMessageSizeException.class);

        rsa.encrypt(
                OpenSSLRSAPEM.INTEGER.valueOf("6000000000000000"),
                OpenSSLRSAPEM.INTEGER.valueOf("2000000000000000"),
                OpenSSLRSAPEM.INTEGER.valueOf("4000000000000000")
        );
    }

    @Test
    public void testRSALarge() throws Exception {
        final OpenSSLRSAPEM privateKey = new OpenSSLRSAPEM(ClassLoader.getSystemResourceAsStream("test.pem"));
        privateKey.getPublicExponent().getInternalBig();

        OpenSSLRSAPEM.INTEGER message;
        for (int i = 0; i < 5; i++) {
            do {
                message = OpenSSLRSAPEM.INTEGER.randomINTEGER(12).abs();
            } while (message.compareTo(OpenSSLRSAPEM.INTEGER.INT_MAX) <= 0 || message.compareTo(privateKey.getModulus()) >= 0);

            OpenSSLRSAPEM.INTEGER encrypt = rsa.encrypt(message, privateKey.getPublicExponent(), privateKey.getModulus());
            OpenSSLRSAPEM.INTEGER decrypt = rsa.decrypt(encrypt, privateKey.getPrivateExponent(), privateKey.getModulus());
            Assert.assertEquals(0, message.compareTo(decrypt));
        }
    }
}