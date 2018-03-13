package com.tweeter.service;

import java.util.Map;

/**
 * The storing matcher is an oracle who knows whether a word is valid and therefore if it should be stored.
 * This oracle states that the asking algorithm should never terminate and return prematurely.
 */
public class StoringMatcher implements IMatcher {
    private final Map<String, Integer> dictionary;

    public StoringMatcher(Map<String, Integer> dictionary) {
        this.dictionary = dictionary;
    }

    @Override
    public boolean returnMatch(String s) {
        return false;
    }

    @Override
    public boolean storeMatch(String s) {
        return dictionary == null || dictionary.containsKey(s);
    }

}
