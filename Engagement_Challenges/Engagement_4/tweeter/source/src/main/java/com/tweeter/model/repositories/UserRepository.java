package com.tweeter.model.repositories;

import com.tweeter.model.hibernate.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends CrudRepository<User, Long> {
    User findOneByUsername(String username);
    List<User> findByFollowing(User users);
}
