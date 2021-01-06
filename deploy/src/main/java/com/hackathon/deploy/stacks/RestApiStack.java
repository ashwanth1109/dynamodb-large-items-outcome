package com.hackathon.deploy.stacks;

import lombok.Getter;
import software.amazon.awscdk.core.Construct;
import software.amazon.awscdk.core.Duration;
import software.amazon.awscdk.core.NestedStack;
import software.amazon.awscdk.services.apigateway.Cors;
import software.amazon.awscdk.services.apigateway.CorsOptions;
import software.amazon.awscdk.services.apigateway.GatewayResponse;
import software.amazon.awscdk.services.apigateway.GatewayResponseProps;
import software.amazon.awscdk.services.apigateway.IResource;
import software.amazon.awscdk.services.apigateway.LambdaIntegration;
import software.amazon.awscdk.services.apigateway.LambdaIntegrationOptions;
import software.amazon.awscdk.services.apigateway.MethodLoggingLevel;
import software.amazon.awscdk.services.apigateway.MethodOptions;
import software.amazon.awscdk.services.apigateway.Resource;
import software.amazon.awscdk.services.apigateway.ResponseType;
import software.amazon.awscdk.services.apigateway.RestApi;
import software.amazon.awscdk.services.apigateway.RestApiProps;
import software.amazon.awscdk.services.apigateway.StageOptions;
import software.amazon.awscdk.services.lambda.Function;

import java.util.ArrayList;
import java.util.Map;

import static com.hackathon.deploy.DeploymentManager.withEnv;

public class RestApiStack extends NestedStack {
    @Getter
    private final RestApi api;

    public RestApiStack(final Construct scope, final String id, final String env) {
        super(scope, id);

        // TODO: Add error response status codes
        RestApiProps restApiProps = RestApiProps.builder()
                .restApiName(String.format("DDBH API REST %s", env))
                .deployOptions(StageOptions.builder()
                        .dataTraceEnabled(true)
                        .loggingLevel(MethodLoggingLevel.INFO)
                        .build())
                .defaultCorsPreflightOptions(CorsOptions.builder()
                        .allowOrigins(Cors.ALL_ORIGINS)
                        .allowMethods(Cors.ALL_METHODS)
                        .allowHeaders(new ArrayList<>(Cors.DEFAULT_HEADERS))
                        .maxAge(Duration.days(7))
                        .build())
                .build();

        api = new RestApi(this, withEnv("rest-api"), restApiProps);

        this.createDefaultResponse("rest-api-response-4xx", ResponseType.DEFAULT_4_XX);
        this.createDefaultResponse("rest-api-response-5xx", ResponseType.DEFAULT_5_XX);
    }

    /**
     * Create a REST API Gateway method for lambdas
     */
    public void createLambdaApiMethod(String resourceName, String verb, Function lambdaFunction,
                                      MethodOptions methodOptions) {
        this.getResource(resourceName).addMethod(verb,
                new LambdaIntegration(lambdaFunction, LambdaIntegrationOptions.builder()
                        .proxy(true)
                        .build()), methodOptions);
    }

    private Resource getResource(String path) {
        String[] parts = path.split("/");
        IResource currentNode = this.api.getRoot();
        for (String part : parts) {
            currentNode = currentNode.getResource(part) == null ? currentNode.addResource(part)
                    : currentNode.getResource(part);
        }
        return (Resource) currentNode;
    }

    private void createDefaultResponse(String name, ResponseType responseType) {
        String gatewayResponseName = withEnv(name);
        Map<String, String> responseHeaders = Map.of("Access-Control-Allow-Origin", "'*'");
        Map<String, String> responseTemplates = Map.of(
                "application/json", "{\"message\":$context.error.messageString}");

        GatewayResponseProps gatewayResponseProps = GatewayResponseProps.builder()
                .type(responseType)
                .restApi(api)
                .responseHeaders(responseHeaders)
                .templates(responseTemplates)
                .build();

        new GatewayResponse(this, gatewayResponseName, gatewayResponseProps);
    }
}
