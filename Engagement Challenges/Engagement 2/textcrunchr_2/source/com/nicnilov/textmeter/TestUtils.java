package com.nicnilov.textmeter;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.MissingResourceException;

/**
 * Created as part of textmeter project
 * by Nic Nilov on 14.11.13 at 23:19
 */
public class TestUtils {

    public static final String EN_UNIGRAMS = "en/english_unigrams.txt";

    public static final int EN_UNIGRAMS_EXCNT = 26;

    private static final String UNIGRAM_STRING = "E 529117365\nT 390965105\nA 374061888\nO 326627740\nI 320410057\nN 313720540\nS 294300210\nR 277000841\nH 216768975\nL 183996130\nD 169330528\nC 138416451\nU 117295780\nM 110504544\nF 95422055\nG 91258980\nP 90376747\nW 79843664\nY 75294515\nB 70195826\nV 46337161\nK 35373464\nJ 9613410\nX 8369915\nZ 4975847\nQ 4550166";

    public static InputStream loadResource(Class clazz, String resourceName) {
        //        InputStream is = clazz.getClassLoader().getResourceAsStream(resourceName);
        //        if (is == null) {
        //            throw new MissingResourceException("Could not load example resource", clazz.getName(), resourceName);
        //        }
        //        return is;
        String str;
        if (resourceName == EN_UNIGRAMS) {
            str = UNIGRAM_STRING;
        } else {
            str = "unsupported";
        }
        return new  ByteArrayInputStream(str.getBytes(StandardCharsets.UTF_8));
    }
}
