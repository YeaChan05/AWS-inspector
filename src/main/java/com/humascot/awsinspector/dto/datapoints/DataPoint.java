package com.humascot.awsinspector.dto.datapoints;

import lombok.Data;

import java.util.Date;

/**
 * package :  com.humascot.awsinspector.dto
 * fileName : DataPoint
 * author :  ShinYeaChan
 * date : 2023-07-11
 */
@Data
public class DataPoint {
    private String sampleCount;
    private String average;
    private String maximum;
    private String minimum;
    private String sum;
    private String unit;
    private Date timeStamp;
}
