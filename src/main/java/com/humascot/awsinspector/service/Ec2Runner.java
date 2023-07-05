package com.humascot.awsinspector.service;

import com.amazonaws.services.cloudwatch.AmazonCloudWatch;
import com.amazonaws.services.cloudwatch.model.*;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.Reservation;
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
//        String instanceId = runningInstances.getInstanceId();
//        String instanceId= awsProfile.getInstanceID();
    
        runningInstances.stream().map(Instance::getInstanceId)
                .filter(instanceId -> instanceId.equals(awsProfile.getInstanceID()))
                .forEach(instanceId -> {
            displayEc2CpuUtilization(amazonCloudWatch, instanceId);
//            displayMemoryUtilization(amazonCloudWatch, instanceId);
            displayNetworkPacketsIn(amazonCloudWatch, instanceId);
            displayNetworkPacketsOut(amazonCloudWatch, instanceId);
            displayCPUCreditUsage(amazonCloudWatch, instanceId);
        });
        //            displayElbRequestCount(amazonCloudWatch);
        displayRdsCpuUtilization(amazonCloudWatch);
        System.out.println();
        getMetData(amazonCloudWatch);
    }
    
    private void displayCPUCreditUsage(AmazonCloudWatch amazonCloudWatch, String instanceId) {
        String networkMetricName = "CPUCreditUsage";
        GetMetricStatisticsRequest networkRequest = new GetMetricStatisticsRequest()
                .withNamespace("AWS/EC2")
                .withDimensions(new Dimension()
                        .withName("InstanceId")
                        .withValue(instanceId))
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
    
    private void displayNetworkPacketsIn(AmazonCloudWatch amazonCloudWatch, String instanceId) {
        String networkMetricName = "NetworkPacketsIn";
        GetMetricStatisticsRequest networkRequest = new GetMetricStatisticsRequest()
                .withNamespace("AWS/EC2")
                .withDimensions(new Dimension()
                        .withName("InstanceId")
                        .withValue(instanceId))
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
    
    private void displayNetworkPacketsOut(AmazonCloudWatch amazonCloudWatch, String instanceId) {
        String networkMetricName = "NetworkPacketsOut";
        GetMetricStatisticsRequest networkRequest = new GetMetricStatisticsRequest()
                .withNamespace("AWS/EC2")
                .withDimensions(new Dimension()
                        .withName("InstanceId")
                        .withValue(instanceId))
                .withMetricName(networkMetricName)
                .withStatistics("Sum")
                .withPeriod(60) // 통계의 간격 (초)
                .withStartTime(Date.from(Instant.now().minusSeconds(600))) // 10분 전부터
                .withEndTime(Date.from(Instant.now())); // 현재까지
        GetMetricStatisticsResult networkResult = amazonCloudWatch.getMetricStatistics(networkRequest);
        networkResult.getDatapoints().forEach(dataPoint -> {
            System.out.println("Instance ID: " + instanceId);
            System.out.println("Network Packets Out: " + dataPoint.getSum());
            System.out.println("Timestamp: " + dataPoint.getTimestamp());
            System.out.println();
        });
    }
    
    private void displayRdsCpuUtilization(AmazonCloudWatch amazonCloudWatch) {
        GetMetricStatisticsRequest rdsRequest =new GetMetricStatisticsRequest()
                .withNamespace("AWS/RDS")
                .withDimensions(new Dimension()
                        .withName("DBInstanceIdentifier")
                        .withValue(awsProfile.getRdsInstanceID()))
                .withMetricName("CPUUtilization")
                .withStatistics("Average")
                .withPeriod(60) // 통계의 간격 (초)
                .withStartTime(Date.from(Instant.now().minusSeconds(600))) // 10분 전부터
                .withEndTime(Date.from(Instant.now()));
        
        GetMetricStatisticsResult rdsResult = amazonCloudWatch.getMetricStatistics(rdsRequest);
        List<Datapoint> rdsDataPoints = rdsResult.getDatapoints();
    
        System.out.println("RDS CPU Utilization:");
        for (Datapoint dataPoint : rdsDataPoints) {
            System.out.println("Timestamp: " + dataPoint.getTimestamp());
            System.out.println("Average CPU Utilization: " + dataPoint.getAverage());
            System.out.println();
        }
    }
    
    //    비용 청구됨
    private void displayElbRequestCount(AmazonCloudWatch amazonCloudWatch) {
        GetMetricStatisticsRequest elbRequest =new  GetMetricStatisticsRequest()
                .withNamespace("AWS/ELB")
                .withDimensions(new Dimension()
                        .withName("LoadBalancerName")
                        .withValue("???")) // 로드 밸런서 이름을 변경하세요
                .withMetricName("RequestCount")
                .withStatistics("Sum")
                .withPeriod(300) // 통계의 간격 (초)
                .withStartTime(Date.from(Instant.now().minusSeconds(600))) // 10분 전부터
                .withEndTime(Date.from(Instant.now()));

        GetMetricStatisticsResult elbResult= amazonCloudWatch.getMetricStatistics(elbRequest);
        System.out.println("ELB Request Count:");
        elbResult.getDatapoints().forEach(dataPoint -> {
            System.out.println("Timestamp: " + dataPoint.getTimestamp());
            System.out.println("Sum Request Count: " + dataPoint.getSum());
        });
        System.out.println();
    }
//    비용 청구됨
    private void displayMemoryUtilization(AmazonCloudWatch amazonCloudWatch, String instanceId) {
        String memoryMetricName = "MemoryUtilization";
        GetMetricStatisticsRequest memoryRequest = new GetMetricStatisticsRequest()
                .withNamespace("System/Linux")
                .withDimensions(new Dimension()
                        .withName("InstanceId")
                        .withValue(instanceId))
                .withMetricName(memoryMetricName)
                .withStatistics("Average")
                .withPeriod(60) // 통계의 간격 (초)
                .withStartTime(Date.from(Instant.now().minusSeconds(600))) // 10분 전부터
                .withEndTime(Date.from(Instant.now())); // 현재까지
        GetMetricStatisticsResult memoryResult = amazonCloudWatch.getMetricStatistics(memoryRequest);
        memoryResult.getDatapoints().forEach(dataPoint -> {
            System.out.println("Instance ID: " + instanceId);
            System.out.println("Memory Utilization: " + dataPoint.getAverage());
            System.out.println("Timestamp: " + dataPoint.getTimestamp());
            System.out.println();
        });
    }
    
    private void displayEc2CpuUtilization(AmazonCloudWatch amazonCloudWatch, String instanceId) {
        String cpuMetricName = "CPUUtilization";
        GetMetricStatisticsRequest cpuRequest = new GetMetricStatisticsRequest()
                .withNamespace("AWS/EC2")
                .withDimensions(new Dimension()
                        .withName("InstanceId")
                        .withValue(instanceId))
                .withMetricName(cpuMetricName)
                .withStatistics("Average")
                .withPeriod(60) // 통계의 간격 (초)
                .withStartTime(Date.from(Instant.now().minusSeconds(600))) // 10분 전부터
                .withEndTime(Date.from((Instant.now()))); // 현재까지
        GetMetricStatisticsResult cpuResult = amazonCloudWatch.getMetricStatistics(cpuRequest);
        System.out.println("Instance ID: " + instanceId);
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
    
    
    public static void getMetData( AmazonCloudWatch amazonCloudWatch) {
        try {
            // Set the date.
            Instant start = Instant.now().minusSeconds(600);
            Instant endDate = Instant.now();
            Metric met =new Metric()
                    .withMetricName("DiskReadBytes")
                    .withNamespace("AWS/EC2");
            
            MetricStat metStat =new MetricStat()
                    .withStat("Minimum")
                    .withPeriod(60)
                    .withMetric(met);
            
            MetricDataQuery dataQUery = new MetricDataQuery()
                    .withMetricStat(metStat)
                    .withId("foo2")
                    .withReturnData(true);
            
            List<MetricDataQuery> dq = new ArrayList<>();
            dq.add(dataQUery);
            
            GetMetricDataRequest getMetReq =new GetMetricDataRequest()
                    .withMaxDatapoints(100)
                    .withStartTime(Date.from(start))
                    .withEndTime(Date.from(endDate))
                    .withMetricDataQueries(dq);
            
            GetMetricDataResult metricDataResult = amazonCloudWatch.getMetricData(getMetReq);
            List<MetricDataResult> data = metricDataResult.getMetricDataResults();
            
            for (MetricDataResult item : data) {
                System.out.println("The label is " + item.getLabel());
                System.out.println("The status code is " + item.getStatusCode());
                System.out.println(item.getValues());
            }
            
        } catch (AmazonCloudWatchException e) {
            System.err.println(e.getErrorMessage());
            System.exit(1);
        }
    }
}
