package com.hackathon.deploy.stacks;

import com.hackathon.deploy.ref.LambdaConfiguration;
import software.amazon.awscdk.core.Duration;
import software.amazon.awscdk.core.NestedStack;
import software.amazon.awscdk.services.iam.Effect;
import software.amazon.awscdk.services.iam.ManagedPolicy;
import software.amazon.awscdk.services.iam.Policy;
import software.amazon.awscdk.services.iam.PolicyProps;
import software.amazon.awscdk.services.iam.PolicyStatement;
import software.amazon.awscdk.services.iam.Role;
import software.amazon.awscdk.services.iam.RoleProps;
import software.amazon.awscdk.services.iam.ServicePrincipal;
import software.amazon.awscdk.services.lambda.Code;
import software.amazon.awscdk.services.lambda.Function;
import software.amazon.awscdk.services.lambda.FunctionProps;
import software.amazon.awscdk.services.lambda.LayerVersion;
import software.amazon.awscdk.services.lambda.LayerVersionProps;
import software.amazon.awscdk.services.lambda.Runtime;
import software.amazon.awscdk.services.logs.RetentionDays;
import software.constructs.Construct;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.hackathon.deploy.DeploymentManager.withEnv;

/**
 * Represents stack element for all lambdas
 */
public class LambdaStack extends NestedStack {
    private static final String LAMBDA_STACK = "lambda-stack";
    private static final String LAMBDA_EXECUTION_ROLE_NAME = "lambda-execution";
    private static final String LAMBDA_PRINCIPAL = "lambda.amazonaws.com";
    private static final String LAMBDA_EXECUTE_POLICY_ID = "lambdaExecutePolicy";
    private static final String LAMBDA_EXECUTE_MANAGED_ARN = "arn:aws:iam::aws:policy/AWSLambdaExecute";
    private static final String CLOUDWATCH_PUT_METRIC_DATA = "cloudwatch:PutMetricData";
    private static final String ALL_RESOURCES = "*";
    private static final String S_3_GET_OBJECT = "s3:GetObject";
    private static final String S_3_DELETE_OBJECT = "s3:DeleteObject";
    private static final String S_3_PUT_OBJECT = "s3:PutObject";
    private static final String DYNAMO_ALLOW_ALL = "dynamodb:*";
    private static final String POLICY_ID = "policy";
    private static final String STS_ASSUME_ROLE = "sts:AssumeRole";
    private static final String API_GW_PRINCIPAL = "apigateway.amazonaws.com";

    private static final String LAMBDA_CODE_PATH = Path.of("../")
            .resolve("backend/build/distributions/function").toString();
    private static final String LAMBDA_LIBS_PATH = Path.of("../")
            .resolve("backend/build/distributions/libs").toString();
    private static final String LAMBDA_LAYER_NAME = "lambda-layer";
    public static final String LAMBDA_NAME_PATTERN = "[^\\w_]";
    private static final Integer LAMBDA_DEFAULT_TIMEOUT_MINUTES = 5;
    private static final int LAMBDA_MEMORY_SIZE = 512;

    private final Role executionRole;
    private final Map<String, String> environmentVariables;
    private final Map<String, Function> lambdas;
    private final LayerVersion sharedLayer;


    /**
     * Represents stack element for all lambdas
     */
    public LambdaStack(Construct scope, Map<String, String> environmentVariables) {
        super(scope, withEnv(LAMBDA_STACK));

        lambdas = new HashMap<>();

        this.environmentVariables = environmentVariables;
        executionRole = getExecutionRole();
        sharedLayer = getSharedLayer();

        createLambda(LambdaConfiguration.SplitWrite.getHandler(), LambdaConfiguration.SplitWrite.name());
        createLambda(LambdaConfiguration.CompressWrite.getHandler(), LambdaConfiguration.CompressWrite.name());
        createLambda(LambdaConfiguration.S3Write.getHandler(), LambdaConfiguration.S3Write.name());
    }

    public Function getLambdaByName(LambdaConfiguration lambdaConfiguration) {
        return lambdas.get(lambdaConfiguration.name());
    }

    private void createLambda(String handlerPath, String name) {
        String lambdaName = withEnv(name).replaceAll(LAMBDA_NAME_PATTERN, "_");
        System.out.println("Creating lambda: " + lambdaName);
        FunctionProps functionProps = FunctionProps.builder()
                .handler(handlerPath)
                .functionName(lambdaName)
                .runtime(Runtime.JAVA_11)
                .layers(Collections.singletonList(sharedLayer))
                .code(Code.fromAsset(LAMBDA_CODE_PATH))
                .timeout(Duration.minutes(LAMBDA_DEFAULT_TIMEOUT_MINUTES))
                .role(executionRole)
                .logRetention(RetentionDays.ONE_WEEK)
                .memorySize(LAMBDA_MEMORY_SIZE)
                .environment(environmentVariables)
                .build();

        Function function = new Function(this, lambdaName, functionProps);
        lambdas.put(name, function);
    }

    private Role getExecutionRole() {
        final String roleName = withEnv(LAMBDA_EXECUTION_ROLE_NAME);
        final RoleProps roleProps = RoleProps.builder()
                .roleName(roleName)
                .assumedBy(new ServicePrincipal(LAMBDA_PRINCIPAL))
                .build();
        final Role role = new Role(this, roleName, roleProps);

        role.addManagedPolicy(ManagedPolicy.fromManagedPolicyArn(
                this, LAMBDA_EXECUTE_POLICY_ID, LAMBDA_EXECUTE_MANAGED_ARN));

        // Custom policies here
        List<PolicyStatement> lambdaPolicyStatements = new ArrayList<>(List.of(
                PolicyStatement.Builder.create()
                        .effect(Effect.ALLOW)
                        .actions(List.of(CLOUDWATCH_PUT_METRIC_DATA))
                        .resources(List.of(ALL_RESOURCES)).build(),
                PolicyStatement.Builder.create()
                        .effect(Effect.ALLOW)
                        .actions(List.of(S_3_GET_OBJECT, S_3_DELETE_OBJECT, S_3_PUT_OBJECT))
                        .resources(List.of(ALL_RESOURCES)).build(), // Specific s3 resources to be defined when added
                PolicyStatement.Builder.create()
                        .effect(Effect.ALLOW)
                        .actions(List.of(DYNAMO_ALLOW_ALL))
                        .resources(List.of(ALL_RESOURCES)).build()
        ));

        Policy lambdaPolicy = new Policy(this, POLICY_ID, PolicyProps.builder()
                .statements(lambdaPolicyStatements).build());

        role.attachInlinePolicy(lambdaPolicy);

        role.getAssumeRolePolicy().addStatements(
                PolicyStatement.Builder.create()
                        .effect(Effect.ALLOW)
                        .actions(List.of(STS_ASSUME_ROLE))
                        .principals(List.of(ServicePrincipal.Builder.create(API_GW_PRINCIPAL).build()))
                        .build()
        );

        return role;
    }

    private LayerVersion getSharedLayer() {
        String layerVersionName = withEnv(LAMBDA_LAYER_NAME);
        LayerVersionProps layerVersionProps = LayerVersionProps.builder()
                .compatibleRuntimes(Collections.singletonList(Runtime.JAVA_11))
                .description("Shared layer with all lib jars")
                .code(Code.fromAsset(LAMBDA_LIBS_PATH))
                .layerVersionName(layerVersionName)
                .build();
        return new LayerVersion(this, layerVersionName, layerVersionProps);
    }
}
