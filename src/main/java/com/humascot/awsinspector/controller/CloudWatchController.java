package com.humascot.awsinspector.controller;


import com.humascot.awsinspector.dto.datapoints.DashboardDto;
import com.humascot.awsinspector.service.Ec2Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * package :  com.humascot.awsinspector.controller
 * fileName : CloudWatchController
 * author :  ShinYeaChan
 * date : 2023-06-30
 */
@RestController
@Slf4j
@RequiredArgsConstructor
@CrossOrigin("http://localhost:3000")
public class CloudWatchController {
    private final Ec2Service ec2Service;
    @GetMapping("/dashboard")
    public ResponseEntity<?> getInstanceStatus(@RequestParam(name = "id") String instanceId){
        DashboardDto ec2Status = ec2Service.getEc2Status(instanceId);
        return ResponseEntity.status(HttpStatus.SC_OK).body(ec2Status);
    }
    
    @GetMapping("/instances")
    public ResponseEntity<?> getInstances(){
        List<String> instances=ec2Service.getRunningInstances();
        return ResponseEntity.status(HttpStatus.SC_OK).body(instances);
    }
}
