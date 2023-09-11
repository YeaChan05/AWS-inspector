package com.humascot.awsinspector.service;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.*;
import com.humascot.awsinspector.config.AwsConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.stream.Collectors;

/**
 * package :  com.humascot.awsinspector.service
 * fileName : Ec2ServiceTest
 * author :  ShinYeaChan
 * date : 2023-09-05
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest
class Ec2ServiceTest {
    @Autowired
    private AwsConfig awsConfig;
    
    @Autowired
    private Ec2Service ec2Service;
    
    private  AmazonEC2 ec2Client;
    
    @BeforeEach
    void setUp() {
        ec2Client = awsConfig.ec2Client();
    }
    
    @Test
    void 목록_가져오기() {
        DescribeInstancesRequest request = new DescribeInstancesRequest();
        DescribeInstancesResult result = ec2Client.describeInstances();
    
        // 가져온 인스턴스 목록을 출력
        // 필요한 다른 정보를 추가로 출력
        result.getReservations().stream()
                .flatMap(reservation -> reservation.getInstances().stream())
                .filter(instance -> instance.getMonitoring().getState().equals("enabled"))
                .filter(instance -> instance.getState().getName().equals("running"))
                .forEach(instance -> {
                    System.out.println("Instance ID: " + instance.getInstanceId());
                    System.out.println("State: " + instance.getState().getName());
                    System.out.println("Public IP: " + instance.getPublicIpAddress());
                    System.out.println("Private IP: " + instance.getPrivateIpAddress());
                    System.out.println("Instance Type: " + instance.getInstanceType());
                    System.out.println("VPC ID: " + instance.getVpcId());
                    System.out.println("Availability Zone: " + instance.getPlacement().getAvailabilityZone());
                    System.out.println("Key Pair Name: " + instance.getKeyName());
                    System.out.println("IAM Role: " + instance.getIamInstanceProfile().getArn());
                    System.out.println("Security Groups: " + instance.getSecurityGroups().stream()
                            .map(GroupIdentifier::getGroupName)
                            .collect(Collectors.joining(", ")));
                    System.out.println("Launch Time: " + instance.getLaunchTime());
                    System.out.println("EBS Optimized: " + instance.getEbsOptimized());
                    System.out.println("Elastic GPU IDs: " + instance.getElasticGpuAssociations().stream()
                            .map(ElasticGpuAssociation::getElasticGpuId)
                            .collect(Collectors.joining(", ")));
                    System.out.println("Elastic Inference Accelerator IDs: " + instance.getElasticInferenceAcceleratorAssociations().stream()
                            .map(ElasticInferenceAcceleratorAssociation::getElasticInferenceAcceleratorAssociationId)
                            .collect(Collectors.joining(", ")));
                    System.out.println("Tags: ");
                    instance.getTags().forEach(tag -> {
                        System.out.println("  Key: " + tag.getKey());
                        System.out.println("  Value: " + tag.getValue());
                    });
                    // 필요한 추가 정보를 출력할 수 있습니다.
                    System.out.println("---------------------------------------------");
                });
    }
    
    @Test
    void 상태_확인() {
        
        DescribeInstanceStatusRequest request = new DescribeInstanceStatusRequest();
        DescribeInstanceStatusResult response = ec2Client.describeInstanceStatus(request);
    
        response.getInstanceStatuses().forEach(instanceStatus -> {
            System.out.println("Instance ID: " + instanceStatus.getInstanceId());
            System.out.println("Status: " + instanceStatus.getInstanceStatus().getStatus());
            System.out.println("System Status: " + instanceStatus.getSystemStatus().getStatus());
            System.out.println("Events: ");
            List<InstanceStatusEvent> events = instanceStatus.getEvents();
            events.forEach(event -> {
                System.out.println("  Event Code: " + event.getCode());
                System.out.println("  Description: " + event.getDescription());
                System.out.println("  Not Before: " + event.getNotBefore());
                System.out.println("  Not After: " + event.getNotAfter());
                System.out.println("  Instance Event ID: " + event.getInstanceEventId());
                System.out.println("---------------------------------------------");
            });
        });
    
    }
    
    @Test
    void 비용_측정() {
//        ec2Service.costExplorerMonthly();
        ec2Service.costExplorerHourly();
    }
}