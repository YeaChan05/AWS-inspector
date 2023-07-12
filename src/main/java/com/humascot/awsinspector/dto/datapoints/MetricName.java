package com.humascot.awsinspector.dto.datapoints;

/**
 * package :  com.humascot.awsinspector.dto.datapoints
 * fileName : MetricName
 * author :  ShinYeaChan
 * date : 2023-07-12
 */
public enum MetricName {
    DISK_READ_BYTES("DiskReadBytes"),
    MEMORY_UTIL_BYTES("MemoryUtilization"),
    CPU_UTIL("CPUUtilization"),
    PACKET_OUT("NetworkPacketsOut"),
    PACKET_IN("NetworkPacketsIn"),
    CPU_CREDIT("CPUCreditUsage");
    
    
    private final String value;
    
    MetricName(String value) {
        this.value = value;
    }
    
    public String getValue() {
        return value;
    }
}
