package com.humascot.awsinspector.dto.datapoints;

public enum MetricStatType {
        SAMPLE_COUNT("SampleCount"),
        AVERAGE("Average"),
        MAXIMUM("Maximum"),
        MINIMUM("Minimum"),
        SUM("Sum");

        private final String type;

        MetricStatType(String type) {
            this.type = type;
        }

        public String getType() {
            return type;
        }
    }