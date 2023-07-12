package com.humascot.awsinspector.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.Bucket;
import com.humascot.awsinspector.config.AwsConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * package :  com.humascot.awsinspector.service
 * fileName : S3Runner
 * author :  ShinYeaChan
 * date : 2023-07-03
 */
@Component
@RequiredArgsConstructor
public class S3Runner implements ApplicationRunner {
    private final AwsConfig awsConfig;
    
    @Override
    public void run(ApplicationArguments args) throws Exception {
        AmazonS3 s3 = awsConfig.s3client();
        List<Bucket> buckets = s3.listBuckets();
//        buckets.forEach(System.out::println);
    }
}
