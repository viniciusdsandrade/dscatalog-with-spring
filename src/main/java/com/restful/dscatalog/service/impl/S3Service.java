package com.restful.dscatalog.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.nio.file.Path;

@Service
public class S3Service {
    private static final Logger LOG = LoggerFactory.getLogger(S3Service.class);

    private final S3Client s3;

    @Value("${aws.s3.bucket}")
    private String bucketName;

    public S3Service(S3Client s3) {
        this.s3 = s3;
    }

    public void uploadFile(String localFilePath, String key) {
        try {
            var req = PutObjectRequest.builder().bucket(bucketName).key(key).build();
            s3.putObject(req, RequestBody.fromFile(Path.of(localFilePath)));
        } catch (S3Exception e) {
            LOG.error("S3Exception: {}", e.awsErrorDetails().errorMessage());
        } catch (SdkClientException e) {
            LOG.error("SdkClientException: {}", e.getMessage());
        }
    }
}