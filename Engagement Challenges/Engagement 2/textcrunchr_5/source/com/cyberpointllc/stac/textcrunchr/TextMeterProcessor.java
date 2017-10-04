package com.cyberpointllc.stac.textcrunchr;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Locale;
import java.util.Scanner;
import com.cyberpointllc.stac.textcrunchr.Processor;
import com.nicnilov.textmeter.TextMeter;
import com.nicnilov.textmeter.TextLanguage;
import com.nicnilov.textmeter.TestUtils;
import com.nicnilov.textmeter.ngrams.NgramType;
import com.nicnilov.textmeter.ngrams.TextScore;
import com.nicnilov.textmeter.ngrams.storage.LineFormatException;
import com.nicnilov.textmeter.ngrams.storage.NgramStorageStrategy;

class TextMeterProcessor extends Processor {

    private static final String NAME = "languageAnalysis";

    public TCResult process(InputStream inps) throws IOException {
        Classprocess replacementClass = new  Classprocess(inps);
        ;
        replacementClass.doIt0();
        replacementClass.doIt1();
        replacementClass.doIt2();
        return replacementClass.doIt3();
    }

    public String getName() {
        return NAME;
    }

    public class Classprocess {

        public Classprocess(InputStream inps) throws IOException {
            this.inps = inps;
        }

        private InputStream inps;

        private InputStreamReader is;

        private StringBuilder sb;

        public void doIt0() throws IOException {
            is = new  InputStreamReader(inps);
            sb = new  StringBuilder();
        }

        private BufferedReader br;

        private String read;

        public void doIt1() throws IOException {
            br = new  BufferedReader(is);
            read = br.readLine();
        }

        private String theString;

        private TextMeter textMeter;

        private TextLanguage en;

        public void doIt2() throws IOException {
            while (read != null) {
                //System.out.println(read);
                sb.append(read);
                read = br.readLine();
            }
            theString = sb.toString();
            textMeter = new  TextMeter();
            textMeter.createTextLanguage("en");
            en = textMeter.get("en");
        }

        private long mark;

        private String message;

        public TCResult doIt3() throws IOException {
            mark = System.currentTimeMillis();
            try {
                en.getNgram(NgramType.UNIGRAM, TestUtils.loadResource(TextMeterProcessor.this.getClass(), TestUtils.EN_UNIGRAMS), NgramStorageStrategy.TREEMAP, TestUtils.EN_UNIGRAMS_EXCNT);
                //        	en.getNgram(NgramType.BIGRAM, TestUtils.loadResource(this.getClass(), TestUtils.EN_BIGRAMS), NgramStorageStrategy.TREEMAP, TestUtils.EN_BIGRAMS_EXCNT);
                //        	en.getNgram(NgramType.TRIGRAM, TestUtils.loadResource(this.getClass(), TestUtils.EN_TRIGRAMS), NgramStorageStrategy.TREEMAP, TestUtils.EN_TRIGRAMS_EXCNT);
                //        	en.getNgram(NgramType.QUADGRAM, TestUtils.loadResource(this.getClass(), TestUtils.EN_QUADGRAMS), NgramStorageStrategy.TREEMAP, TestUtils.EN_QUADGRAMS_EXCNT);
                //        	en.getNgram(NgramType.QUINTGRAM, TestUtils.loadResource(this.getClass(), TestUtils.EN_QUINTGRAMS), NgramStorageStrategy.TREEMAP, TestUtils.EN_QUINTGRAMS_EXCNT);
                //        
                // score text
                TextScore textScore = en.score(theString.toUpperCase(Locale.ENGLISH));
                message = "en-based score for english text: " + textScore;
            } catch (LineFormatException lfe) {
                message = "Processing failed.";
                lfe.printStackTrace();
            }
            return new  TCResult("TextLanguage analysis:", message);
        }
    }
}
