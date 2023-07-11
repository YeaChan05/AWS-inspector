package com.humascot.awsinspector.dto.datapoints;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * package :  com.humascot.awsinspector.dto.datapoints
 * fileName : Ec2CpuUtilization
 * author :  ShinYeaChan
 * date : 2023-07-11
 */
@Data
public class Ec2CpuUtilization {
    private List<DataPoint> dataPoints=new ArrayList<>();
}
