---
layout: default
title: Installing
nav_order: 4
permalink: Installing
---

Here you can find how to install JAICF using different building tools like Gradle and Maven.

## JAICF version

In all examples below you have to replace `$jaicfVersion` with the latest version ![](https://img.shields.io/github/v/release/just-ai/jaicf-kotlin?color=%23000&label=&style=flat-square) 

## Components

Each JAICF component like [channel](Channels) or [NLU connector](Natural-Language-Understanding) should be added to the build configuration directly using separate library.
The version of component's library is the same as for `core` component that should be present in your build configuration for each JAICF project.

For example, to make your project ready for [Amazon Alexa](https://github.com/just-ai/jaicf-kotlin/tree/master/channels/alexa) you have to provide its library in dependencies section of your _build.gradle_:

`implementation("com.just-ai.jaicf:alexa:$jaicfVersion")`

If you'd like to use [CAILA NLU](https://github.com/just-ai/jaicf-kotlin/tree/master/activators/caila) to recognise users' requests and build your scenario using [intents](activators#intent), you have to provide:

`implementation("com.just-ai.jaicf:caila:$jaicfVersion")`

_Please refer to the related JAICF component documentation to learn how to append it to your configuration._

## Gradle

```groovy
plugins {
    id 'org.jetbrains.kotlin.jvm' version '1.4.21'
}
repositories {
    mavenCentral()
}
dependencies {
    implementation "org.jetbrains.kotlin:kotlin-stdlib"
    implementation "com.just-ai.jaicf:core:$jaicfVersion"
}
```

## Gradle Kotlin DSL

```kotlin
plugins {
    kotlin("jvm") version "1.4.21"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("com.just-ai.jaicf:core:$jaicfVersion")
}
```

## Maven

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd">

    <modelVersion>4.0.0</modelVersion>

    <groupId>com.example</groupId>
    <artifactId>sample</artifactId>
    <version>1.0-SNAPSHOT</version>
    <packaging>jar</packaging>

    <properties>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <kotlin.version>1.4.10</kotlin.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.jetbrains.kotlin</groupId>
            <artifactId>kotlin-stdlib</artifactId>
            <version>${kotlin.version}</version>
        </dependency>
        <dependency>
            <groupId>com.just-ai.jaicf</groupId>
            <artifactId>core</artifactId>
            <version>${jaicfVersion}</version>
        </dependency>
    </dependencies>
    
    <build>
        <plugins>
            <plugin>
                <groupId>org.jetbrains.kotlin</groupId>
                <artifactId>kotlin-maven-plugin</artifactId>
                <version>${kotlin.version}</version>
                <executions>
                    <execution>
                        <id>compile</id>
                        <phase>compile</phase>
                        <goals>
                            <goal>compile</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
</project>
```