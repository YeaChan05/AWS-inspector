package com.humascot.awsinspector.service;

import com.amazonaws.services.cloudwatch.AmazonCloudWatch;
import com.amazonaws.services.cloudwatch.model.*;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.*;
import com.humascot.awsinspector.config.AwsConfig;
import com.humascot.awsinspector.dto.datapoints.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * package :  com.humascot.awsinspector
 * fileName : Ec2Service
 * author :  ShinYeaChan
 * date : 2023-07-02
 */
@Service
@RequiredArgsConstructor
public class Ec2Service {
    
    private final AwsConfig awsConfig;
    public static final String EC2_NAMESPACE = "AWS/EC2";
    public static final String LINUX_NAMESPACE = "System/Linux";
    
    public DashboardDto getEc2Status(String requestId) {
        AmazonEC2 ec2 = awsConfig.ec2Client();
        AmazonCloudWatch amazonCloudWatch = awsConfig.cloudWatch();
        
        Optional<Instance> runningInstance = verifyRunningInstance(ec2, requestId);
        DashboardDto dashboardDto = new DashboardDto();
        if (Objects.requireNonNull(runningInstance).isPresent()) {
            Dimension dimension = new Dimension()
                    .withName("InstanceId")
                    .withValue(runningInstance.get().getInstanceId());
            dashboardDto = DashboardDto.of(
                    getCPUCreditUsage(amazonCloudWatch, dimension),
                    getEc2CpuUtilization(amazonCloudWatch, dimension),
                    getMemoryUtilization(amazonCloudWatch, dimension),
                    getNetworkPacketsIn(amazonCloudWatch, dimension),
                    getNetworkPacketsOut(amazonCloudWatch, dimension),
                    getDiskReadBytes(amazonCloudWatch)
            );
        }
        return dashboardDto;
    }
    
    private Optional<Instance> verifyRunningInstance(AmazonEC2 ec2, String requestId) {
        List<Reservation> reservations = ec2.describeInstances().getReservations();
        return reservations.stream()
                .flatMap(reservation -> reservation.getInstances().stream())
                .filter(instance -> instance.getMonitoring().getState().equals("enabled"))
                .filter(instance -> instance.getInstanceId().equals(requestId))
                .findAny();
    }
    
    public List<String> getRunningInstances() {
        AmazonEC2 ec2 = awsConfig.ec2Client();
        
        List<Reservation> reservations = ec2.describeInstances().getReservations();
        reservations.stream().flatMap(reservation -> reservation.getInstances().stream())
                .forEach(System.out::println);
        return reservations.stream()
                .flatMap(reservation -> reservation.getInstances().stream())
                .filter(instance -> instance.getState().getName().equals("running"))
                .map(Instance::getInstanceId)
                .collect(Collectors.toList());
    }
    
    public GetMetricStatisticsRequest initMetricStatisticsRequest(String namespace, Dimension dimension, String metricName,
                                                                         Integer Period, Date startTime, Date endTime) {
        return new GetMetricStatisticsRequest()
                .withNamespace(namespace)
                .withDimensions(dimension)
                .withMetricName(metricName)
                .withStatistics(
                        MetricStatType.SAMPLE_COUNT.getType(),
                        MetricStatType.AVERAGE.getType(),
                        MetricStatType.MAXIMUM.getType(),
                        MetricStatType.MINIMUM.getType(),
                        MetricStatType.SUM.getType()
                )
//                .withExtendedStatistics("p99.9", "p50")
                .withPeriod(Period)
                .withStartTime(startTime)
                .withEndTime(endTime);
    }
    
    private Ec2CpuCreditUsage getCPUCreditUsage(AmazonCloudWatch amazonCloudWatch, Dimension dimension) {
        GetMetricStatisticsRequest cpuCreditRequest = initMetricStatisticsRequest(
                EC2_NAMESPACE,
                dimension,
                MetricName.CPU_CREDIT.getValue(),
                60,
                Date.from(Instant.now().minusSeconds(600)),
                Date.from(Instant.now()));
        return Ec2CpuCreditUsage.of(amazonCloudWatch.getMetricStatistics(cpuCreditRequest).getDatapoints());
    }
    
    private NetworkPacketsIn getNetworkPacketsIn(AmazonCloudWatch amazonCloudWatch, Dimension dimension) {
        GetMetricStatisticsRequest networkPacketInRequest = initMetricStatisticsRequest(
                EC2_NAMESPACE,
                dimension,
                MetricName.PACKET_IN.getValue(),
                60,
                Date.from(Instant.now().minusSeconds(600)),
                Date.from(Instant.now()));
        return NetworkPacketsIn.of( amazonCloudWatch.getMetricStatistics(networkPacketInRequest).getDatapoints());
    }
    
    private NetworkPacketsOut getNetworkPacketsOut(AmazonCloudWatch amazonCloudWatch, Dimension dimension) {
        GetMetricStatisticsRequest networkPacketsOutRequest = initMetricStatisticsRequest(
                EC2_NAMESPACE,
                dimension,
                MetricName.PACKET_OUT.getValue(),
                60,
                Date.from(Instant.now().minusSeconds(600)),
                Date.from(Instant.now()));
        return NetworkPacketsOut.of(amazonCloudWatch.getMetricStatistics(networkPacketsOutRequest).getDatapoints());
    }
    
    private MemoryUtilization getMemoryUtilization(AmazonCloudWatch amazonCloudWatch, Dimension dimension) {
        GetMetricStatisticsRequest memoryRequest = initMetricStatisticsRequest(
                LINUX_NAMESPACE,
                dimension,
                MetricName.MEMORY_UTIL_BYTES.getValue(),
                60,
                Date.from(Instant.now().minusSeconds(600)),
                Date.from(Instant.now()));
        return MemoryUtilization.of(amazonCloudWatch.getMetricStatistics(memoryRequest).getDatapoints());
    }
    
    private Ec2CpuUtilization getEc2CpuUtilization(AmazonCloudWatch amazonCloudWatch, Dimension dimension) {
        GetMetricStatisticsRequest cpuRequest = initMetricStatisticsRequest(
                EC2_NAMESPACE,
                dimension,
                MetricName.CPU_UTIL.getValue(),
                60,
                Date.from(Instant.now().minusSeconds(600)),
                Date.from(Instant.now()));
        return Ec2CpuUtilization.of(amazonCloudWatch.getMetricStatistics(cpuRequest).getDatapoints());
    }
    
    public List<MetricDataResult> getDiskReadBytes(AmazonCloudWatch amazonCloudWatch) {
        try {
            Instant start = Instant.now().minusSeconds(600);
            Instant endDate = Instant.now();
            
            MetricDataQuery sampleCountQuery = new MetricDataQuery()
                    .withMetricStat(new MetricStat()
                            .withStat(MetricStatType.SAMPLE_COUNT.getType())
                            .withPeriod(60)
                            .withMetric(new Metric()
                                    .withMetricName(MetricName.DISK_READ_BYTES.getValue())
                                    .withNamespace(EC2_NAMESPACE)))
                    .withId("sampleCountQuery")
                    .withReturnData(true);
            MetricDataQuery averageQuery = new MetricDataQuery()
                    .withMetricStat(new MetricStat()
                            .withStat(MetricStatType.AVERAGE.getType())
                            .withPeriod(60)
                            .withMetric(new Metric()
                                    .withMetricName(MetricName.DISK_READ_BYTES.getValue())
                                    .withNamespace(EC2_NAMESPACE)))
                    .withId("averageQuery")
                    .withReturnData(true);
            MetricDataQuery maximumQuery = new MetricDataQuery()
                    .withMetricStat(new MetricStat()
                            .withStat(MetricStatType.MAXIMUM.getType())
                            .withPeriod(60)
                            .withMetric(new Metric()
                                    .withMetricName(MetricName.DISK_READ_BYTES.getValue())
                                    .withNamespace(EC2_NAMESPACE)))
                    .withId("maximumQuery")
                    .withReturnData(true);
            MetricDataQuery minimumQuery = new MetricDataQuery()
                    .withMetricStat(new MetricStat()
                            .withStat(MetricStatType.MINIMUM.getType())
                            .withPeriod(60)
                            .withMetric(new Metric()
                                    .withMetricName(MetricName.DISK_READ_BYTES.getValue())
                                    .withNamespace(EC2_NAMESPACE)))
                    .withId("minimumQuery")
                    .withReturnData(true);
            MetricDataQuery sumQuery = new MetricDataQuery()
                    .withMetricStat(new MetricStat()
                            .withStat(MetricStatType.SUM.getType())
                            .withPeriod(60)
                            .withMetric(new Metric()
                                    .withMetricName(MetricName.DISK_READ_BYTES.getValue())
                                    .withNamespace(EC2_NAMESPACE)))
                    .withId("sumQuery")
                    .withReturnData(true);
            List<MetricDataQuery> dataQueries = new ArrayList<>();
            dataQueries.add(sampleCountQuery);
            dataQueries.add(averageQuery);
            dataQueries.add(maximumQuery);
            dataQueries.add(minimumQuery);
            dataQueries.add(sumQuery);
            
            GetMetricDataRequest getMetReq = new GetMetricDataRequest()
                    .withMaxDatapoints(100)
                    .withStartTime(Date.from(start))
                    .withEndTime(Date.from(endDate))
                    .withMetricDataQueries(dataQueries);
            
            GetMetricDataResult metricDataResult = amazonCloudWatch.getMetricData(getMetReq);
            return metricDataResult.getMetricDataResults();
            
        } catch (AmazonCloudWatchException e) {
            System.err.println(e.getErrorMessage());
            System.exit(1);
        }
        return null;
    }
    //    private List<Instance> getRunningInstance(AmazonEC2 ec2) {
//        List<Reservation> reservations = ec2.describeInstances().getReservations();
//        return reservations.stream()
//                .flatMap(reservation -> reservation.getInstances()
//                        .stream()
//                        .filter()
//                )
//                .filter(instance -> instance.getMonitoring().getState().equals("enabled"))
//                .collect(Collectors.toList());
//    }
//    public static void displayLatency(AmazonCloudWatch amazonCloudWatch) {
//        try {
//            Instant start = Instant.now().minus(Duration.ofMinutes(10));
//            Instant endDate = Instant.now();
//            String metricName = "Latency";
//            String namespace = "AWS/EC2";
//
//            Metric metric =new Metric()
//                    .withMetricName(metricName)
//                    .withNamespace(namespace);
//
//            MetricStat metricStat = new MetricStat()
//                    .withStat("Minimum")
//                    .withPeriod(60)
//                    .withMetric(metric);
//
//            MetricDataQuery dataQuery =new MetricDataQuery()
//                    .withMetricStat(metricStat)
//                    .withId("foo2")
//                    .withReturnData(true);
//
//            List<MetricDataQuery> dataQueries = new ArrayList<>();
//            dataQueries.add(dataQuery);
//
//            GetMetricDataRequest getMetricDataRequest = new GetMetricDataRequest()
//                    .withMaxDatapoints(100)
//                    .withStartTime(Date.from(start))
//                    .withEndTime(Date.from(endDate))
//                    .withMetricDataQueries(dataQueries);
//
//            GetMetricDataResult metricDataResult = amazonCloudWatch.getMetricData(getMetricDataRequest);
//            List<MetricDataResult> metricDataResults = metricDataResult.getMetricDataResults();
//
//            for (MetricDataResult result : metricDataResults) {
//                System.out.println("The label is " + result.getLabel());
//                System.out.println("The status code is " + result.getStatusCode());
//                System.out.println(result.getId());
//                System.out.println(result.getTimestamps());
//                System.out.println(result.getValues());
//                System.out.println(result.getMessages());
//            }
//
//        } catch (AmazonCloudWatchException e) {
//            System.err.println(e.getMessage());
//            System.exit(1);
//        }
//    }

//    private void displayRdsCpuUtilization(AmazonCloudWatch amazonCloudWatch) {
//        GetMetricStatisticsRequest rdsRequest = new GetMetricStatisticsRequest()
//                .withNamespace("AWS/RDS")
//                .withDimensions(new Dimension()
//                        .withName("DBInstanceIdentifier")
//                        .withValue(awsProfile.getRdsInstanceID()))
//                .withMetricName("CPUUtilization")
//                .withStatistics("Average")
//                .withPeriod(60) // 통계의 간격 (초)
//                .withStartTime(Date.from(Instant.now().minusSeconds(600))) // 10분 전부터
//                .withEndTime(Date.from(Instant.now()));
//
//        GetMetricStatisticsResult rdsResult = amazonCloudWatch.getMetricStatistics(rdsRequest);
//        List<Datapoint> rdsDataPoints = rdsResult.getDatapoints();
//
//        System.out.println("RDS CPU Utilization:");
//        for (Datapoint dataPoint : rdsDataPoints) {
//            System.out.println("Timestamp: " + dataPoint.getTimestamp());
//            System.out.println("Average CPU Utilization: " + dataPoint.getAverage());
//            System.out.println();
//        }
//    }
}
