package com.humascot.awsinspector.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "gateway")
public class AwsProfile {
    private String privateIPAddress;
    private String accessKey;
    private String secretKey;
    private String rdsInstanceID;
}