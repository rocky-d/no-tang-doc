package com.ntdoc.notangdoccore.dto.log;


import com.fasterxml.jackson.annotation.JsonProperty;

public record LogsCountDTO(@JsonProperty("label") String label,@JsonProperty("count") Long count) {
    //For CI/CD Test

    // 紧凑构造器（验证参数）
    public LogsCountDTO {
        if (label == null || label.isBlank()) {
            throw new IllegalArgumentException("Label cannot be null or empty");
        }
        if (count < 0) {
            throw new IllegalArgumentException("Count cannot be negative");
        }
    }

    // 自定义构造器
    public LogsCountDTO(String label) {
        this(label, 0L);
    }

}
