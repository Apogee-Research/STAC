package com.nicnilov.textmeter.ngrams;

/**
 * Created as part of textmeter project
 * by Nic Nilov on 07.10.13 at 16:06
 */
public enum NgramType {

    UNIGRAM(1), BIGRAM(2), TRIGRAM(3), QUADGRAM(4), QUINTGRAM(5);

    private int length;

    NgramType(int length) {
        this.length = length;
    }

    public int length() {
        return this.length;
    }
}
