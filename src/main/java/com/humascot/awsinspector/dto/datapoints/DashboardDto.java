package com.humascot.awsinspector.dto.datapoints;

import com.amazonaws.services.cloudwatch.model.MetricDataResult;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * package :  com.humascot.awsinspector.dto
 * fileName : DashboardDto
 * author :  ShinYeaChan
 * date : 2023-07-11
 */
@Data
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
public class DashboardDto {
    private Ec2CpuCreditUsage ec2CpuCreditUsage;
    private Ec2CpuUtilization ec2CpuUtilization;
    private MemoryUtilization memoryUtilization;
    private NetworkPacketsIn networkPacketsIn;
    private NetworkPacketsOut networkPacketsOut;
    private List<MetricDataResult> metricData=new ArrayList<>();
}
