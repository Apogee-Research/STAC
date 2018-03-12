package com.tweeter.model.hibernate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;

/**
 * These TweetCorrections objects represent the set of correction objects that a tweet needs.
 */
@Entity
public class TweetCorrections {
    @Id
    @GeneratedValue
    private Long id;


    @Column
    @OneToMany
    private List<AlternativeSuggestions> alternatives = new ArrayList<>();

    public List<AlternativeSuggestions> getAlternatives() {
        return alternatives;
    }
}
