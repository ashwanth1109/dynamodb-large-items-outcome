package com.hackathon.deploy;

import com.hackathon.deploy.after.AfterDeployScripts;
import com.hackathon.deploy.stacks.BackendStack;
import software.amazon.awscdk.core.App;
import software.amazon.awscdk.cxapi.CloudAssembly;

import java.io.IOException;

public class DeploymentManager {
    private static String env;
    private static String synthDirectory;
    private static final OsTools osTools = new OsTools();

    private static BackendStack backendStack;

    /**
     * Run the cdk deployment for backend stack
     *
     * @param args - Command line arguments
     *             1st param -> env
     *             2nd param -> run deploy or load testing script
     */
    public static void main(String[] args) throws IllegalArgumentException, IOException, InterruptedException {
        if (args.length < 1) {
            throw new IllegalArgumentException("Arguments not passed correctly");
        }

        env = args[0];
        System.out.println("DeploymentManager is called with the following arguments");
        System.out.println("Env: " + env);

        // Run synth
        synth();

        // Run CDK deployment
        deploy();

        // Run after deploy scripts
        System.out.println("Running after deploy scripts . . .");
        AfterDeployScripts afterDeployScripts = new AfterDeployScripts();
        // Generate aws-exports.json file for frontend
        afterDeployScripts.runAwsExportsGeneration(env);
    }

    public static String withEnv(String name) {
        // Project Name: Dynamo DB Hackathon (ddbh)
        final String PROJECT_NAME = "ddbh";
        return String.format("%s-%s-%s", PROJECT_NAME, name, env);
    }

    /**
     * Build the CF template configuration needed for backend deployment Returns the directory where the template config
     * is stored.
     */
    public static void synth() {
        App app = new App();
        backendStack = new BackendStack(app, env);
        CloudAssembly cs = app.synth();
        synthDirectory = cs.getDirectory();
    }

    /**
     * Run backend deployment
     */
    public static void deploy() throws IOException, InterruptedException {
        osTools.executeCommandAndGetOutput(getCdkDeployCommand(), true);
    }

    /**
     * Get CDK deploy command for DeploymentManager and FrontendDeployment
     *
     * @return CDK deploy command to be executed by os tools
     */
    public static String[] getCdkDeployCommand() {
        return new String[]{"cdk", "--require-approval", "never", "--app", synthDirectory, "deploy" };
    }
}