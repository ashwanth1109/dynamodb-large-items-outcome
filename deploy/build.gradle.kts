plugins {
    id("java")
    id("application")
}

group = "com.hackathon"
version = "1.0"

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
    }
}

//create a single Jar with all dependencies
tasks.register<Jar>("fatJar") {
    manifest {
        attributes("Implementation-Title" to "DynamoDB Hackathon Deployment",
                "Implementation-Version" to version,
                "Main-Class" to "com.hackathon.deploy.DeploymentManager")
    }
    baseName = project.name + "-all"
    from({
        configurations.compileClasspath.get().map { if (it.isDirectory) it else zipTree(it) }
        configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) }
    })
    with(tasks["jar"] as CopySpec)
}

application {
    mainClass.set("com.hackathon.deploy.DeploymentManager")
}

tasks.named("run") { dependsOn("fatJar", ":backend:build") }

dependencies {
    implementation("com.google.code.gson:gson:2.8.6")

    implementation(group = "software.amazon.awscdk", name = "core", version = "1.73.0")
    implementation(group = "software.amazon.awscdk", name = "cloudformation", version = "1.73.0")
    implementation(group = "software.amazon.awscdk", name = "iam", version = "1.73.0")
    implementation(group = "software.amazon.awscdk", name = "lambda", version = "1.73.0")
    implementation(group = "software.amazon.awscdk", name = "apigateway", version = "1.73.0")
    implementation(group = "software.amazon.awscdk", name = "dynamodb", version = "1.73.0")

    implementation(group = "software.amazon.awssdk", name = "cloudformation", version = "2.15.33")
    implementation(group = "software.amazon.awssdk", name = "dynamodb", version = "2.15.33")

    implementation(group = "org.apache.commons", name = "commons-lang3", version = "3.11")
    implementation(group = "org.slf4j", name = "slf4j-log4j12", version = "1.7.30")

    annotationProcessor("org.projectlombok:lombok:1.18.16")
    implementation("org.projectlombok:lombok:1.18.16")
    implementation(group = "org.slf4j", name = "slf4j-api", version = "1.7.30")

    testAnnotationProcessor("org.projectlombok:lombok:1.18.16")
}