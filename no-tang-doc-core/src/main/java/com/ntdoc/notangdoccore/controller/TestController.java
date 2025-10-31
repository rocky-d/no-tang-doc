package com.ntdoc.notangdoccore.controller;

import com.ntdoc.notangdoccore.dto.common.ApiResponse;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
@Slf4j
public class TestController {
    @GetMapping
    public ResponseEntity<ApiResponse<String>> getUserDocuments() {
        return ResponseEntity.ok(ApiResponse.success("CONGRETS, YOU'VE GOT IT!"));
    }
}