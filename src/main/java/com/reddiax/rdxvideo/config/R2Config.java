package com.reddiax.rdxvideo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.net.URI;

@Configuration
public class R2Config {

    @Bean
    public S3Client r2S3Client(
            @Value("${cloudflare.r2.account-id}") String accountId,
            @Value("${cloudflare.r2.access-key}") String accessKey,
            @Value("${cloudflare.r2.secret-key}") String secretKey
    ) {
        return S3Client.builder()
                .endpointOverride(URI.create(
                        "https://" + accountId + ".r2.cloudflarestorage.com"
                ))
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(accessKey, secretKey)
                        )
                )
                .region(Region.US_EAST_1) // required, value ignored by R2
                .build();
    }
}
