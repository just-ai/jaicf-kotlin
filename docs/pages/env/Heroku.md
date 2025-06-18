---
layout: default
title: Heroku
permalink: Heroku
parent: Environments
---

![](/assets/images/env/heroku.png)

[Heroku](https://heroku.com) is a cloud platform that can host your JAICF server running with [Ktor](Ktor), [Spring Boot](Spring-Boot) or any other HTTP server.

# Example

Here is a [ready to use template](https://github.com/just-ai/jaicf-template) that can be used to deploy your JAICF project to Heroku cloud with a single click.
You can investigate its [Procfile](https://github.com/just-ai/jaicf-template/blob/master/Procfile) and [build.gradle.kts](https://github.com/just-ai/jaicf-template/blob/master/build.gradle.kts) to learn how you can integrate your JAICF project with Heroku cloud.

# How to use

Your JAICF project should be compiled into _fat jar file_ that contains all required libraries inside.
Also there should be Procfile available in the root of your project's source.

#### 1. Create a Heroku app

Sign-in and create an app on the [Heroku dashboard](https://dashboard.heroku.com/apps).

#### 2. Add an application and shadow plugins to build.gradle

Also add corresponding `stage` task and main class configuration that points to the class with `main` function.
Here is an example:

```kotlin
plugins {
    application
    kotlin("jvm") version "11"
    id("com.github.johnrengelman.shadow") version "8.1.1"
}
application {
    mainClassName = "com.justai.jaicf.template.ServerKt"
}
repositories {
    mavenCentral()
    jcenter()
}
dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("com.just-ai.jaicf:core:$jaicf")
    implementation("io.ktor:ktor-server-netty:$ktor")
    ...
}
tasks.withType<Jar> {
    manifest {
        attributes(
            mapOf(
                "Main-Class" to application.mainClassName
            )
        )
    }
}

tasks.create("stage") {
    dependsOn("shadowJar")
}
```

#### 3. Add Procfile to the root

Replace "app-name-1.0.0" with your app name and version:

```
web: java -jar build/libs/app-name-1.0.0-all.jar
```

#### 4. Push your code to Heroku

Install [Heroku CLI](https://devcenter.heroku.com/articles/heroku-cli) and run these commands from inside the project's folder:

```
$ git add .
$ git commit -am "make it better"
$ git push heroku master
```

Heroku will build and deploy your server automatically.

## Making changes

Each time you're ready to push some code changes to Heroku, just commit and push your code to the Heroku git as described in section 4.
