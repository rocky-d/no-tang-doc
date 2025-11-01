package com.ntdoc.notangdoccore.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@ConfigurationProperties(prefix = "ntdoc.cors")
@Data
public class CorsProps {
    private List<String> allowedOrigins = List.of(); // 默认空
}