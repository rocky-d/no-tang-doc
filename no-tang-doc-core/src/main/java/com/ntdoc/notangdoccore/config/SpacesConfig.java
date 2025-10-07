package com.ntdoc.notangdoccore.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.net.URI;

@Slf4j
@Configuration
public class SpacesConfig {
    @Value("${digitalocean.spaces.endpoint}")
    private String endpoint;

    @Value("${digitalocean.spaces.region}")
    private String region;

    @Value("${digitalocean.spaces.access-key}")
    private String accessKey;

    @Value("${digitalocean.spaces.secret-key}")
    private String secretKey;

    @Bean
    public S3Client s3Client() {
        log.info("Initializing DigitalOcean Spaces S3 Client with endpoint: {}, region: {}", endpoint, region);

        AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);

        return S3Client.builder()
                .endpointOverride(URI.create(endpoint))
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .serviceConfiguration(S3Configuration.builder()
                        .pathStyleAccessEnabled(false)
                        .build())
                .build();
    }

    @Bean
    public S3Presigner s3Presigner() {
        log.info("Initializing S3 Presigner for DigitalOcean Spaces");

        AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);

        return S3Presigner.builder()
                .endpointOverride(URI.create(endpoint))
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .build();
    }
}