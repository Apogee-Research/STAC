package stac.crypto;

import org.junit.Test;
import stac.parser.OpenSSLRSAPEM;

import java.io.InputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 *
 */
public class DERTLVTest {
    @Test
    public void testDecodeTL() throws Exception {
        byte[] bytes = {0x30, 0x02, 0x05, 0x00};
        final OpenSSLRSAPEM.DER.DERTLV dertlv = new OpenSSLRSAPEM.DER.DERTLV(bytes);

        assertEquals(OpenSSLRSAPEM.DER.SEQUENCE, dertlv.getTag());
        assertFalse(dertlv.getLen().isBig());
        assertEquals(2, dertlv.getLen().getInternal().intValue());
        assertEquals(OpenSSLRSAPEM.DER.NULL, ((ArrayList<OpenSSLRSAPEM.DER.DERTLV>) dertlv.retrieve()).get(0).getTag());
    }

    @Test
    public void testDecodeLongTL() throws Exception {
        byte[] bytes = {0x30, (byte)0x81, 0x02, 0x05, 0x00};
        final OpenSSLRSAPEM.DER.DERTLV dertlv = new OpenSSLRSAPEM.DER.DERTLV(bytes);

        assertEquals(OpenSSLRSAPEM.DER.SEQUENCE, dertlv.getTag());
        assertFalse(dertlv.getLen().isBig());
        assertEquals(2, dertlv.getLen().getInternal().intValue());
        assertEquals(OpenSSLRSAPEM.DER.NULL, ((ArrayList<OpenSSLRSAPEM.DER.DERTLV>) dertlv.retrieve()).get(0).getTag());
    }

    @Test
    public void testSanity() throws Exception {
        final BigInteger hugePart = BigInteger.valueOf(Integer.MAX_VALUE).multiply(BigInteger.valueOf(2));
        final byte[] hugeBytes = hugePart.toByteArray();
        final BigInteger bigInteger = new BigInteger(hugeBytes);
        assertArrayEquals(hugeBytes, bigInteger.toByteArray());
    }

    @Test
    public void testDecodeHugeTL() throws Exception {
        final BigInteger hugePart = BigInteger.valueOf(2);
        final byte[] hugeBytes = hugePart.toByteArray();
        byte[] bytes = new byte[hugeBytes.length + 2 + 2];
        bytes[0] = 0x30;
        bytes[1] = (byte) (-128 + hugeBytes.length);
        for (int i = 2, j = 0; i < hugeBytes.length + 2; i++, j++) {
            bytes[i] = hugeBytes[j];
        }
        bytes[bytes.length - 2] = 0x05;
        bytes[bytes.length - 1] = 0x00;

        final OpenSSLRSAPEM.DER.DERTLV dertlv = new OpenSSLRSAPEM.DER.DERTLV(bytes);

        assertEquals(OpenSSLRSAPEM.DER.SEQUENCE, dertlv.getTag());
        final OpenSSLRSAPEM.DER.DERTLV dertlv1 = ((ArrayList<OpenSSLRSAPEM.DER.DERTLV>) dertlv.retrieve()).get(0);
        assertEquals(OpenSSLRSAPEM.DER.NULL, dertlv1.getTag());
        assertArrayEquals(""
                        + Arrays.toString(hugeBytes)
                        + " <==> "
                        + Arrays.toString(dertlv.getLen().getInternalBig().toByteArray()),
                hugeBytes,
                dertlv.getLen().getInternalBig().toByteArray()
        );
    }

    @Test
    public void testExt() throws Exception {
        final InputStream systemResourceAsStream = ClassLoader.getSystemResourceAsStream("test.der");
        final OpenSSLRSAPEM.DER.DERTLV dertlv = new OpenSSLRSAPEM.DER.DERTLV(systemResourceAsStream);
        assertEquals(OpenSSLRSAPEM.DER.SEQUENCE, dertlv.getTag());
    }


    @Test
    public void testExternal() throws Exception {
        final InputStream systemResourceAsStream = ClassLoader.getSystemResourceAsStream("test.der");
        final OpenSSLRSAPEM.DER.DERTLV dertlv = new OpenSSLRSAPEM.DER.DERTLV(systemResourceAsStream);
        assertEquals(OpenSSLRSAPEM.DER.SEQUENCE, dertlv.getTag());
        assertTrue(dertlv.getLen().compareTo(1189) == 0);
        final ArrayList<OpenSSLRSAPEM.DER.DERTLV> retrieve = (ArrayList<OpenSSLRSAPEM.DER.DERTLV>) dertlv.retrieve();
        for (OpenSSLRSAPEM.DER.DERTLV tlv : retrieve) {
            assertEquals(OpenSSLRSAPEM.DER.INTEGER, tlv.getTag());
        }
        assertEquals("0", retrieve.get(0).retrieve().toString());
        assertEquals("27519823697233735380743008444130462617504498856263805871960236657200750121491906540499872291996754025489263624426553812035305541814903945972268589353118652954079253754576536430217822166478205071003616836953369194884400421042197625785938688693838920702670372740431095261769257911090648794601174073197679675467305173154014605412894043855147928268581795957942549003942037398536289838858951686413466875005597254461362998032920019698820378024162053501797753746706215422695634740954934259192335032738745383771922261490145825796696863115526907366197089744544792405226041309150600507346500655638015195669274915998750485762351", retrieve.get(1).retrieve().toString());
        assertEquals("65537", retrieve.get(2).retrieve().toString());
        assertEquals("25054935693468503119807026333764626287419944966304415901891320027239531218991677793490789630995778299874389927501399000605926053427374975118289667225117577335109126657831731681062709541285915772335822601133660066087363167711747649995721557872580174276610664667337567495979161569511050881600748484451316862170802605650882183607947062040298120704364323058153494467326052596291578721692158987007647846010094253528440675301088958184436475963154266673034451906878589283686653705868407679907007755601529862667240264708559390985199877145541968578134624717119957550292290209508076486687996197095252953016995918199651168694737", retrieve.get(3).retrieve().toString());
        assertEquals("171504653925518355226468767714358135816355065768153436394760598923196736895700838663615344810811083303425068873024784572069491245934220028407197048303096084650261571961466797636101295585386466585932090112825564917545379045276805351639191297027183054740491621956697360915835258660307152391343654467397499521239", retrieve.get(4).retrieve().toString());
        assertEquals("160461089931618656138646116058807925818139364324737181753663763878747572884546982438704382123598628739290828799436907197855294152534549498150325504927628599698008228213843312226360150767962255553548030357914947942401784093925098332368264309955964589168476205025689415087188990102356629902449647053491879720809", retrieve.get(5).retrieve().toString());
        assertEquals("156857789589019488415315591754255255212053079056763614103513287142475432343993595518517841340922171188905482830295948658770546489483759532824624121874476697345570877876166438047330693461526539316123250703614208174888536551473087153474420958295456799993058391749461217530481489907972759118316960629504037739033", retrieve.get(6).retrieve().toString());
        assertEquals("63900910724099597274250041701921480322987308993565706495397570433059690018062952144097924364005075170769967206867934769013310375619866752402846412760824859598674287015778821212380389320430396687246136446758658869779577384795629054983892003380391531695196003402131132709164982417281927519172884606391573749689", retrieve.get(7).retrieve().toString());
        assertEquals("126609768008734548028968717676367359745073171768893864543812789476987026847646111928442669699038407296347314420371541512446893717685458953298083300685405236110932235007673320307962646431267571756566041354368552253429826259428872605811381294148376013268875098261283558071028420971973188525861814142525203622427", retrieve.get(8).retrieve().toString());
    }
}