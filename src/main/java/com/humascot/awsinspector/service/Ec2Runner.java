package com.humascot.awsinspector.service;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.MonitorInstancesRequest;
import com.humascot.awsinspector.config.AwsConfig;
import com.humascot.awsinspector.config.AwsProfile;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * package :  com.humascot.awsinspector
 * fileName : Ec2Runner
 * author :  ShinYeaChan
 * date : 2023-07-02
 */
@Component
@RequiredArgsConstructor
public class Ec2Runner implements ApplicationRunner {
    
    private final AwsConfig awsConfig;
    private final AwsProfile awsProfile;
    @Override
    public void run(ApplicationArguments args) throws Exception {
        AmazonEC2 ec2 = awsConfig.ec2Client();
        MonitorInstancesRequest request=new MonitorInstancesRequest().withInstanceIds(awsProfile.getInstanceID());
        ec2.monitorInstances(request);
    }
}
