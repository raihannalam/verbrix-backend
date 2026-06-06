package com.verbrix;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class VerbrixApplication {

    public static void main(String[] args) {
        SpringApplication.run(VerbrixApplication.class, args);
    }

}
