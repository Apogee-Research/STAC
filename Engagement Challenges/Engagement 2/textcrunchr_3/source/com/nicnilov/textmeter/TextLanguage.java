package com.nicnilov.textmeter;

import com.nicnilov.textmeter.ngrams.Ngram;
import com.nicnilov.textmeter.ngrams.NgramBuilder;
import com.nicnilov.textmeter.ngrams.NgramType;
import com.nicnilov.textmeter.ngrams.TextScore;
import com.nicnilov.textmeter.ngrams.storage.LineFormatException;
import com.nicnilov.textmeter.ngrams.storage.NgramStorageStrategy;
import java.io.IOException;
import java.io.InputStream;
import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;

/**
 * Created as part of textmeter project
 * by Nic Nilov on 25.10.13 at 23:19
 */
public class TextLanguage {

    private EnumMap<NgramType, Ngram> ngrams = new  EnumMap(NgramType.class);

    private final String language;

    public TextLanguage(String language) {
        this.language = language;
    }

    protected Ngram getNgram(NgramType ngramType) {
        if (ngrams.containsKey(ngramType)) {
            return ngrams.get(ngramType);
        }
        throw new  NotInitializedException(String.format("Ngrams of type %s have not been loaded", ngramType));
    }

    public Ngram getNgram(NgramType ngramType, InputStream inputStream, NgramStorageStrategy ngramStorageStrategy, int sizeHint) throws IOException, LineFormatException {
        Ngram ngram = NgramBuilder.build(ngramType, inputStream, ngramStorageStrategy, sizeHint);
        ngrams.put(ngramType, ngram);
        return ngram;
    }

    public TextScore score(final String text) {
        TextScore textScore = new  TextScore();
        Ngram ngram;
        for (Map.Entry<NgramType, Ngram> entry : ngrams.entrySet()) {
            if ((ngram = entry.getValue()) != null) {
                textScore.getNgramScores().put(entry.getKey(), ngram.score(text));
            }
        }
        return textScore;
    }

    public void releaseNgram(NgramType ngramType) {
        releaseNgramHelper(ngramType);
    }

    public void releaseAllNgrams() {
        releaseAllNgramsHelper();
    }

    private void releaseNgramHelper(NgramType ngramType) {
        ngrams.remove(ngramType);
    }

    private void releaseAllNgramsHelper() {
        ngrams.clear();
    }
}
