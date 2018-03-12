package com.tweeter.model;

import com.tweeter.model.hibernate.AlternativeSuggestions;
import com.tweeter.model.hibernate.TweetCorrections;
import com.tweeter.model.hibernate.Tweet;
import com.tweeter.model.repositories.TweetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This service applies suggested word corrections to tweets while they are still in the actionRequired mode.
 */
@Service
public class TweetCorrectionService {

    private TweetRepository tweets;

    @Autowired
    public TweetCorrectionService(@Qualifier("tweetRepository") TweetRepository tweets) {
        this.tweets = tweets;
    }

    public void nextCorrection(long pendingTweetId, int wordIndex) {
        Tweet one = tweets.findOne(pendingTweetId);
        if (one != null) {
            TweetCorrections tweetCorrections = one.getTweetCorrections();
            if (tweetCorrections == null) {
                return;
            }
            List<AlternativeSuggestions> alternatives = tweetCorrections.getAlternatives();
            AlternativeSuggestions alternative = null;

            int i;
            for (i = 0; i < alternatives.size(); i++) {
                AlternativeSuggestions alternativeSuggestions = alternatives.get(i);
                if (alternativeSuggestions.getWordIndex() == wordIndex) {
                    alternative = alternativeSuggestions;
                    break;
                }
            }

            String text = one.getText();
            String result = "";

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

                if (index == wordIndex && alternative != null) {
                    result += alternative.thisCorrection();
                } else if (index != wordIndex) {
                    result += word;
                }

                textCounter = end;
                index += 1;
            }
            if (textCounter < text.length()) {
                result += text.substring(textCounter);
            }
            one.setText(result);
            if (alternative != null) {
                alternative.nextCorrection();
            }
            tweets.save(one);
        }
    }

    public void finalizeCorrections(long pendingTweetId, int wordIndex) {
        Tweet one = tweets.findOne(pendingTweetId);
        if (one != null) {
            TweetCorrections tweetCorrections = one.getTweetCorrections();
            if (tweetCorrections != null) {
                List<AlternativeSuggestions> alternatives = tweetCorrections.getAlternatives();
                int i = 0;

                while (i < alternatives.size()) {
                    AlternativeSuggestions alternativeSuggestions = alternatives.get(i);
                    if (alternativeSuggestions.getWordIndex() == wordIndex) {
                        alternatives.remove(i);
                        continue;
                    } else if (alternativeSuggestions.getAlternatives().size() == 0) {
                        alternatives.remove(i);
                        continue;
                    }
                    i++;
                }

                if (alternatives.size() == 0) {
                    one.setActionRequired(false);
                }
            } else {
                one.setActionRequired(false);
            }

            tweets.save(one);
        }
    }
}
