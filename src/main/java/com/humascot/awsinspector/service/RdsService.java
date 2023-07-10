package com.humascot.awsinspector.service;

import com.amazonaws.services.cloudwatch.AmazonCloudWatch;
import com.amazonaws.services.cloudwatch.model.*;
import com.humascot.awsinspector.config.AwsConfig;
import com.humascot.awsinspector.config.AwsProfile;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * package :  com.humascot.awsinspector.service
 * fileName : RdsService
 * author :  ShinYeaChan
 * date : 2023-07-09
 */
@Service
@RequiredArgsConstructor
public class RdsService {
    private final AwsConfig awsConfig;
    private final AwsProfile awsProfile;
    public void getRdsStatus() {
        AmazonCloudWatch amazonCloudWatch = awsConfig.cloudWatch();
        displayRdsCpuUtilization(amazonCloudWatch);
        displayLatency(amazonCloudWatch);
    }
    public static void displayLatency(AmazonCloudWatch amazonCloudWatch) {
        try {
            Instant start = Instant.now().minus(Duration.ofMinutes(10));
            Instant endDate = Instant.now();
            String metricName = "Latency";
            String namespace = "AWS/EC2";
            
            Metric metric =new Metric()
                    .withMetricName(metricName)
                    .withNamespace(namespace);
            
            MetricStat metricStat = new MetricStat()
                    .withStat("Minimum")
                    .withPeriod(60)
                    .withMetric(metric);
            
            MetricDataQuery dataQuery =new MetricDataQuery()
                    .withMetricStat(metricStat)
                    .withId("foo2")
                    .withReturnData(true);
            
            List<MetricDataQuery> dataQueries = new ArrayList<>();
            dataQueries.add(dataQuery);
            
            GetMetricDataRequest getMetricDataRequest = new GetMetricDataRequest()
                    .withMaxDatapoints(100)
                    .withStartTime(Date.from(start))
                    .withEndTime(Date.from(endDate))
                    .withMetricDataQueries(dataQueries);
            
            GetMetricDataResult metricDataResult = amazonCloudWatch.getMetricData(getMetricDataRequest);
            List<MetricDataResult> metricDataResults = metricDataResult.getMetricDataResults();
            
            for (MetricDataResult result : metricDataResults) {
                System.out.println("The label is " + result.getLabel());
                System.out.println("The status code is " + result.getStatusCode());
                System.out.println(result.getId());
                System.out.println(result.getTimestamps());
                System.out.println(result.getValues());
                System.out.println(result.getMessages());
            }
            
        } catch (AmazonCloudWatchException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }
    
    private void displayRdsCpuUtilization(AmazonCloudWatch amazonCloudWatch) {
        GetMetricStatisticsRequest rdsRequest = new GetMetricStatisticsRequest()
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
}
