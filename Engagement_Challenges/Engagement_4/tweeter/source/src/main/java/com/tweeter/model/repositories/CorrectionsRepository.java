package com.tweeter.model.repositories;

import com.tweeter.model.hibernate.TweetCorrections;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CorrectionsRepository extends CrudRepository<TweetCorrections, Long> {}
