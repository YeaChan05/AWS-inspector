package com.humascot.awsinspector.service;

import com.amazonaws.services.cloudwatch.AmazonCloudWatch;
import com.amazonaws.services.cloudwatch.model.Datapoint;
import com.amazonaws.services.cloudwatch.model.Dimension;
import com.amazonaws.services.cloudwatch.model.GetMetricStatisticsRequest;
import com.amazonaws.services.cloudwatch.model.GetMetricStatisticsResult;
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
import java.util.Date;
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
        AmazonCloudWatch amazonCloudWatch = awsConfig.cloudWatch();
    
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
        String instanceId = runningInstance.getInstanceId();
    
        // CloudWatch 메트릭 이름 설정
        String cpuMetricName = "CPUUtilization";
        String memoryMetricName = "MemoryUtilization";
        String networkMetricName = "NetworkPacketsIn";
    
        // CloudWatch 메트릭 가져오기
        GetMetricStatisticsRequest cpuRequest =new  GetMetricStatisticsRequest()
                .withNamespace("AWS/EC2")
                .withDimensions(new Dimension()
                        .withName("InstanceId")
                        .withValue(instanceId))
                .withMetricName(cpuMetricName)
                .withStatistics("Average")
                .withPeriod(60) // 통계의 간격 (초)
                .withStartTime(Date.from(Instant.now().minusSeconds(600))) // 10분 전부터
                .withEndTime(Date.from((Instant.now()))); // 현재까지
    
        GetMetricStatisticsRequest memoryRequest =new  GetMetricStatisticsRequest()
                .withNamespace("System/Linux")
                .withDimensions(new Dimension()
                        .withName("InstanceId")
                        .withValue( instanceId))
                .withMetricName(memoryMetricName)
                .withStatistics("Average")
                .withPeriod(60) // 통계의 간격 (초)
                .withStartTime(Date.from(Instant.now().minusSeconds(600))) // 10분 전부터
                .withEndTime(Date.from(Instant.now())); // 현재까지
    
        GetMetricStatisticsRequest networkRequest = new  GetMetricStatisticsRequest()
                .withNamespace("AWS/EC2")
                .withDimensions(new Dimension()
                        .withName("InstanceId")
                        .withValue(instanceId))
                .withMetricName(networkMetricName)
                .withStatistics("Sum")
                .withPeriod(60) // 통계의 간격 (초)
                .withStartTime(Date.from(Instant.now().minusSeconds(600))) // 10분 전부터
                .withEndTime(Date.from(Instant.now())); // 현재까지
    
        GetMetricStatisticsResult cpuResponse = amazonCloudWatch.getMetricStatistics(cpuRequest);
        GetMetricStatisticsResult memoryResponse = amazonCloudWatch.getMetricStatistics(memoryRequest);
        GetMetricStatisticsResult networkResponse = amazonCloudWatch.getMetricStatistics(networkRequest);
    
        // 결과 처리
        List<Datapoint> cpuDataPoints = cpuResponse.getDatapoints();
        List<Datapoint> memoryDataPoints = memoryResponse.getDatapoints();
        List<Datapoint> networkDataPoints = networkResponse.getDatapoints();
    
        // CPU 사용량 출력
        for (Datapoint dataPoint : cpuDataPoints) {
            System.out.println("Instance ID: " + instanceId);
            System.out.println("CPU Utilization: " + dataPoint.getAverage());
            System.out.println("Timestamp: " + dataPoint.getTimestamp());
            System.out.println();
        }
    
//        // 메모리 사용량 출력
//        for (Datapoint dataPoint : memoryDataPoints) {
//            System.out.println("Instance ID: " + instanceId);
//            System.out.println("Memory Utilization: " + dataPoint.getAverage());
//            System.out.println("Timestamp: " + dataPoint.getTimestamp());
//            System.out.println();
//        }
//       // 비용 청구하는 서비스인 만큼 아무나 사용 가능한건 아닌듯
    
        // 네트워크 패킷 입력량 출력
        for (Datapoint dataPoint : networkDataPoints) {
            System.out.println("Instance ID: " + instanceId);
            System.out.println("Network Packets In: " + dataPoint.getSum());
            System.out.println("Timestamp: " + dataPoint.getTimestamp());
            System.out.println();
        }
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
