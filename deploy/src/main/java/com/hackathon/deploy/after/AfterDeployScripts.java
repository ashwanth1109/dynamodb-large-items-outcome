package com.hackathon.deploy.after;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hackathon.deploy.AwsDeploymentService;
import com.hackathon.deploy.ref.AwsExports;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

import static com.hackathon.deploy.Constants.BACKEND_STACK;
import static com.hackathon.deploy.DeploymentManager.withEnv;

public class AfterDeployScripts {

    public void runAwsExportsGeneration(String env) {
        try {
            String filePath = Path.of("../frontend") + "/aws-exports.json";
            File file = new File(filePath);
            if (file.createNewFile()) {
                System.out.println("File created: " + file.getName());
            } else {
                System.out.println("File already exists.");
            }

            AwsDeploymentService awsDeploymentService = new AwsDeploymentService();

            Map<String, String> backendOutputs = awsDeploymentService
                    .getStackOutputs(env, withEnv(BACKEND_STACK));

            System.out.println(backendOutputs);

            AwsExports awsExports = new AwsExports(
                    "default",
                    env,
                    backendOutputs.get("region"),
                    backendOutputs.get("apiUrl")
            );

            Gson gson = new GsonBuilder().setPrettyPrinting().create();

            FileWriter writer = new FileWriter(filePath);
            writer.write(gson.toJson(awsExports));
            writer.close();
        } catch (IOException e) {
            System.out.println("Error generation aws-exports.json file");
            e.printStackTrace();
        }
    }
}
