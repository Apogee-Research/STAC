package com.tweeter.service;

import com.tweeter.utility.TrieNode;

/**
 * The possible-prefix word matcher is the oracle who knows when s's are prefixes of valid words in the dictionary.
 */
public class PossiblePrefixWordMatcher implements IMatcher {
    private final TrieNode possible;

    public PossiblePrefixWordMatcher(TrieNode pTrie) {
        this.possible = pTrie;
    }

    @Override
    public boolean returnMatch(String s) {
        return possible.lookup(s);
    }

    @Override
    public boolean storeMatch(String s) {
        return true;
    }

}
