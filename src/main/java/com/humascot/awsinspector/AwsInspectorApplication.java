package com.humascot.awsinspector;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@Slf4j
@ConfigurationPropertiesScan
@SpringBootApplication
public class AwsInspectorApplication {
    public static void main(String[] args) {
        SpringApplication.run(AwsInspectorApplication.class, args);
    }
}
