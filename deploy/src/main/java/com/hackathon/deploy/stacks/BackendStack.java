package com.hackathon.deploy.stacks;

import com.hackathon.deploy.constructs.DDBBucket;
import com.hackathon.deploy.constructs.DDBTable;
import com.hackathon.deploy.ref.BackendOutput;
import com.hackathon.deploy.ref.LambdaConfiguration;
import lombok.Getter;
import software.amazon.awscdk.core.CfnOutput;
import software.amazon.awscdk.core.CfnOutputProps;
import software.amazon.awscdk.core.Construct;
import software.amazon.awscdk.core.Stack;

import java.util.Map;

import static com.hackathon.deploy.Constants.BACKEND_STACK;
import static com.hackathon.deploy.DeploymentManager.withEnv;

public class BackendStack extends Stack {
    @Getter
    private final DDBTable table;

    @Getter
    private final RestApiStack restApiStack;
    private final LambdaStack lambdaStack;

    public BackendStack(final Construct parent, String env) {
        super(parent, withEnv(BACKEND_STACK));

        table = new DDBTable(this, withEnv("ddb-table-construct"));
        DDBBucket bucket = new DDBBucket(this, withEnv("ddb-bucket-construct"));

        final Map<String, String> environmentVariables = Map.of(
                "ENV", env,
                "TABLE_NAME", table.getTableName(),
                "BUCKET_NAME", bucket.getBucketName()
        );
        final String restApiId = String.format("rest-api-stack-%s", env);
        restApiStack = new RestApiStack(this, restApiId, env);
        lambdaStack = new LambdaStack(this, environmentVariables);

        addRestMethods();

        new CfnOutput(this, BackendOutput.env.name(), CfnOutputProps.builder()
            .value(env)
            .build());

        new CfnOutput(this, BackendOutput.region.name(), CfnOutputProps.builder()
            .value(restApiStack.getRegion())
            .build());

        new CfnOutput(this, BackendOutput.apiUrl.name(), CfnOutputProps.builder()
            .value(restApiStack.getApi().getUrl())
            .build());
    }

    private void addRestMethods() {
        restApiStack.createLambdaApiMethod("split-write", "POST",
                lambdaStack.getLambdaByName(LambdaConfiguration.SplitWrite), null);

        restApiStack.createLambdaApiMethod("compress-write", "POST",
                lambdaStack.getLambdaByName(LambdaConfiguration.CompressWrite), null);

        restApiStack.createLambdaApiMethod("s3-write", "POST",
                lambdaStack.getLambdaByName(LambdaConfiguration.S3Write), null);
    }
}
