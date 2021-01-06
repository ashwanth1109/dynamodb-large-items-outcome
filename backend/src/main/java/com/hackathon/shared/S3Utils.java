package com.hackathon.shared;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Utility class to help with working with S3
 */
public final class S3Utils {
    // force non-instantiability through the `private` constructor
    private S3Utils() {
        throw new UnsupportedOperationException("This class cannot be instantiated");
    }

    public static S3Client client = S3Client.create();

    /**
     * Helper method to put an object to S3
     * @return S3 access url
     */
    public static String putObjectToS3(String key, String text) {
        String bucketName = System.getenv("BUCKET_NAME");
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();
        RequestBody body = RequestBody.fromString(text);
        client.putObject(putObjectRequest, body);

        return String.format("https://%s.s3.amazonaws.com/%s",
                bucketName, URLEncoder.encode(key, StandardCharsets.UTF_8));
    }
}
