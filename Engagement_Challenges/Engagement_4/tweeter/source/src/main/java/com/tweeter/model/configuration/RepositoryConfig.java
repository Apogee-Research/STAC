package com.tweeter.model.configuration;

import org.springframework.boot.orm.jpa.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Spring JPA persistance configuration. GRTM.
 *
 * Please be aware that just because I've listed the tables I want in the com.tweeter.model.hibernate package, that
 * does not mean that they are the tables that are actually created.
 *
 * Please read the Spring documentation for more on the model.hibernate, model.configuration, and model.repositories
 * packages.
 */
@Configuration
@EntityScan(basePackages = {"com.tweeter.model.hibernate"})
@EnableJpaRepositories(basePackages = {"com.tweeter.model.repositories"})
@EnableTransactionManagement
public class RepositoryConfig {}
