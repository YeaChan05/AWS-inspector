package com.humascot.awsinspector.service;

import com.amazonaws.services.cloudwatch.AmazonCloudWatch;
import com.amazonaws.services.cloudwatch.model.*;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.Reservation;
import com.humascot.awsinspector.config.AwsConfig;
import com.humascot.awsinspector.config.AwsProfile;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * package :  com.humascot.awsinspector
 * fileName : Ec2AService
 * author :  ShinYeaChan
 * date : 2023-07-02
 */
@Service
@RequiredArgsConstructor
public class Ec2AService {
    
    private final AwsConfig awsConfig;
    private final AwsProfile awsProfile;
    
    public void getEc2Status() {
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
                    displayMemoryUtilization(amazonCloudWatch, dimension);//안됨
                    displayNetworkPacketsIn(amazonCloudWatch, dimension);
                    displayNetworkPacketsOut(amazonCloudWatch, dimension);
                    displayCPUCreditUsage(amazonCloudWatch, dimension);
//                    displayEc2ConnectorsIP(ec2,instanceId);
                });
        //            displayElbRequestCount(amazonCloudWatch);
        System.out.println();
        getDiskReadBytes(amazonCloudWatch);
        
    }
    
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
