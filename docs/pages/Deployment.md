---
layout: default
title: Deployment
nav_order: 4
permalink: Deployment
---

Here you can learn how to deploy your JAICF project into production-ready environments using multiple tools and approaches.

> Deployment process and tools depend on your company environment, platforms and experience.
> Here we provide common practices for any JAICF-related project that can be adopted or changed.

## Example

We provide a [comprehensive project example](https://github.com/just-ai/jaicf-jaicp-spring-template) that shows how you can use a production-ready tools with your JAICF application.
It utilises

- [Spring Boot](https://spring.io/projects/spring-boot) for building and running production-ready multi-service configurable application
- [Mongo DB](https://mongodb.com/) for storing application data in noSQL database
- [Prometheus](https://prometheus.io/) for collecting runtime metrics from the application
- [Grafana](https://grafana.com/) for visualising all collected Prometheus metrics 
- [Graylog](https://graylog.org/) for collecting and searching over an application logs
- [Docker](https://docker.com/) for delivering all together to production environment

## Spring Boot

We recommend to use [Spring Boot](Spring-Boot) to pack your JAICF into a self-hosted Spring application.
Spring enables you to separate configurations between development, test and production profiles, as well as re-use Spring components like Spring Data, caches, transactions and others.

#### Spring Actuator

Also Spring provides a production-ready [Actuator](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html) feature that helps to manage and monitor your application by using HTTP endpoints or with JMX.
This module can be easily appended you any JAICF project to allow you to monitor your application state and make some changes in runtime.

## Docker

[Docker](Docker) is the best choice for delivering and running JAICF applications.
It makes it easy to isolate your image from the host environment and guarantee the same behaviour of your application everywhere.

Docker compose could be used to build and run multi-service Dockerized application that includes third-party production-ready services like monitoring tools, log managers and others.
Here is an [example](https://github.com/just-ai/jaicf-jaicp-spring-template/blob/master/docker-compose.yaml) of such Docker compose configuration.

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

#### Graylog

[Graylog](https://graylog.org/) is a great open-source choice for collecting, filtering and searching over an application logs.
It's easy to enable Graylog integration via `logback.xml` configuration.
Here is an [example](https://github.com/just-ai/jaicf-jaicp-spring-template/blob/master/src/main/resources/logback-spring.xml) of such configuration that sends all logs to the selected Graylog server in production mode.

## Monitoring

[There are a lot of open source and enterprise tools and libraries](https://www.overops.com/blog/docker-monitoring-5-methods-for-monitoring-java-applications-in-docker/) that can be used in your JAICF project for monitoring purposes.
These tools help you to track your JAICF bot performance, errors, logs and Docker container metrics.

_There is [another list of open source libraries](https://www.overops.com/blog/java-performance-monitoring-5-open-source-tools-you-should-know/) to monitor and visualize your JAICF bot performance._

#### Prometheus

[Prometheus](https://prometheus.io/) is an open-source tool for collecting runtime metrics from any application.
Micrometer provides an [open-source library](https://micrometer.io/docs/registry/prometheus) that exposes all configured metrics that Prometheus service can collect from your JAICF app.
It can be used in any Spring Boot application via _Actuator_ feature described above.

#### Grafana

[Grafana](https://grafana.com/) is one of the best free to use tools that visualises all collected metrics on configured web dashboards helping you monitor your application behaviour in real time.
All you have to do is to run your Grafana instance and connect it to the Prometheus service via datasources configuration.
Here is an [example](https://github.com/just-ai/jaicf-jaicp-spring-template/blob/master/docker/grafana/provisioning/datasources/all.yml) of such configuration.
