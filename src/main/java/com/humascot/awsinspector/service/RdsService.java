package com.humascot.awsinspector.service;

import com.amazonaws.services.cloudwatch.model.*;
import com.humascot.awsinspector.config.AwsConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Date;

/**
 * package :  com.humascot.awsinspector.service
 * fileName : RdsService
 * author :  ShinYeaChan
 * date : 2023-07-24
 */
@Service
@RequiredArgsConstructor
public class RdsService {
    private final AwsConfig awsConfig;
    public GetMetricDataResult getRdsCpuUtilization(){
        Dimension dimension = new Dimension()
                .withName("DBInstanceIdentifier")
                .withValue("iiadbinstance");
        Metric metric = new Metric()
                .withNamespace("AWS/RDS")
                .withMetricName("CPUUtilization")
                .withDimensions(dimension);
    
        MetricStat metricStat = new MetricStat()
                .withMetric(metric)
                .withPeriod(300)
                .withStat("Average");
    
        MetricDataQuery query = new MetricDataQuery()
                .withId("m1")
                .withMetricStat(metricStat)
                .withReturnData(true);
    
        GetMetricDataRequest request = new GetMetricDataRequest()
                .withStartTime(Date.from(Instant.now().minusSeconds(3600)))
                .withEndTime(Date.from(Instant.now()))
                .withMetricDataQueries(query);
    
        return awsConfig.cloudWatch().getMetricData(request);
    }
    
    
    public GetMetricDataResult getConnectionCount() {
        Dimension dimension = new Dimension()
                .withName("DBInstanceIdentifier")
                .withValue("iiadbinstance");
        
        Metric metric = new Metric()
                .withNamespace("AWS/RDS")
                .withMetricName("DatabaseConnections")
                .withDimensions(dimension);
        
        MetricStat metricStat = new MetricStat()
                .withMetric(metric)
                .withPeriod(300)
                .withStat("Average");
        
        MetricDataQuery query = new MetricDataQuery()
                .withId("m1")
                .withMetricStat(metricStat)
                .withReturnData(true);
    
        GetMetricDataRequest request = new GetMetricDataRequest()
                .withStartTime(Date.from(Instant.now().minusSeconds(3600)))
                .withEndTime(Date.from(Instant.now()))
                .withMetricDataQueries(query);
        
        return awsConfig.cloudWatch().getMetricData(request);
    }
}
