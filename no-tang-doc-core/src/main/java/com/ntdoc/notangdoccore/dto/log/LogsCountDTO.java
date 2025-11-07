package com.ntdoc.notangdoccore.dto.log;


import com.fasterxml.jackson.annotation.JsonProperty;

public record LogsCountDTO(@JsonProperty("label") String label,@JsonProperty("count") Long count) {
}
