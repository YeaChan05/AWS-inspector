package com.humascot.awsinspector.service;

import com.amazonaws.services.cloudwatch.AmazonCloudWatch;
import com.amazonaws.services.cloudwatch.model.*;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.*;
import com.humascot.awsinspector.config.AwsConfig;
import com.humascot.awsinspector.config.AwsProfile;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

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
        AmazonCloudWatch amazonCloudWatch = awsConfig.cloudWatch();
        
        List<Instance> runningInstances = getRunningInstance(ec2);

        
        runningInstances.stream().map(Instance::getInstanceId)
                .filter(instanceId -> instanceId.equals(awsProfile.getInstanceID()))
                .forEach(instanceId -> {
                    Dimension dimension = new Dimension()
                            .withName("InstanceId")
                            .withValue(instanceId);
                    displayEc2CpuUtilization(amazonCloudWatch, dimension);
                    displayMemoryUtilization(amazonCloudWatch, dimension);
                    displayNetworkPacketsIn(amazonCloudWatch, dimension);
                    displayNetworkPacketsOut(amazonCloudWatch, dimension);
                    displayCPUCreditUsage(amazonCloudWatch, dimension);
//                    displayEc2ConnectorsIP(ec2,instanceId);
                });
        //            displayElbRequestCount(amazonCloudWatch);
        System.out.println();
        getDiskReadBytes(amazonCloudWatch);
        
    }

//    private static void displayEc2ConnectorsIP(AmazonEC2 ec2, String instanceId) {
//        DescribeNetworkInterfacesRequest networkInterfacesRequest = new DescribeNetworkInterfacesRequest()
//                .withFilters(new Filter("attachment.instance-id").withValues(instanceId));
//
//        DescribeNetworkInterfacesResult networkInterfacesResult = ec2.describeNetworkInterfaces(networkInterfacesRequest);
//        List<NetworkInterface> networkInterfaces = networkInterfacesResult.getNetworkInterfaces();
//
//        // EC2 인스턴스에 연결된 IP 주소 출력
//        for (NetworkInterface networkInterface : networkInterfaces) {
//            List<GroupIdentifier> securityGroups = networkInterface.getGroups();
//            List<NetworkInterfaceIpv6Address> ipv6Addresses = networkInterface.getIpv6Addresses();
//            List<NetworkInterfacePrivateIpAddress> privateIpAddresses = networkInterface.getPrivateIpAddresses();
//            List<NetworkInterfacePrivateIpAddress> association = networkInterface.getPrivateIpAddresses();
//
//            // IPv4 주소 출력
//            for (NetworkInterfacePrivateIpAddress privateIp : association) {
//                System.out.println("IPv4 Address: " + privateIp.getPrivateIpAddress());
//            }
//
//            // IPv6 주소 출력
//            for (NetworkInterfaceIpv6Address ipv6 : ipv6Addresses) {
//                System.out.println("IPv6 Address: " + ipv6.getIpv6Address());
//            }
//
//            // 보안 그룹 출력
//            for (GroupIdentifier securityGroup : securityGroups) {
//                System.out.println("Security Group: " + securityGroup.getGroupId());
//            }
//
//            // Private IP 주소 출력
//            for (NetworkInterfacePrivateIpAddress privateIpAddress : privateIpAddresses) {
//                System.out.println("Private IP Address: " + privateIpAddress.getPrivateIpAddress());
//            }
//
//            System.out.println();
//        }
//    }
//
    
    private void displayCPUCreditUsage(AmazonCloudWatch amazonCloudWatch, Dimension dimension) {
        String networkMetricName = "CPUCreditUsage";
        GetMetricStatisticsRequest networkRequest = new GetMetricStatisticsRequest()
                .withNamespace("AWS/EC2")
                .withDimensions(dimension)
                .withMetricName(networkMetricName)
                .withStatistics("Sum")
                .withPeriod(60) // 통계의 간격 (초)
                .withStartTime(Date.from(Instant.now().minusSeconds(600))) // 10분 전부터
                .withEndTime(Date.from(Instant.now())); // 현재까지
        GetMetricStatisticsResult cpuCreditUsageResult = amazonCloudWatch.getMetricStatistics(networkRequest);
        cpuCreditUsageResult.getDatapoints().forEach(dataPoint -> {
            System.out.println("Timestamp: " + dataPoint.getTimestamp());
            System.out.println("Sum CPU Credit Usage: " + dataPoint.getSum());
            System.out.println();
        });
    }
    
    private void displayNetworkPacketsIn(AmazonCloudWatch amazonCloudWatch, Dimension dimension) {
        String networkMetricName = "NetworkPacketsIn";
        GetMetricStatisticsRequest networkRequest = new GetMetricStatisticsRequest()
                .withNamespace("AWS/EC2")
                .withDimensions(dimension)
                .withMetricName(networkMetricName)
                .withStatistics("Sum")
                .withPeriod(60) // 통계의 간격 (초)
                .withStartTime(Date.from(Instant.now().minusSeconds(600))) // 10분 전부터
                .withEndTime(Date.from(Instant.now())); // 현재까지
        GetMetricStatisticsResult networkResult = amazonCloudWatch.getMetricStatistics(networkRequest);
        networkResult.getDatapoints().forEach(dataPoint -> {
            System.out.println("Network Packets In: " + dataPoint.getSum());
            System.out.println("Timestamp: " + dataPoint.getTimestamp());
            System.out.println();
        });
    }
    
    private void displayNetworkPacketsOut(AmazonCloudWatch amazonCloudWatch, Dimension dimension) {
        String networkMetricName = "NetworkPacketsOut";
        GetMetricStatisticsRequest networkRequest = new GetMetricStatisticsRequest()
                .withNamespace("AWS/EC2")
                .withDimensions(dimension)
                .withMetricName(networkMetricName)
                .withStatistics("Sum")
                .withPeriod(60) // 통계의 간격 (초)
                .withStartTime(Date.from(Instant.now().minusSeconds(600))) // 10분 전부터
                .withEndTime(Date.from(Instant.now())); // 현재까지
        GetMetricStatisticsResult networkResult = amazonCloudWatch.getMetricStatistics(networkRequest);
        networkResult.getDatapoints().forEach(dataPoint -> {
            System.out.println("Network Packets Out: " + dataPoint.getSum());
            System.out.println("Timestamp: " + dataPoint.getTimestamp());
            System.out.println();
        });
    }
    
    
    //    비용 청구됨
    private void displayElbRequestCount(AmazonCloudWatch amazonCloudWatch) {
        GetMetricStatisticsRequest elbRequest = new GetMetricStatisticsRequest()
                .withNamespace("AWS/ELB")
                .withDimensions(new Dimension()
                        .withName("LoadBalancerName")
                        .withValue("???")) // 로드 밸런서 이름 변경
                .withMetricName("RequestCount")
                .withStatistics("Sum")
                .withPeriod(60) // 통계의 간격 (초)
                .withStartTime(Date.from(Instant.now().minusSeconds(600))) // 10분 전부터
                .withEndTime(Date.from(Instant.now()));
        
        GetMetricStatisticsResult elbResult = amazonCloudWatch.getMetricStatistics(elbRequest);
        System.out.println("ELB Request Count:");
        elbResult.getDatapoints().forEach(dataPoint -> {
            System.out.println("Timestamp: " + dataPoint.getTimestamp());
            System.out.println("Sum Request Count: " + dataPoint.getSum());
        });
        System.out.println();
    }
    
    //    비용 청구됨
    private void displayMemoryUtilization(AmazonCloudWatch amazonCloudWatch, Dimension dimension) {
        String memoryMetricName = "MemoryUtilization";
        GetMetricStatisticsRequest memoryRequest = new GetMetricStatisticsRequest()
                .withNamespace("System/Linux")
                .withDimensions(dimension)
                .withMetricName(memoryMetricName)
                .withStatistics("Average")
                .withPeriod(60) // 통계의 간격 (초)
                .withStartTime(Date.from(Instant.now().minusSeconds(600))) // 10분 전부터
                .withEndTime(Date.from(Instant.now())); // 현재까지
        GetMetricStatisticsResult memoryResult = amazonCloudWatch.getMetricStatistics(memoryRequest);
        memoryResult.getDatapoints().forEach(dataPoint -> {
            System.out.println("Memory Utilization: " + dataPoint.getAverage());
            System.out.println("Timestamp: " + dataPoint.getTimestamp());
            System.out.println();
        });
    }
    
    private void displayEc2CpuUtilization(AmazonCloudWatch amazonCloudWatch, Dimension dimension) {
        String cpuMetricName = "CPUUtilization";
        GetMetricStatisticsRequest cpuRequest = new GetMetricStatisticsRequest()
                .withNamespace("AWS/EC2")
                .withDimensions(dimension)
                .withMetricName(cpuMetricName)
                .withStatistics("Average")
                .withPeriod(60) // 통계의 간격 (초)
                .withStartTime(Date.from(Instant.now().minusSeconds(600))) // 10분 전부터
                .withEndTime(Date.from((Instant.now()))); // 현재까지
        GetMetricStatisticsResult cpuResult = amazonCloudWatch.getMetricStatistics(cpuRequest);
        cpuResult.getDatapoints().forEach(dataPoint -> {
            System.out.println("EC2 CPU Utilization: " + dataPoint.getAverage());
            System.out.println("Timestamp: " + dataPoint.getTimestamp());
            System.out.println();
        });
    }
    
    private List<Instance> getRunningInstance(AmazonEC2 ec2) {
        List<Reservation> reservations = ec2.describeInstances().getReservations();
        return reservations.stream()
                .flatMap(reservation -> reservation.getInstances().stream())
                .filter(instance -> instance.getMonitoring().getState().equals("enabled"))
                .collect(Collectors.toList());
    }
    
    
    public static void getDiskReadBytes(AmazonCloudWatch amazonCloudWatch) {
        try {
            Instant start = Instant.now().minusSeconds(600);
            Instant endDate = Instant.now();
            Metric met = new Metric()
                    .withMetricName("DiskReadBytes")
                    .withNamespace("AWS/EC2");
            
            MetricStat metStat = new MetricStat()
                    .withStat("Minimum")
                    .withPeriod(60)
                    .withMetric(met);
            
            MetricDataQuery dataQuery = new MetricDataQuery()
                    .withMetricStat(metStat)
                    .withId("foo2")
                    .withReturnData(true);
            
            List<MetricDataQuery> dq = new ArrayList<>();
            dq.add(dataQuery);
            
            GetMetricDataRequest getMetReq = new GetMetricDataRequest()
                    .withMaxDatapoints(100)
                    .withStartTime(Date.from(start))
                    .withEndTime(Date.from(endDate))
                    .withMetricDataQueries(dq);
            
            GetMetricDataResult metricDataResult = amazonCloudWatch.getMetricData(getMetReq);
            List<MetricDataResult> data = metricDataResult.getMetricDataResults();
            
            for (MetricDataResult item : data) {
                System.out.println("The label is " + item.getLabel());
                System.out.println("The status code is " + item.getStatusCode());
                System.out.println(" " + item.getStatusCode());
                System.out.println(item.getValues());
            }
            
        } catch (AmazonCloudWatchException e) {
            System.err.println(e.getErrorMessage());
            System.exit(1);
        }
    }
    
   
}
