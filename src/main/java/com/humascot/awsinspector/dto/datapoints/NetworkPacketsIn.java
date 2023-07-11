package com.humascot.awsinspector.dto.datapoints;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * package :  com.humascot.awsinspector.dto
 * fileName : NetworkPacketsIn
 * author :  ShinYeaChan
 * date : 2023-07-11
 */
@Data
public class NetworkPacketsIn{
    private List<DataPoint> dataPoints=new ArrayList<>();
}
