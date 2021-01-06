package com.hackathon.deploy;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import software.amazon.awssdk.services.cloudformation.CloudFormationClient;
import software.amazon.awssdk.services.cloudformation.model.DescribeStacksRequest;
import software.amazon.awssdk.services.cloudformation.model.DescribeStacksResponse;
import software.amazon.awssdk.services.cloudformation.model.ListStacksRequest;
import software.amazon.awssdk.services.cloudformation.model.ListStacksResponse;
import software.amazon.awssdk.services.cloudformation.model.Stack;
import software.amazon.awssdk.services.cloudformation.model.StackStatus;
import software.amazon.awssdk.services.cloudformation.model.StackSummary;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
public class AwsDeploymentService {

    private static CloudFormationClient cloudFormationClient;

    /**
     * Get hold of all the output variables of a stack given the stack name with environment
     *
     * @param env           environment being deployed to. Example "env-43ed23"
     * @param stackName     Stack name such as "frontend-stack" or "backend-stack"
     */
    public Map<String, String> getStackOutputs(String env, String stackName) {
        Stack stackDescription = getStackDescription(env, stackName);
        Map<String, String> outputs = new HashMap<>();
        stackDescription.outputs().forEach(o -> {
            if (StringUtils.isNotEmpty(o.exportName())) {
                outputs.put(o.exportName(), o.outputValue());
            } else {
                outputs.put(o.outputKey(), o.outputValue());
            }
        });
        return outputs;
    }

    /**
     * Get all the stacks list. Iterates over all pages and makes a single list if there are a lot of stacks
     *
     * @param cloudFormation CF client
     * @param token          Token to be used for pagination. Leave it blank if you are calling first time
     * @param stacks         Results of the past pages. leave it blank if you are calling first time
     * @return A list of stack summaries
     */
    public List<StackSummary> getPaginatedStacks(CloudFormationClient cloudFormation, String token,
                                                 List<StackSummary> stacks) {
        if (stacks == null) {
            stacks = new ArrayList<>();
        }
        ListStacksResponse response = cloudFormation.listStacks(ListStacksRequest.builder()
                .nextToken(token)
                .stackStatusFilters(StackStatus.CREATE_COMPLETE, StackStatus.ROLLBACK_COMPLETE,
                        StackStatus.UPDATE_COMPLETE, StackStatus.UPDATE_ROLLBACK_COMPLETE)
                .build());

        stacks.addAll(response.stackSummaries());
        if (StringUtils.isNotBlank(response.nextToken()) && !response.stackSummaries().isEmpty()) {
            return getPaginatedStacks(cloudFormation, response.nextToken(), stacks);
        }
        return stacks;
    }

    /**
     * Get description for a single stack
     *
     * @param env           environment being deployed to. Example "env-43ed23"
     * @param stackName     Stack name such as "frontend-stack" or "backend-stack"
     * @param stackName If this is provided, the env and stack name are ignored.
     */
    private Stack getStackDescription(String env, String stackName) {
        CloudFormationClient cloudFormation = getCloudFormationClient();
        List<StackSummary> stacks = getPaginatedStacks(cloudFormation, null, null);
        log.info("Looking for stack {}", stackName);
        Optional<StackSummary> rootStack = stacks.stream()
                .filter(s -> s.stackName().equals(stackName))
                .findFirst();

        if (rootStack.isEmpty()) {
            throw new IllegalStateException(String.format("Stack %s not found!", stackName));
        }

        DescribeStacksResponse describeStacksResponse = cloudFormation.describeStacks(DescribeStacksRequest.builder()
                .stackName(rootStack.get().stackName())
                .build());
        if (describeStacksResponse.stacks().isEmpty()) {
            throw new IllegalStateException(String.format("Stack description for %s not found!", stackName));
        }

        return describeStacksResponse.stacks().get(0);
    }

    /**
     * Create or return existing CloudFormation client
     */
    public CloudFormationClient getCloudFormationClient() {
        if (cloudFormationClient == null) {
            cloudFormationClient = CloudFormationClient.builder().build();
        }
        return cloudFormationClient;
    }
}
