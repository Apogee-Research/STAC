package com.nicnilov.textmeter;

import com.cyberpointllc.stac.hashmap.HashMap;

public class TextMeter {

    private HashMap<String, TextLanguage> textLanguages = new  HashMap();

    public TextMeter() {
    }

    public TextLanguage createTextLanguage(final String language) {
        if ((language == null) || (language.length() == 0))
            throw new  IllegalArgumentException();
        TextLanguage tl = new  TextLanguage(language);
        textLanguages.put(language, tl);
        return tl;
    }

    public TextLanguage get(final String language) {
        return textLanguages.get(language);
    }

    public void release(final String language) {
        textLanguages.remove(language);
    }

    public void releaseAll() {
        releaseAllHelper();
    }

    private void releaseAllHelper() {
        textLanguages.clear();
    }
}
