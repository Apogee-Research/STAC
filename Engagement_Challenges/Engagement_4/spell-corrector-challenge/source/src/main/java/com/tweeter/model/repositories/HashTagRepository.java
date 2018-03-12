package com.tweeter.model.repositories;

import com.tweeter.model.hibernate.HashTag;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HashTagRepository extends CrudRepository<HashTag, Long> {
    HashTag findOneByHashTag(String hashTag);
}
