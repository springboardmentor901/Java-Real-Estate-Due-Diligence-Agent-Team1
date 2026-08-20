
package com.realestate.due_diligence_agent;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;

@EnableRetry
@SpringBootApplication
public class DueDiligenceAgentApplication {

    public static void main(String[] args) {
        SpringApplication.run(DueDiligenceAgentApplication.class, args);
    }

}
