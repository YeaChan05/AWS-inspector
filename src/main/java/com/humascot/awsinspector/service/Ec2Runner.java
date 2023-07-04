package com.humascot.awsinspector.service;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.CpuOptions;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.Reservation;
import com.humascot.awsinspector.config.AwsConfig;
import com.humascot.awsinspector.config.AwsProfile;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

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
        
        Instance runningInstance = getRunningInstance(ec2);
        System.out.printf(
                "Found instance with id %s, " +
                        "AMI %s, " +
                        "type %s, " +
                        "state %s " +
                        "and monitoring state %s",
                Objects.requireNonNull(runningInstance).getInstanceId(),
                runningInstance.getImageId(),
                runningInstance.getInstanceType(),
                runningInstance.getState().getName(),
                runningInstance.getMonitoring().getState());
        System.out.println();
        CpuOptions cpuOptions = runningInstance.getCpuOptions();
        System.out.println(cpuOptions.getAmdSevSnp());
        System.out.println(cpuOptions.getThreadsPerCore());
        System.out.println(cpuOptions.getCoreCount());
    }
    
    private Instance getRunningInstance(AmazonEC2 ec2) {
        List<Reservation> reservations = ec2.describeInstances().getReservations();
        return reservations.stream()
                .flatMap(reservation -> reservation.getInstances().stream())
                .filter(instance -> instance.getMonitoring().getState().equals("enabled"))
                .findFirst()
                .orElse(null);
    }
}
