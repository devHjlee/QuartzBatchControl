package com.batchcontrol;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class QuartzbatchcontrolBatchApplication {

    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(QuartzbatchcontrolBatchApplication.class, args);
        int exitCode = SpringApplication.exit(context);
        System.exit(exitCode);
    }

}
