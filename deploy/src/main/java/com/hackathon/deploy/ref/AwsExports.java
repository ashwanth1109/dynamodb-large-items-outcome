package com.hackathon.deploy.ref;

public class AwsExports {
    public final String aws_profile;
    public final String aws_env;
    public final String aws_project_region;
    public final String aws_rest_api_url;

    public AwsExports(String aws_profile, String aws_env, String aws_project_region, String aws_rest_api_url) {
        this.aws_profile = aws_profile;
        this.aws_env = aws_env;
        this.aws_project_region = aws_project_region;
        this.aws_rest_api_url = aws_rest_api_url;
    }
}
