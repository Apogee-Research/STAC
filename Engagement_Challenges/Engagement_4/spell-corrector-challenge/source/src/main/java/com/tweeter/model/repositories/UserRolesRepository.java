package com.tweeter.model.repositories;

import com.tweeter.model.hibernate.UserRole;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRolesRepository extends CrudRepository<UserRole, Long> {}
