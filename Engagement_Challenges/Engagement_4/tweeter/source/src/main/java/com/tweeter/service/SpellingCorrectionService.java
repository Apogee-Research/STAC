package com.tweeter.service;

import com.tweeter.model.hibernate.AlternativeSuggestions;
import com.tweeter.model.hibernate.TweetCorrections;
import com.tweeter.model.hibernate.HashTag;
import com.tweeter.model.hibernate.Tweet;
import com.tweeter.model.hibernate.User;
import com.tweeter.model.repositories.CorrectionRepository;
import com.tweeter.model.repositories.CorrectionsRepository;
import com.tweeter.model.repositories.HashTagRepository;
import com.tweeter.model.repositories.TweetRepository;
import com.tweeter.model.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The spelling correction service compiles a list of suggestions for each misspelled word in a tweet and stores them
 * for the user to select the correct suggestion.
 */
@Service
public class SpellingCorrectionService {
    private static final Pattern MENTION_PATTERN = Pattern.compile("@[A-Za-z]+");
    private static final Pattern HASH_TAG_PATTERN = Pattern.compile("#[A-Za-z]+");

    private final Spelling spelling;
    private final CorrectionsRepository correctionsRepo;
    private final CorrectionRepository correctionRepo;
    private final UserRepository users;
    private final HashTagRepository hashTags;
    private final TweetRepository tweets;

    @Autowired
    public SpellingCorrectionService(@Qualifier("correctionsRepository") CorrectionsRepository correctionsRepository,
                                     @Qualifier("correctionRepository") CorrectionRepository correctionRepository,
                                     @Qualifier("userRepository") UserRepository userRepository,
                                     @Qualifier("tweetRepository") TweetRepository tweetRepository,
                                     @Qualifier("hashTagRepository") HashTagRepository hashTagRepository) {
        this.correctionsRepo = correctionsRepository;
        this.correctionRepo = correctionRepository;
        this.users = userRepository;
        this.tweets = tweetRepository;
        this.hashTags = hashTagRepository;
        try {
            URL dict = getClass().getClassLoader().getResource("dictionary.txt");
            if (dict != null) {
                spelling = new Spelling(dict.openStream());
            } else throw new RuntimeException("Where is the dictionary");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void correctAndSaveTweet(final String author, final String text) {
        final Task task = new Task(author, text);
        task.run();
        String result = task.getResult();
        TweetCorrections tweetCorrections = task.getTweetCorrections();
        User user = users.findOneByUsername(task.getUser());

        Tweet tweet = new Tweet(user, result);

        Set<User> tweetMentions = tweet.getAtMentions();
        Set<HashTag> tweetTags = tweet.getHashTags();

        Matcher mentions = MENTION_PATTERN.matcher(result);
        Matcher tags = HASH_TAG_PATTERN.matcher(result);

        while (mentions.find()) {
            String mention = mentions.group();
            User aMention = users.findOneByUsername(mention.substring(1));
            if (aMention != null) tweetMentions.add(aMention);
        }

        while (tags.find()) {
            String hashTag = tags.group();
            HashTag aHashTag = hashTags.findOneByHashTag(hashTag.substring(1));
            if (aHashTag == null) {
                aHashTag = new HashTag();
                aHashTag.setHashTag(hashTag.substring(1));
                hashTags.save(aHashTag);
            }
            tweetTags.add(aHashTag);
        }

        tweet.setAtMentions(tweetMentions);
        tweet.setHashTags(tweetTags);
        tweet.setTweetCorrections(tweetCorrections);

        int tc = 0;
        for (AlternativeSuggestions cr : tweetCorrections.getAlternatives()) {
            if (cr != null && cr.getAlternatives() != null) {
                tc += cr.getAlternatives().size();
            }
        }

        tweet.setActionRequired(tc > 0);

        tweets.save(tweet);
    }

    public class Task implements Runnable {
        private final String text;
        private String result = "";
        private TweetCorrections tweetCorrections = new TweetCorrections();
        private String user;

        public Task(String author, String text) {
            this.text = text;
            this.user = author;
        }

        public String getResult() {
            return result;
        }

        @Override
        public void run() {
            Pattern p = Pattern.compile("[@#]?[A-Za-z]+"); // Extract words, mentions, and tags.
            Matcher m = p.matcher(text);
            int textCounter = 0; // Indicates pos 0 in '***a***'
            int index = 0;
            while (m.find()) {
                final int start = m.start(); // Indicates pos 3 in '***a***'
                final int end = m.end(); // Indicates pos 4 in '***a***' and becomes textCounter
                String word = m.group(); // Slice from start to end resulting in 'a'

                if (textCounter < start) { // Do this when the first '***' exists
                    result += text.substring(textCounter, start);
                }

                if (word == null || word.startsWith("@") || word.startsWith("#")) // Don't correct null, mentions, or tags.
                    continue;

                Map<Integer, String> correct = spelling.correct(word);
                if (correct != null && correct.size() > 0) {
                    AlternativeSuggestions corr = new AlternativeSuggestions(index, word, correct);
                    correctionRepo.save(corr);

                    this.tweetCorrections.getAlternatives().add(corr);
                }

                result += word;
                textCounter = end;
                index += 1;
            }
            if (textCounter < text.length()) {
                result += text.substring(textCounter);
            }

            correctionsRepo.save(this.tweetCorrections);
        }

        public String getText() {
            return text;
        }

        public String getUser() {
            return user;
        }

        public void setUser(String user) {
            this.user = user;
        }

        public TweetCorrections getTweetCorrections() {
            return tweetCorrections;
        }
    }

}

