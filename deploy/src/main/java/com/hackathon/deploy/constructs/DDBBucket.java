package com.hackathon.deploy.constructs;

import lombok.Getter;
import software.amazon.awscdk.core.Construct;
import software.amazon.awscdk.services.s3.Bucket;
import software.amazon.awscdk.services.s3.BucketProps;

import static com.hackathon.deploy.Constants.DDB_LARGE_BUCKET_SUFFIX;
import static com.hackathon.deploy.DeploymentManager.withEnv;

public class DDBBucket extends Construct {
    @Getter
    private final String bucketName;

    /**
     * Create S3 bucket for storing DynamoDB large items
     */
    public DDBBucket(Construct scope, String id) {
        super(scope, id);

        bucketName = withEnv(DDB_LARGE_BUCKET_SUFFIX);

        BucketProps bucketProps = BucketProps.builder()
                .bucketName(bucketName)
                .build();

        new Bucket(scope, bucketName, bucketProps);
    }
}
