---
layout: default
title: Docker
permalink: Docker
parent: Environments
---

<img src="/assets/images/env/docker.png" width="256"/>

[Docker](https://www.docker.com/) enables developer to separate an application from an infrastructure.
This significantly reduces the delay between writing code and running applications in production.

_As a rule Docker is used for production deployments of JAICF bots in Just AI._

# How to use

## Dockerfile

All you need to build a JAICF Docker container - is to write a [Dockerfile](https://docs.docker.com/develop/develop-images/dockerfile_best-practices/) that contains all your JAICF bot needs to be ran.
Being a Java application, JAICF project requires a standard Java environment and settings.

> Please investigate a [sample JAICF project](https://github.com/just-ai/jaicf-jaicp-caila-template) that contains Dockerfile and instructions of how to build and run Docker image.

```dockerfile
FROM openjdk:11-jdk-slim

ENV JAICP_API_TOKEN = ""

EXPOSE 8080

ADD build/libs/app.jar /opt/jaicf/app.jar

ENTRYPOINT ["java", "-DJAICP_API_TOKEN=$JAICP_API_TOKEN", "-jar", "/opt/jaicf/app.jar"]
```

## Fat JAR

The simplest way to run JAICF bot with Docker - is to pack it into a so known _fat JAR_ - JAR archive that contains everything your application needs, including third-party libraries and resources.

This can be done with open source [Shadow JAR](https://imperceptiblethoughts.com/shadow/introduction/) plugin.
If you're using _jaicp-build-plugin_ you don't need to include _shadowJar_ plugin into your _build.gradle.kts_:

```groovy
plugins {
    application
    kotlin("jvm") version "1.4.21"
    id("com.justai.jaicf.jaicp-build-plugin") version "0.1.1"
}

application {
    mainClassName = "com.justai.jaicf.template.connections.JaicpServerKt"
}

tasks {
    shadowJar {
        archiveFileName.set("app.jar")
    }
}

tasks.create("stage") {
    dependsOn("shadowJar")
}

tasks.withType<com.justai.jaicf.plugins.jaicp.build.JaicpBuild> {
    mainClassName.set(application.mainClassName)
}
```

With these settings you only have to run `stage` gradle task to build a fat JAR named `app.jar` and then use it in your Docker image.

## Build and run Docker image

To build your JAICF bot's Docker image just make sure you've built a fat JAR and then run `docker build` command inside the root project's folder:

`docker build -t jaicf-project-name .`

This builds a _jaicf-project-name_ image that can be ran with

`docker run -p 8080:8080 jaicf-project-name`

This command runs a _jaicf-project-name_ image and makes its endpoint accessible via `http://localhost:8080`.

Learn more about how to [build](https://docs.docker.com/engine/reference/commandline/build/) and [run](https://docs.docker.com/engine/reference/commandline/run/) Docker images.

## Java options

To pass any Java machine options to your dockerized JAICF application define `JAVA_TOOL_OPTIONS` environment variable in the Docker's `run` command

`docker run -p 8080:8080 -e "JAVA_TOOL_OPTIONS=-Xms1024m -Xmx2048m" jaicf-project-name`

Learn more about _JAVA_TOOL_OPTIONS_ [here](https://docs.oracle.com/javase/8/docs/technotes/guides/troubleshoot/envvars002.html).
