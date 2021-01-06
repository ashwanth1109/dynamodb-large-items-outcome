plugins {
    java
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.amazonaws:aws-lambda-java-core:1.2.1")
    implementation("com.amazonaws:aws-lambda-java-events:3.1.0")
    implementation("com.google.code.gson:gson:2.8.6")
    implementation(group = "software.amazon.awssdk", name = "cloudformation", version = "2.15.33")
    implementation(group = "software.amazon.awssdk", name = "s3", version = "2.15.33")
    implementation(group = "software.amazon.awssdk", name = "lambda", version = "2.15.33")
    implementation(group = "software.amazon.awssdk", name = "dynamodb", version = "2.15.33")

    implementation(group = "com.amazonaws", name = "aws-java-sdk-osgi", version = "1.11.925")

    implementation("org.projectlombok:lombok:1.18.16")
    implementation("junit:junit:4.12")
    annotationProcessor("org.projectlombok:lombok:1.18.16")
    testImplementation("org.projectlombok:lombok:1.18.16")
    testImplementation(group = "org.slf4j", name = "slf4j-api", version = "1.7.30")
    testImplementation("org.slf4j:slf4j-log4j12:1.7.30")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.16")
    testCompile("junit", "junit", "4.12")
}

tasks.register<Copy>("prepareFunction") {
    from(tasks.compileJava)
    from(tasks.processResources)
    destinationDir = buildDir.resolve("distributions/function") // LAMBDA_CODE_PATH
}

/**
 * Prepare lambda libraries (to be deployed as a cdk asset for shared layer)
 */
tasks.register<Copy>("prepareLibs") {
    into("java/lib") {
        from(configurations.runtimeClasspath)
    }
    destinationDir = buildDir.resolve("distributions/libs") // LAMBDA_LIBS_PATH
}

tasks.named("build") { dependsOn("prepareFunction", "prepareLibs") }

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}