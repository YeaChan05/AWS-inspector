package com.humascot.awsinspector.controller;

import com.amazonaws.services.cloudwatch.model.GetMetricDataResult;
import com.amazonaws.services.cloudwatch.model.MetricDataResult;
import com.amazonaws.services.logs.model.GetLogEventsRequest;
import com.amazonaws.services.logs.model.GetLogEventsResult;
import com.amazonaws.services.logs.model.OutputLogEvent;
import com.humascot.awsinspector.config.AwsConfig;
import com.humascot.awsinspector.service.RdsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@CrossOrigin("http://localhost:3000")
public class RdsController {
    private final AwsConfig awsConfig;
    private final RdsService rdsService;
    @GetMapping("/logs")
    public List<String> getLogs() {
        GetLogEventsRequest request = new GetLogEventsRequest()
                .withLogGroupName("inspector")
                .withLogStreamName("inspector-1");
        GetLogEventsResult result = awsConfig.awsLogs().getLogEvents(request);
        return result.getEvents().stream()
                .map(OutputLogEvent::getMessage)
                .collect(Collectors.toList());
    }
    
    @GetMapping("/rdscpu")
    public GetMetricDataResult getMetrics() {
        GetMetricDataResult result = rdsService.getRdsCpuUtilization();
        MetricDataResult metricDataResult = result.getMetricDataResults().get(0);
        
        List<Date> timestamps = metricDataResult.getTimestamps().stream().toList();
        List<Double> values = metricDataResult.getValues();
        
        List<Map.Entry<Date, Double>> entries = new ArrayList<>();
        for (int i = 0; i < timestamps.size(); i++) {
            entries.add(Map.entry(timestamps.get(i), values.get(i)));
        }
        
        entries.sort(Map.Entry.comparingByKey());
        
        List<Date> sortedTimestamps = entries.stream()
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        List<Double> sortedValues = entries.stream()
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());
        
        metricDataResult.setTimestamps(sortedTimestamps);
        metricDataResult.setValues(sortedValues);
        
        return result;
    }
    
    @GetMapping("/connection")
    public Double getConnectionCount(){
        List<MetricDataResult> metricDataResults = rdsService.getConnectionCount().getMetricDataResults();
        Optional<Double> maxVal = metricDataResults.get(metricDataResults.size() - 1).getValues().stream().max(Double::compareTo);
        return maxVal.orElse(null);
    }
    
}
