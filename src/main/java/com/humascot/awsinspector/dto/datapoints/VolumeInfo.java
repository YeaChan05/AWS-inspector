package com.humascot.awsinspector.dto.datapoints;

import com.amazonaws.services.ec2.model.InstanceBlockDeviceMapping;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Data
@AllArgsConstructor(staticName = "of")
public class VolumeInfo {
   List<InstanceBlockDeviceMapping> instanceBlockDeviceMappings=new ArrayList<>();
    public void sort(){
        instanceBlockDeviceMappings.sort(Comparator.comparing(blockDevice-> blockDevice.getEbs().getAttachTime()));
    }
}