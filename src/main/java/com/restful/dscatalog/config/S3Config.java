package com.restful.dscatalog.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;


@Configuration
public class S3Config {
    @Bean
    S3Client s3Client(
            @Value("${aws.s3.region:us-east-1}") String region,
            @Value("${aws.access_key_id}") String accessKeyId,
            @Value("${aws.secret_access_key}") String secretKey
    ) {
        var builder = S3Client
                .builder()
                .region(Region.of(region));

        builder.credentialsProvider(!accessKeyId.isBlank() && !secretKey.isBlank()
                ? StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKeyId, secretKey))
                : DefaultCredentialsProvider.builder().build());

        return builder.build();
    }
}