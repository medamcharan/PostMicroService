package com.example.blogapp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class PostServiceApplication {
    private static final Logger logger = LoggerFactory.getLogger(PostServiceApplication.class);

    public static void main(String[] args) {
        logger.info("Starting Post Service Application");
        SpringApplication.run(PostServiceApplication.class, args);
        logger.info("Post Service Application started");
    }

    @Bean
    public RestTemplate restTemplate() {
        logger.info("Creating RestTemplate bean");
        return new RestTemplate();
    }
}