package com.humascot.awsinspector.controller;

import com.amazonaws.services.ec2.model.InstanceStatusEvent;
import com.humascot.awsinspector.service.Ec2Service;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.List;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class EC2InstanceEventSSEController {
    private final Ec2Service ec2Service;
    @GetMapping(value = "/{instanceId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<ServerSentEvent<List<InstanceStatusEvent>>> streamEC2Events(@PathVariable String instanceId) {
        return Flux.interval(Duration.ofSeconds(5))
                .flatMap(sequence -> {
                    List<InstanceStatusEvent> ec2EventJsons = ec2Service.retrieveEC2Events(instanceId);
                    return Flux.just(ServerSentEvent.<List<InstanceStatusEvent>>builder()
                            .data(ec2EventJsons)
                            .build());
                });
    }
}
