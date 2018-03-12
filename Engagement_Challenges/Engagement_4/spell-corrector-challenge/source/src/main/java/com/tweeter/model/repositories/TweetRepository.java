package com.tweeter.model.repositories;

import com.tweeter.model.hibernate.HashTag;
import com.tweeter.model.hibernate.Tweet;
import com.tweeter.model.hibernate.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TweetRepository extends CrudRepository<Tweet, Long> {
    List<Tweet> findByAuthorAndActionRequiredFalseOrderByTweetedDesc(User user);
    List<Tweet> findByAtMentionsAndActionRequiredFalseOrderByTweetedDesc(User atMention);
    List<Tweet> findByHashTagsAndActionRequiredFalseOrderByTweetedDesc(HashTag hashTag);
    List<Tweet> findByAuthorAndActionRequiredTrueOrderByTweetedDesc(User author);
    List<Tweet> findTop20ByActionRequiredFalseOrderByTweetedDesc();

    long countAllByAuthor(User user);
    long countAllByHashTagsIn(HashTag hashTag);
}
