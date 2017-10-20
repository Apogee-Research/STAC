package com.nicnilov.textmeter.ngrams;

import com.nicnilov.textmeter.ngrams.storage.LineFormatException;
import com.nicnilov.textmeter.ngrams.storage.NgramStorageStrategy;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created as part of textmeter project
 * by Nic Nilov on 26.10.13 at 0:12
 */
public class NgramBuilder {

    public static Ngram build(NgramType ngramType, InputStream inputStream, NgramStorageStrategy ngramStorageStrategy, int sizeHint) throws IOException, LineFormatException {
        Ngram ngram = new  Ngram(ngramType, ngramStorageStrategy, sizeHint);
        ngram.load(inputStream);
        return ngram;
    }
}
