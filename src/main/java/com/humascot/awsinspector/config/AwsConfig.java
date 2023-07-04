package com.humascot.awsinspector.config;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.cloudwatch.AmazonCloudWatch;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchClientBuilder;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * package :  com.humascot.awsinspector.config
 * fileName : AwsConfig
 * author :  ShinYeaChan
 * date : 2023-07-02
 */
@Configuration
@RequiredArgsConstructor
public class AwsConfig {
    private final AwsProfile awsProfile;
    @Bean
    public AWSCredentials credentials(){
        return new BasicAWSCredentials(
                awsProfile.getAccessKey(),
                awsProfile.getSecretKey()
        );
    }
    @Bean
    public AmazonEC2 ec2Client(){
        return AmazonEC2ClientBuilder
                .standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials()))
                .withRegion(Regions.AP_NORTHEAST_2)
                .build();
    }
    @Bean
    public AmazonS3 s3client(){
        return AmazonS3ClientBuilder
                .standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials()))
                .withRegion(Regions.AP_NORTHEAST_2)
                .build();
    }
    @Bean
    public AmazonCloudWatch cloudWatch(){
        return AmazonCloudWatchClientBuilder
                .standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials()))
                .withRegion(Regions.AP_NORTHEAST_2)
                .build();
    }
}
