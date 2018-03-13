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
        assertEquals("27519823697233735380743008444130462617504498856263805871960236657200750121491906540499872291996754025489263624426553812035305541814903945972268589353118652954079253754576536430217822166478205071003616836953369194884400421042197625785938688693838920702670372740431095261769257911090648794601174073197679675467305173154014605412894043855147928268581795957942549003942037398536289838858951686413466875005597254461362998032920019698820378024162053501797753746706215422695634740954934259192335032738745383771922261490145825796696863115526907366197089744544792405226041309150600507346500655638015195669274915998750485762351", openSSLRSAPEM.getModulus().toString());
        assertEquals("65537", openSSLRSAPEM.getPublicExponent().toString());
        assertEquals("25054935693468503119807026333764626287419944966304415901891320027239531218991677793490789630995778299874389927501399000605926053427374975118289667225117577335109126657831731681062709541285915772335822601133660066087363167711747649995721557872580174276610664667337567495979161569511050881600748484451316862170802605650882183607947062040298120704364323058153494467326052596291578721692158987007647846010094253528440675301088958184436475963154266673034451906878589283686653705868407679907007755601529862667240264708559390985199877145541968578134624717119957550292290209508076486687996197095252953016995918199651168694737", openSSLRSAPEM.getPrivateExponent().toString());
        assertEquals("171504653925518355226468767714358135816355065768153436394760598923196736895700838663615344810811083303425068873024784572069491245934220028407197048303096084650261571961466797636101295585386466585932090112825564917545379045276805351639191297027183054740491621956697360915835258660307152391343654467397499521239", openSSLRSAPEM.getPrime1().toString());
        assertEquals("160461089931618656138646116058807925818139364324737181753663763878747572884546982438704382123598628739290828799436907197855294152534549498150325504927628599698008228213843312226360150767962255553548030357914947942401784093925098332368264309955964589168476205025689415087188990102356629902449647053491879720809", openSSLRSAPEM.getPrime2().toString());
        assertEquals("156857789589019488415315591754255255212053079056763614103513287142475432343993595518517841340922171188905482830295948658770546489483759532824624121874476697345570877876166438047330693461526539316123250703614208174888536551473087153474420958295456799993058391749461217530481489907972759118316960629504037739033", openSSLRSAPEM.getExponent1().toString());
        assertEquals("63900910724099597274250041701921480322987308993565706495397570433059690018062952144097924364005075170769967206867934769013310375619866752402846412760824859598674287015778821212380389320430396687246136446758658869779577384795629054983892003380391531695196003402131132709164982417281927519172884606391573749689", openSSLRSAPEM.getExponent2().toString());
        assertEquals("126609768008734548028968717676367359745073171768893864543812789476987026847646111928442669699038407296347314420371541512446893717685458953298083300685405236110932235007673320307962646431267571756566041354368552253429826259428872605811381294148376013268875098261283558071028420971973188525861814142525203622427", openSSLRSAPEM.getCoefficient().toString());

        assertEquals("-----BEGIN RSA PRIVATE KEY-----", openSSLRSAPEM.getType().getHeader());
        assertEquals("-----END RSA PRIVATE KEY-----", openSSLRSAPEM.getType().getFooter());
    }

    @Test
    public void testExternalPublic() throws Exception {
        final InputStream systemResourceAsStream = ClassLoader.getSystemResourceAsStream("test.pub");
        final OpenSSLRSAPEM openSSLRSAPEM = new OpenSSLRSAPEM(systemResourceAsStream);

        assertEquals(OpenSSLRSAPEM.DER_TYPE.PUBLIC_KEY, openSSLRSAPEM.getType());
        assertEquals("27519823697233735380743008444130462617504498856263805871960236657200750121491906540499872291996754025489263624426553812035305541814903945972268589353118652954079253754576536430217822166478205071003616836953369194884400421042197625785938688693838920702670372740431095261769257911090648794601174073197679675467305173154014605412894043855147928268581795957942549003942037398536289838858951686413466875005597254461362998032920019698820378024162053501797753746706215422695634740954934259192335032738745383771922261490145825796696863115526907366197089744544792405226041309150600507346500655638015195669274915998750485762351", openSSLRSAPEM.getModulus().toString());
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
                message = OpenSSLRSAPEM.INTEGER.randomINTEGER(128).abs();
            } while (message.compareTo(OpenSSLRSAPEM.INTEGER.INT_MAX) <= 0 || message.compareTo(privateKey.getModulus()) >= 0);

            OpenSSLRSAPEM.INTEGER encrypt = rsa.encrypt(message, privateKey.getPublicExponent(), privateKey.getModulus());
            OpenSSLRSAPEM.INTEGER decrypt = rsa.decrypt(encrypt, privateKey.getPrivateExponent(), privateKey.getModulus());
            Assert.assertEquals(0, message.compareTo(decrypt));
        }
    }
}