/*
 * This file was generated by the Gradle 'init' task.
 *
 * This is a general purpose Gradle build.
 * Learn more about Gradle by exploring our samples at https://docs.gradle.org/6.7.1/samples
 */
plugins {
    this.id("java")
}

repositories  {
    this.mavenCentral()
}

allprojects {
    this.tasks.withType<JavaCompile>().configureEach {
        sourceCompatibility = "11"
        targetCompatibility = "11"
    }
}