package com.kpi.parallelcomputing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

import java.util.Arrays;

@SpringBootApplication
public class KpiApplication {

    private static final Logger LOGGER = LoggerFactory.getLogger("course");

    public static void main(String[] args) {
        SpringApplication.run(KpiApplication.class, args);
    }

    @Bean
    public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
        return args -> {

            String[] beanNames = ctx.getBeanDefinitionNames();
            Arrays.sort(beanNames);

            LOGGER.debug("Context has count of bean's = " + beanNames.length);

        };
    }
}
