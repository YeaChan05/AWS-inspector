package com.humascot.awsinspector.dto.response;

import com.amazonaws.services.cloudwatch.model.MetricDataResult;
import com.humascot.awsinspector.dto.datapoints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * package :  com.humascot.awsinspector.dto
 * fileName : DashboardResponse
 * author :  ShinYeaChan
 * date : 2023-07-11
 */
@Data
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
public class DashboardResponse {
    private Ec2CpuCreditUsage ec2CpuCreditUsage;
    private Ec2CpuUtilization ec2CpuUtilization;
    private MemoryUtilization memoryUtilization;
    private NetworkPacketsIn networkPacketsIn;
    private NetworkPacketsOut networkPacketsOut;
    private List<MetricDataResult> metricData=new ArrayList<>();
    private VolumeInfo volumeInfo;
    
    private static int compare(MetricDataResult o1, MetricDataResult o2) {
        List<Date> timestamps1 = o1.getTimestamps();
        List<Date> timestamps2 = o2.getTimestamps();
        for (int i = 0; i < Math.min(timestamps1.size(), timestamps2.size()); i++) {
            int comparison = timestamps1.get(i).compareTo(timestamps2.get(i));
            if (comparison != 0) {
                return comparison;
            }
        }
        return Integer.compare(timestamps1.size(), timestamps2.size());
    }
    
    public void sort(){
        ec2CpuCreditUsage.sort();
        ec2CpuUtilization.sort();
        memoryUtilization.sort();
        networkPacketsIn.sort();
        networkPacketsOut.sort();
        metricData.sort(DashboardResponse::compare);
        volumeInfo.sort();
    }
}
