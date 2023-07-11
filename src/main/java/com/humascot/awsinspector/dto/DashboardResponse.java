package com.humascot.awsinspector.dto;

import com.humascot.awsinspector.dto.datapoints.DataPoint;
import lombok.Data;

/**
 * package :  com.humascot.awsinspector.dto
 * fileName : DashboardResponse
 * author :  ShinYeaChan
 * date : 2023-07-11
 */
@Data
public class DashboardResponse {
    private DataPoint dataPoint;
    
}
