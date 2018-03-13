package com.tweeter.model.hibernate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * This table represents hashtags which tweets reference.
 */
@Entity
@Table(name = "hashTags")
public class HashTag implements Comparable<HashTag> {

    public HashTag() {}

    @Id
    @GeneratedValue
    private Long id;

    @Column(unique = true)
    private String hashTag;

    public String getHashTag() {
        return hashTag;
    }

    public void setHashTag(String hashTag) {
        this.hashTag = hashTag;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        HashTag hashTag1 = (HashTag) o;

        return hashTag.equals(hashTag1.hashTag);

    }

    @Override
    public int hashCode() {
        return hashTag.hashCode();
    }

    @Override
    public int compareTo(HashTag o) {
        return o.hashCode() - hashCode();
    }
}
