package com.tweeter.model.hibernate;

import org.joda.time.Duration;
import org.joda.time.Instant;
import org.joda.time.format.PeriodFormatterBuilder;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Date;
import java.util.Set;
import java.util.TreeSet;

/**
 * Tweets are the actual posted messages and any required meta-data (Who a tweet mentions, the hashTags in the tweet, author, etc)
 */
@Entity
@Table(name = "tweets")
public class Tweet {
    public Tweet() {}

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(optional = false)
    private User author;

    @Column(nullable = false)
    private String text;

    @Column
    @ManyToMany
    private Set<User> atMentions = new TreeSet<>();

    @Column
    @ManyToMany
    private Set<HashTag> hashTags = new TreeSet<>();

    @Column
    private Boolean actionRequired = true;

    @ManyToOne
    private TweetCorrections tweetCorrections;

    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date tweeted;

    public Tweet(User author, String text) {
        this.author = author;
        this.text = text;
    }

    public Long getId() {
        return id;
    }

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Set<User> getAtMentions() {
        return atMentions;
    }

    public void setAtMentions(Set<User> atMentions) {
        this.atMentions = atMentions;
    }

    public Set<HashTag> getHashTags() {
        return hashTags;
    }

    public void setHashTags(Set<HashTag> hashTags) {
        this.hashTags = hashTags;
    }

    public Boolean getActionRequired() {
        return actionRequired;
    }

    public void setActionRequired(Boolean actionRequired) {
        this.actionRequired = actionRequired;
    }

    public TweetCorrections getTweetCorrections() {
        return tweetCorrections;
    }

    public void setTweetCorrections(TweetCorrections tweetCorrections) {
        this.tweetCorrections = tweetCorrections;
    }

    public String getAge() {
        Duration dur = new Duration(new Instant(tweeted), new Instant());
        PeriodFormatterBuilder pfb = new PeriodFormatterBuilder();

        if (dur.isShorterThan(Duration.standardMinutes(1))) {
            pfb.appendSeconds().appendLiteral("s");
        } else if (dur.isShorterThan(Duration.standardHours(1))) {
            pfb.appendMinutes().appendLiteral("m");
        } else if (dur.isShorterThan(Duration.standardDays(2))) {
            pfb.appendHours().appendLiteral("h");
        } else if (dur.isShorterThan(Duration.standardDays(8))) {
            pfb.appendDays().appendLiteral("d");
        } else if (dur.isShorterThan(Duration.standardDays(32))) {
            pfb.appendWeeks().appendLiteral("w");
        } else if (dur.isShorterThan(Duration.standardDays(367))) {
            pfb.appendMonths().appendLiteral("m");
        } else {
            pfb.appendYears().appendLiteral("y");
        }

        return pfb.toFormatter().print(dur.toPeriod());
    }

    @PrePersist
    void prePersist() {
        tweeted = new Date();
    }
}
