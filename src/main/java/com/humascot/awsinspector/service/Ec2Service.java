package com.humascot.awsinspector.service;

import com.amazonaws.services.cloudwatch.AmazonCloudWatch;
import com.amazonaws.services.cloudwatch.model.Dimension;
import com.amazonaws.services.cloudwatch.model.Metric;
import com.amazonaws.services.cloudwatch.model.*;
import com.amazonaws.services.costexplorer.AWSCostExplorer;
import com.amazonaws.services.costexplorer.model.*;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.*;
import com.humascot.awsinspector.config.AwsConfig;
import com.humascot.awsinspector.dto.datapoints.*;
import com.humascot.awsinspector.dto.response.DashboardResponse;
import com.humascot.awsinspector.service.utils.StringUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

import static com.amazonaws.services.costexplorer.model.Metric.*;

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
    private static final String blendedCost = StringUtil.toPascalCase(BLENDED_COST.name());
    private static final String unblendedCost = StringUtil.toPascalCase(UNBLENDED_COST.name());
    private static final String amortizedCost = StringUtil.toPascalCase(AMORTIZED_COST.name());
    private static final String netAmortizedCost = StringUtil.toPascalCase(NET_AMORTIZED_COST.name());
    private static final String usageQuantity = StringUtil.toPascalCase(USAGE_QUANTITY.name());
    private static final String normalizedUsageAmount = StringUtil.toPascalCase(NORMALIZED_USAGE_AMOUNT.name());
    private static final String netUnblendedCost = StringUtil.toPascalCase(NET_UNBLENDED_COST.name());
    
    public static final List<String> metrics = Arrays.asList(
            blendedCost,
            unblendedCost,
            amortizedCost,
            netAmortizedCost,
            usageQuantity,
            normalizedUsageAmount,
            netUnblendedCost);
    
    public DashboardResponse getEc2Status(String requestId) {
        AmazonEC2 ec2 = awsConfig.ec2Client();
        AmazonCloudWatch amazonCloudWatch = awsConfig.cloudWatch();
        
        Optional<Instance> runningInstance = verifyRunningInstance(ec2, requestId);
        
        DashboardResponse dashboardResponse = new DashboardResponse();
        if (Objects.requireNonNull(runningInstance).isPresent()) {
            Instance instance = runningInstance.get();
            if(!instance.getMonitoring().getState().equals("enabled")){
                MonitorInstancesRequest request = new MonitorInstancesRequest()
                        .withInstanceIds(instance.getInstanceId());
                ec2.monitorInstances(request);
            }
            Dimension dimension = new Dimension()
                    .withName("InstanceId")
                    .withValue(instance.getInstanceId());
            dashboardResponse = DashboardResponse.of(
                    getCPUCreditUsage(amazonCloudWatch, dimension),
                    getEc2CpuUtilization(amazonCloudWatch, dimension),
                    getMemoryUtilization(amazonCloudWatch, dimension),
                    getNetworkPacketsIn(amazonCloudWatch, dimension),
                    getNetworkPacketsOut(amazonCloudWatch, dimension),
                    getDiskReadBytes(amazonCloudWatch),
                    VolumeInfo.of(instance.getBlockDeviceMappings())
            );
        }
        dashboardResponse.sort();
        return dashboardResponse;
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
    
    public List<InstanceStatusEvent> retrieveEC2Events(String instanceId) {
        DescribeInstanceStatusRequest request = new DescribeInstanceStatusRequest();
        DescribeInstanceStatusResult response = awsConfig.ec2Client().describeInstanceStatus(request);
        
        Instant currentTime = Instant.now();
        Instant fiveSecondsAgo = currentTime.minusSeconds(5);
    
        return response.getInstanceStatuses().stream()
                .filter(instanceStatus -> instanceStatus.getInstanceId().equals(instanceId))
                .flatMap(instanceStatus -> instanceStatus.getEvents().stream())
                .filter(event -> {
                    Instant eventTime = event.getNotBefore().toInstant(); // 이벤트 시간
                    return eventTime.isAfter(fiveSecondsAgo) && eventTime.isBefore(currentTime);
                })
                .collect(Collectors.toList());
    }
    
    public void costExplorerMonthly() {
        AWSCostExplorer awsCostExplorer = awsConfig.costExplorerClient();
        
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(30);
        
        GetCostAndUsageRequest costAndUsageRequest = new GetCostAndUsageRequest()
                .withTimePeriod(new DateInterval()
                        .withStart(startDate.toString())
                        .withEnd(endDate.toString()))
                .withGranularity(Granularity.DAILY)
                .withMetrics(metrics)
                
                .withFilter(new Expression()
                        .withDimensions(new DimensionValues()
                                .withKey("INSTANCE_TYPE")
                                .withValues("t3a.micro")));
        
        // 비용 및 사용량 조회
        GetCostAndUsageResult result = awsCostExplorer.getCostAndUsage(costAndUsageRequest);
        
        // 결과 출력
        List<ResultByTime> results = result.getResultsByTime();
        for (ResultByTime res : results) {
            System.out.println("=======================================");
            System.out.println(res.getTimePeriod().getStart());
            System.out.println(res.getTimePeriod().getEnd());
            System.out.println(convertResultData(res,blendedCost));
            System.out.println(convertResultData(res,unblendedCost));
            System.out.println(convertResultData(res,amortizedCost));
            System.out.println(convertResultData(res,netAmortizedCost));
            System.out.println(convertResultData(res,usageQuantity));
            System.out.println(convertResultData(res,normalizedUsageAmount));
            System.out.println(convertResultData(res,netUnblendedCost));
        }
    }
    
    public void costExplorerHourly() {
        AWSCostExplorer awsCostExplorer = awsConfig.costExplorerClient();
        
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(14);
        
        GetCostAndUsageRequest costAndUsageRequest = new GetCostAndUsageRequest()
                .withTimePeriod(new DateInterval()
                        .withStart(String.valueOf(startDate.atStartOfDay(ZoneId.of("UTC")).toInstant()))
                        .withEnd(String.valueOf(endDate.atStartOfDay(ZoneId.of("UTC")).toInstant())))
                .withGranularity(Granularity.HOURLY)
                .withMetrics(metrics)
                
                .withFilter(new Expression()
                        .withDimensions(new DimensionValues()
                                .withKey("INSTANCE_TYPE")
                                .withValues("t3a.micro")));
        
        // 비용 및 사용량 조회
        GetCostAndUsageResult result = awsCostExplorer.getCostAndUsage(costAndUsageRequest);
        
        // 결과 출력
        List<ResultByTime> results = result.getResultsByTime();
        for (ResultByTime res : results) {
            System.out.println("=======================================");
            System.out.println(res.getTimePeriod().getStart());
            System.out.println(res.getTimePeriod().getEnd());
//            System.out.println(res);
            System.out.println(convertResultData(res,blendedCost));
            System.out.println(convertResultData(res,unblendedCost));
            System.out.println(convertResultData(res,amortizedCost));
            System.out.println(convertResultData(res,netAmortizedCost));
            System.out.println(convertResultData(res,usageQuantity));
            System.out.println(convertResultData(res,normalizedUsageAmount));
            System.out.println(convertResultData(res,netUnblendedCost));
        }
    }
    
    private String convertResultData(ResultByTime res, String key){
        return key+": "+res.getTotal().get(key).getAmount()+res.getTotal().get(key).getUnit();
    }
    
}
