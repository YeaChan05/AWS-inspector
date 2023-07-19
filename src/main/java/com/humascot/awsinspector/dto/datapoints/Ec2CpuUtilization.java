package com.humascot.awsinspector.dto.datapoints;

import com.amazonaws.services.cloudwatch.model.Datapoint;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * package :  com.humascot.awsinspector.dto.datapoints
 * fileName : Ec2CpuUtilization
 * author :  ShinYeaChan
 * date : 2023-07-11
 */
@Data
@AllArgsConstructor(staticName = "of")
public class Ec2CpuUtilization {
    private List<Datapoint> dataPoints=new ArrayList<>();
    public void sort(){
        dataPoints.sort(Comparator.comparing(Datapoint::getTimestamp));
    }
}
