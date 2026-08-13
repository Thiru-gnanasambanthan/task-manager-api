package com.taskmanager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Entry point for the Task Manager REST API.
 *
 * @EnableJpaAuditing turns on automatic population of
 * @CreatedDate / @LastModifiedDate fields on every entity
 * that extends BaseEntity - this is the auditing pattern
 * we designed in the LLD (created_at / updated_at on every table).
 */
@SpringBootApplication
@EnableJpaAuditing
public class TaskManagerApplication {

    public static void main(String[] args) {
        SpringApplication.run(TaskManagerApplication.class, args);
    }
}
