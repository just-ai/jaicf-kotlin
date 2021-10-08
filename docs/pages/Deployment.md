---
layout: default
title: Deployment
nav_order: 4
permalink: Deployment
---

Here you can learn how to deploy your JAICF project into production-ready environments using multiple tools and approaches.

> Deployment process and tools depends on your company environment, platforms and experience.
> Here we provide common practices for any JAICF-related project that can be adopted or changed.

## Spring Boot

We recommend to use [Spring Boot](Spring-Boot) to pack your JAICF into a self-hosted Spring application.
Spring enables you to separate configurations between development, test and production profiles, as well as re-use Spring components like Spring Data, caches, transactions and others.

#### Spring Actuator

Also Spring provides a production-ready [Actuator](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html#actuator) feature that helps to manage and monitor your application by using HTTP endpoints or with JMX.
This module can be easily appended you any JAICF project to allow you to monitor your application state and make some changes in runtime.

## Docker

[Docker](Docker) is the best choice for delivering and running JAICF applications.
It makes it easy to isolate your image from the host environment and guarantee the same behaviour of your application everywhere.

> Here is an [example project](https://github.com/just-ai/jaicf-jaicp-spring-template) that shows how to build and deploy dockerized Spring-based JAICF bot

## Logging

Every JAICF application produces logs.
We recommend using [Slf4J](http://www.slf4j.org/) with [Logback](http://logback.qos.ch/) to manage logs of your app.

JAICF core provides `Slf4jConversationLogger` that logs every interaction with JAICF bot and can be enabled in `BotEngine`

```kotlin
BotEngine(
    ...
    conversationLoggers = arrayOf(Slf4jConversationLogger())
)
```

#### Spring Boot logging

Spring Boot provides [logging features](https://docs.spring.io/spring-boot/docs/2.1.1.RELEASE/reference/html/boot-features-logging.html) that help to flexibly configure your loggers for development and production environments.

#### Conversation logging

[JAICP](JAICP) module also provides `JaicpConversationLogger` logger that sends all conversation logs to the JAICP server.
You can then analyse these logs via web-interface to learn how your bot converses with users and make changes in scenarios.

This logger can be enabled in `BotEngine` with

```kotlin
BotEngine(
    ...
    conversationLoggers = arrayOf(
        Slf4jConversationLogger(),
        JaicpConversationLogger(jaicpAccessToken)
    )
)
```

## Monitoring

[There are a lot of open source and enterprise tools and libraries](https://www.overops.com/blog/docker-monitoring-5-methods-for-monitoring-java-applications-in-docker/) that can be used in your JAICF project for monitoring purposes.
These tools help you to track your JAICF bot performance, errors, logs and Docker container metrics.

_There is [another list of open source libraries](https://www.overops.com/blog/java-performance-monitoring-5-open-source-tools-you-should-know/) to monitor and visualize your JAICF bot performance._
