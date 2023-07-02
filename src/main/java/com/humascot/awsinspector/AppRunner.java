package com.humascot.awsinspector;

import com.humascot.awsinspector.config.AwsConfig;
import com.humascot.awsinspector.config.AwsProfile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * package :  com.humascot.awsinspector
 * fileName : AppRunner
 * author :  ShinYeaChan
 * date : 2023-07-02
 */
@Component
public class AppRunner implements ApplicationRunner {
    @Autowired
    ApplicationContext applicationContext;
    @Override
    public void run(ApplicationArguments args) throws Exception {
        AwsConfig awsConfig = applicationContext.getBean(AwsConfig.class);
        AwsProfile awsProfile = applicationContext.getBean(AwsProfile.class);
    
        System.out.println(awsConfig.credentials().getAWSAccessKeyId());
        System.out.println(awsConfig.credentials().getAWSSecretKey());
    }
}
