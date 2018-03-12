package com.tweeter.model.repositories;

import com.tweeter.model.hibernate.AlternativeSuggestions;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CorrectionRepository extends CrudRepository<AlternativeSuggestions, Long> {}
