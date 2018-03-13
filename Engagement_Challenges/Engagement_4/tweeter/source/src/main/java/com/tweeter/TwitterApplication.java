package com.tweeter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * This is the spring boot application itself (entrypoint). GRTM.
 */
@SpringBootApplication
public class TwitterApplication {
    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(TwitterApplication.class, args);
    }
}


