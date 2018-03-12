package com.tweeter.model.hibernate;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * These AlternativeSuggestions objects represent a misspelled word in a tweet.
 */
@Entity
public class AlternativeSuggestions {
    @Id
    @GeneratedValue
    private Long id;

    @Column
    private int wordIndex;

    @Column
    private String word;

    @Column
    @ElementCollection
    private Map<Integer, String> alternatives = new HashMap<>();

    @Column
    private Integer currentSuggestedAlternative = 0;

    public AlternativeSuggestions() { /* This actually does need to be here */}

    public AlternativeSuggestions(int wordIndex, String word, Map<Integer, String> alternatives) {
        this.wordIndex = wordIndex;
        this.word = word;
        this.alternatives = alternatives;
    }

    public int getWordIndex() {
        return wordIndex;
    }

    public Map<Integer, String> getAlternatives() {
        return alternatives;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public String thisCorrection() {
        if (currentSuggestedAlternative == null) {
            return null;
        }
        if (currentSuggestedAlternative == -1) {
            return getWord();
        }
        ArrayList<Integer> integers = new ArrayList<>(new TreeMap<>(alternatives).descendingKeySet());
        if (currentSuggestedAlternative < integers.size()) {
            return alternatives.get(integers.get(currentSuggestedAlternative));
        }
        return null;
    }

    public String nextCorrection() {
        if (currentSuggestedAlternative + 1 < alternatives.size()) {
            currentSuggestedAlternative++;
            return thisCorrection();
        } else if (currentSuggestedAlternative + 1 == alternatives.size()) {
            currentSuggestedAlternative = -1;
            return thisCorrection();
        } else {
            return thisCorrection();
        }
    }
}
