package com.hackathon.deploy.ref;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum LambdaConfiguration {
    SplitWrite("com.hackathon.lambdas.split_write.Handler"),
    CompressWrite("com.hackathon.lambdas.compress_write.Handler"),
    S3Write("com.hackathon.lambdas.s3_write.Handler");

    @Getter
    private final String handler;
}
