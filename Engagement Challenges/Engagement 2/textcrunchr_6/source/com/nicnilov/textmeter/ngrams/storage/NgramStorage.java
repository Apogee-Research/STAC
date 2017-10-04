package com.nicnilov.textmeter.ngrams.storage;

import com.nicnilov.textmeter.ngrams.NgramType;
import java.io.*;
import java.util.AbstractMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created as part of textmeter project
 * by Nic Nilov on 25.10.13 at 23:30
 */
public abstract class NgramStorage implements Iterable<Map.Entry<String, Float>> {

    protected static int DEFAULT_SIZE_HINT = 16;

    private NgramType ngramType;

    private long count = 0;

    protected AbstractMap<String, Float> storage;

    public abstract NgramStorageStrategy getStorageStrategy();

    protected NgramStorage(NgramType ngramType) {
        this.ngramType = ngramType;
    }

    public long load(InputStream inputStream) throws LineFormatException, IOException {
        BufferedReader br = new  BufferedReader(new  InputStreamReader(inputStream));
        count = 0;
        storage.clear();
        final String lineRegex = String.format("^[A-ZА-ЯЁ]{%d}\\s\\d+$", this.getNgramType().length());
        int lineNo = 0;
        int freqStart = this.getNgramType().length() + 1;
        String line;
        float ngramFrequency;
        long totalOccurences = 0;
        while ((line = br.readLine()) != null) {
            lineNo++;
            if (!line.matches(lineRegex)) {
                throw new  LineFormatException(String.format("Ngram resource line %d doesn't match pattern \"%s\"", lineNo, lineRegex));
            }
            ngramFrequency = Long.parseLong(line.substring(freqStart, line.length()));
            storage.put(line.substring(0, this.getNgramType().length()), ngramFrequency);
            totalOccurences += ngramFrequency;
        }
        count = lineNo;
        return totalOccurences;
    }

    public Float get(String key) {
        return storage.get(key);
    }

    @Override
    public Iterator<Map.Entry<String, Float>> iterator() {
        return storage.entrySet().iterator();
    }

    public NgramType getNgramType() {
        return ngramType;
    }

    public long count() {
        return count;
    }
}
