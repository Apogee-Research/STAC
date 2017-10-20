package com.nicnilov.textmeter;

import com.cyberpointllc.stac.hashmap.HashMap;

public class TextMeter {

    private HashMap<String, TextLanguage> textLanguages = new  HashMap();

    public TextMeter() {
    }

    public TextLanguage createTextLanguage(final String language) {
        TextMeterHelper0 conditionObj0 = new  TextMeterHelper0(0);
        if ((language == null) || (language.length() == conditionObj0.getValue()))
            throw new  IllegalArgumentException();
        TextLanguage tl = new  TextLanguage(language);
        textLanguages.put(language, tl);
        return tl;
    }

    public TextLanguage get(final String language) {
        return textLanguages.get(language);
    }

    public void release(final String language) {
        releaseHelper(language);
    }

    public void releaseAll() {
        textLanguages.clear();
    }

    public class TextMeterHelper0 {

        public TextMeterHelper0(int conditionRHS) {
            this.conditionRHS = conditionRHS;
        }

        private int conditionRHS;

        public int getValue() {
            return conditionRHS;
        }
    }

    private void releaseHelper(String language) {
        textLanguages.remove(language);
    }
}
