package com.swp391.EV.service.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(basePackages = "com.swp391.EV.service.repository")
@EntityScan(basePackages = "com.swp391.EV.service.entity")
public class DatabaseConfig {
}