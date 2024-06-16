package com.sparta.newsfeedteamproject;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
public class NewsfeedTeamProjectApplication {

    public static void main(String[] args) {
        SpringApplication.run(NewsfeedTeamProjectApplication.class, args);
    }
}
