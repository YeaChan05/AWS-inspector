package com.humascot.awsinspector.dto.datapoints;

import com.amazonaws.services.cloudwatch.model.Datapoint;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * package :  com.humascot.awsinspector.dto.datapoints
 * fileName : MemoryUtilization
 * author :  ShinYeaChan
 * date : 2023-07-11
 */
@Data
@AllArgsConstructor(staticName = "of")
public class MemoryUtilization {
    private List<Datapoint> dataPoints=new ArrayList<>();
}